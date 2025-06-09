package org.obsidian.client.screen.clickgui.component.module;

import lombok.Getter;
import lombok.Setter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Namespaced;
import net.minecraft.util.ResourceLocation;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.managers.module.settings.Setting;
import org.obsidian.client.managers.module.settings.impl.*;
import org.obsidian.client.screen.clickgui.ClickGuiScreen;
import org.obsidian.client.screen.clickgui.component.WindowComponent;
import org.obsidian.client.screen.clickgui.component.setting.SettingComponent;
import org.obsidian.client.screen.clickgui.component.setting.impl.*;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.keyboard.Keyboard;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.other.SoundUtil;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.StencilUtil;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.client.utils.render.particle.Particle;
import org.obsidian.common.impl.fastrandom.FastRandom;
import org.obsidian.common.impl.taskript.Script;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
public class ModuleComponent extends WindowComponent {
    private final Module module;
    public List<SettingComponent> settingComponents = new ArrayList<>();
    private boolean expanded = false;
    private float settingHeight = 0;
    private final float margin = 5;
    private final Script script = new Script();
    private final Animation toggleAnimation = new Animation();
    private final List<Particle> particles = new ArrayList<>();
    private final ResourceLocation bloom = new Namespaced("particle/bloom.png");
    private final Random random = new FastRandom();

    public ModuleComponent(Module module, ClickGuiScreen clickGui) {
        this.module = module;
        for (Setting<?> setting : module.getSettings()) {
            if (setting instanceof BindSetting value) {
                settingComponents.add(new BindSettingComponent(value));
            }
            if (setting instanceof BooleanSetting value) {
                settingComponents.add(new BooleanSettingComponent(value));
            }
            if (setting instanceof ColorSetting value) {
                settingComponents.add(new ColorSettingComponent(value));
            }
            if (setting instanceof DelimiterSetting value) {
                settingComponents.add(new DelimiterSettingComponent(value));
            }
            if (setting instanceof ListSetting<?> value) {
                settingComponents.add(new ListSettingComponent(value));
            }
            if (setting instanceof ModeSetting value) {
                settingComponents.add(new ModeSettingComponent(value));
            }
            if (setting instanceof MultiBooleanSetting value) {
                settingComponents.add(new MultiBooleanSettingComponent(value));
            }
            if (setting instanceof SliderSetting value) {
                settingComponents.add(new SliderSettingComponent(value));
            }
            if (setting instanceof StringSetting value) {
                settingComponents.add(new StringSettingComponent(value));
            }
        }
        size.set(clickGui.categoryWidth(), clickGui.categoryHeight());
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        settingComponents.forEach(component -> component.resize(minecraft, width, height));
    }

    @Override
    public void init() {
        settingComponents.forEach(SettingComponent::init);
        if (expanded && panel().getExpandedModule() != this) {
            expandAnimation.set(0F);
            expanded = false;
        }
        toggleAnimation.set(module.isEnabled() ? 1F : 0F);
    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        script.update();
        expandAnimation.update();
        hoverAnimation.update();
        toggleAnimation.update();

        particles.removeIf(Particle::isFinished);

        particles.forEach(particle -> {
            particle.update(Mathf.deltaTime());
            particle.handleBoundaryCollision(position.x, position.y, position.x + size.x, position.y + size.y);
        });

        if (expanded && panel().getExpandedModule() != this) {
            expandAnimation.run(0, 0.25, Easings.QUART_OUT, true).onFinished(() -> expanded = false);
        }

        float append = 2F;
        float angle = (float) Mathf.clamp(0F, 1F, ((Math.sin(System.currentTimeMillis() / 200D) + 1F) / 2F));

        boolean isHover = isHover(mouseX, mouseY, position.x + append, position.y + append, size.x - (append * 2), size.y - (append * 2));
        hoverAnimation.run(binding && !script.isFinished() ? 1.5 : (isHover ? 1 : expanded ? angle : 0), 0.25, binding && !script.isFinished() ? Easings.BACK_OUT : Easings.QUAD_OUT, true);

        int color = module.isEnabled() ? ColorUtil.multAlpha(Theme.getInstance().textColor(), alphaPC()) : ColorUtil.overCol(accentColor(), Color.GRAY.getRGB(), 0.75F);
        int settingBackground = ColorUtil.multAlpha(backgroundColor(), 0.5F);

        if (!module.getSettings().isEmpty()) {
            int green = expanded ? ColorUtil.getColor(0, 255, 0, alpha()) : accentColor();
            RenderUtil.bindTexture(bloom);
            int dotSize = 5;
            RectUtil.drawRect(matrix, position.x + size.x - (append * 2) - dotSize, position.y + size.y / 2F - dotSize / 2F, dotSize, dotSize, green, green, green, green, true, true);
        }

        if (hoverAnimation.getValue() != 0) {
            int shadowColor = ColorUtil.multAlpha(accentColor(), hoverAnimation().get() * 0.1F);
            RectUtil.drawRoundedRectShadowed(matrix, position.x + append, position.y + append, size.x - (append * 2), (size.y - (append * 2)), append, hoverAnimation().getValue() * 6F, shadowColor, shadowColor, shadowColor, shadowColor, true, true, true, true);
        }

        RectUtil.drawGradientV(matrix, position.x + append, position.y + append, size.x - (append * 2), (size.y - (append * 2)) / 2F, ColorUtil.multDark(settingBackground, 0.5f), settingBackground, true);
        RectUtil.drawGradientV(matrix, position.x + append, position.y + append + ((size.y - (append * 2)) / 2F), size.x - (append * 2), (size.y - (append * 2)) / 2F, settingBackground, ColorUtil.multDark(settingBackground, 0.5f), true);
        if (toggleAnimation.getValue() > 0F) {
            int overlay = ColorUtil.multAlpha(accentColor(), toggleAnimation.getValue() * 0.6F);
            RectUtil.drawRect(matrix, position.x + append, position.y + append, size.x - (append * 2), size.y - (append * 2), overlay, true);
        }

        boolean noneMatch = panel().getCategoryComponents()
                .stream()
                .noneMatch(category -> category.getModuleComponents()
                        .stream()
                        .anyMatch(module -> module.isBinding() || module.settingComponents
                                .stream()
                                .anyMatch(settingComponent -> settingComponent instanceof StringSettingComponent component && component.textBox.selected)
                        )
                );

        // Текст модуля
        String moduleText = binding ? "Binding | " + Keyboard.keyName(module.getKey()) : (Keyboard.KEY_RIGHT_CONTROL.isKeyDown() && noneMatch) ? Keyboard.keyName(module.getKey()) + " | " + module.getName() : module.getName();

        // Отрисовка текста модуля
        font.drawCenter(matrix, moduleText, position.x + (size.x / 2F), position.y + (size.y / 2.7F), color, moduleFontSize + (hoverAnimation.get() / 2F));

        // Отрисовка "***" над модулем, если isStarred() возвращает true
        if (module.isStarred()) {
            float starTextSize = 8; // Размер шрифта для "***"
            float starTextX = position.x + (size.x / 1.3F) + 11; // Центр по горизонтали
            float starTextY = position.y + (size.y / 1.17F) - (moduleFontSize / 2F) - starTextSize - 2; // Над текстом модуля
            Fonts.SF_BOLD.drawCenter(matrix, "***", starTextX, starTextY, ColorUtil.multAlpha(Theme.getInstance().starColor(), 0.7F), starTextSize); // Используем цвет из темы
        }

        StencilUtil.enable();
        RectUtil.drawRect(matrix, position.x, position.y + size.y, size.x, expandAnimation.getValue() * settingHeight, ColorUtil.getColor(128, 128));
        StencilUtil.read(1);
        RectUtil.drawGradientV(matrix, position.x + 1F, position.y + size.y, size.x - 2F, expandAnimation.getValue() * (settingHeight - margin / 2F), ColorUtil.multAlpha(backgroundColor(), 0.5F), ColorUtil.getColor(0, 0), false);

        settingHeight = 0;
        if ((expanded || !expandAnimation.isFinished()) && !settingComponents.isEmpty()) {
            float offset = 0;
            for (SettingComponent component : settingComponents) {
                if (!component.value().getVisible().get()) continue;
                component.position().set(position.x + margin, position.y + size.y + offset);
                component.size().x = size.x - (margin * 2);
                component.render(matrix, mouseX, mouseY, partialTicks);
                offset += component.size().y;
            }
            settingHeight = offset;
        }
        StencilUtil.disable();
        RenderUtil.bindTexture(bloom);
        for (Particle particle : particles) {
            particle.setBaseX(position.x + (size.x / 2F));
            particle.setBaseY(position.y + (size.y / 2F));
            particle.getAnimation().run(particle.getTimePC(1500) < 0.5 ? 1 : 0, 1.500 / 2D, Easings.EXPO_OUT, true);
            int white = ColorUtil.multAlpha(accentColor(), particle.getAnimation().get() / 5F);
            RectUtil.drawRect(matrix, particle.getBaseX() - (particle.getSize() * 4), particle.getBaseY() - (particle.getSize() * 4), particle.getSize() * 8.0F, particle.getSize() * 8.0F, ColorUtil.multAlpha(white, 0.05F), true, true);
            RectUtil.drawRect(matrix, particle.getBaseX() - (particle.getSize()), particle.getBaseY() - (particle.getSize()), particle.getSize() * 2.0F, particle.getSize() * 2.0F, white, true, true);
        }
    }

    @Getter
    @Setter
    private boolean binding = false;

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float append = 2F;
        if (isHover(mouseX, mouseY, position.x + append, position.y + append, size.x - (append * 2), size.y - (append * 2))) {
            if (isMClick(button)) {
                panel().getCategoryComponents()
                        .stream()
                        .filter(component -> component.getCategory().equals(module.getCategory()))
                        .flatMap(component -> component.getModuleComponents().stream())
                        .filter(module -> module != this)
                        .forEach(module -> module.setBinding(false));

                setBinding(!isBinding());
            }
            if (!isBinding() && isLClick(button)) {
                module.toggle();
                toggleAnimation.run(module.isEnabled() ? 1F : 0F, 0.35, Easings.CUBIC_OUT);
            }
            if (isRClick(button) && !settingComponents.isEmpty()) {
                expanded = !expanded;
                expandAnimation.run(expanded ? 1 : 0, 0.25, Easings.QUART_OUT);
                SoundUtil.playSound(expanded ? "moduleopen.wav" : "moduleclose.wav");
                if (expanded) panel().setExpandedModule(this);
            }
        }

        boolean valid = button != Keyboard.MOUSE_MIDDLE.getKey()
                && button != Keyboard.MOUSE_RIGHT.getKey()
                && button != Keyboard.MOUSE_LEFT.getKey();

        if (isBinding()) {
            if (valid && script.isFinished()) {
                module.setKey(button);
                stopBinding();
            }
        } else {
            setBinding(false);
        }

        if (expanded && !settingComponents.isEmpty() && expandAnimation.get() == 1.0F && expandAnimation.isFinished()) {
            settingComponents.stream()
                    .filter(component -> component.value().getVisible().get())
                    .forEach(component -> component.mouseClicked(mouseX, mouseY, button));
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!settingComponents.isEmpty()) {
            settingComponents.stream()
                    .filter(component -> component.value().getVisible().get())
                    .forEach(component -> component.mouseReleased(mouseX, mouseY, button));
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (isBinding()) {
            if (keyCode == Keyboard.KEY_ESCAPE.getKey() || keyCode == Keyboard.KEY_DELETE.getKey()) {
                module.setKey(Keyboard.KEY_NONE.getKey());
                stopBinding();
                return true;
            }
            if (script.isFinished()) {
                module.setKey(keyCode);
                stopBinding();
            }
        }
        if (expanded && !settingComponents.isEmpty()) {
            settingComponents.stream()
                    .filter(component -> component.value().getVisible().get())
                    .forEach(component -> component.keyPressed(keyCode, scanCode, modifiers));
        }
        return false;
    }

    private void stopBinding() {
        SoundUtil.playSound("moduleclose.wav");
        script.cleanup().addStep(500, () -> {
            spawnParticles();
            SoundUtil.playSound("moduleopen.wav");
            setBinding(false);
        });
    }

    private void spawnParticles() {
        int randomMultiplier = 50;

        float x = position.x + (size.x / 2F);
        float y = position.y + (size.y / 2F);

        float size = 2F;

        int delay = 1500;

        for (int i = 0; i < 300; i++) {
            float motionX = (random.nextFloat() - 0.5F) * randomMultiplier * (this.size.x / this.size.y);
            float motionY = (random.nextFloat() - 0.5F) * randomMultiplier;
            particles.add(new Particle(motionX + x, motionY + y, size, motionX, motionY, 0F, 0F, 0F, delay));
        }
    }


    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!expanded || settingComponents.isEmpty()) return false;
        for (SettingComponent component : settingComponents) {
            if (component.value().getVisible().get()) {
                component.keyReleased(keyCode, scanCode, modifiers);
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!expanded || settingComponents.isEmpty()) return false;
        for (SettingComponent component : settingComponents) {
            if (component.value().getVisible().get()) {
                component.charTyped(codePoint, modifiers);
            }
        }
        return false;
    }

    @Override
    public void onClose() {
        setBinding(false);
        settingComponents.forEach(SettingComponent::onClose);
    }
}