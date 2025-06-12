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
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.keyboard.Keyboard;
import org.obsidian.client.utils.other.SoundUtil;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.render.particle.Particle;
import org.obsidian.common.impl.fastrandom.FastRandom;
import org.obsidian.common.impl.taskript.Script;

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

        if (expanded && panel().getExpandedModule() != this) {
            expandAnimation.run(0, 0.25, Easings.QUART_OUT, true).onFinished(() -> expanded = false);
        }

        boolean isHover = isHover(mouseX, mouseY);
        hoverAnimation.run(isHover ? 1 : 0, 0.25, Easings.QUAD_OUT, true);

        int bg = ColorUtil.replAlpha(backgroundColor(), alphaPC());
        int border = ColorUtil.replAlpha(Theme.getInstance().textColor(), alphaPC());
        if (toggleAnimation.getValue() > 0F) {
            bg = ColorUtil.overCol(bg, Theme.getInstance().clientColor(), (float) toggleAnimation.getValue());
        }

        if (hoverAnimation.getValue() > 0F) {
            border = ColorUtil.replAlpha(ColorUtil.getColor(240, 240, 240), alphaPC());
        }

        RenderUtil.Rounded.roundedRect(matrix, position.x, position.y, size.x, size.y, bg, Round.of(2));
        RenderUtil.Rounded.roundedOutline(matrix, position.x, position.y, size.x, size.y, 0.5F, border, Round.of(2));

        String moduleText = binding ? "Binding | " + Keyboard.keyName(module.getKey()) : module.getName();
        font.drawCenter(matrix, moduleText, position.x + (size.x / 2F), position.y + (size.y / 2F) - (moduleFontSize / 2F), ColorUtil.multAlpha(Theme.getInstance().textColor(), alphaPC()), moduleFontSize);

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