package org.obsidian.client.screen.clickgui;

import lombok.Getter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.Namespaced;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IMouse;
import org.obsidian.client.api.interfaces.IWindow;
import org.obsidian.client.managers.module.impl.client.ClickGui;
import org.obsidian.client.managers.module.impl.render.Hud;
import org.obsidian.client.screen.clickgui.component.Panel;
import org.obsidian.client.screen.clickgui.component.module.ModuleComponent;
import org.obsidian.client.screen.clickgui.component.setting.impl.StringSettingComponent;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.keyboard.Keyboard;
import org.obsidian.client.utils.math.ScaleMath;
import org.obsidian.client.utils.other.SoundUtil;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.client.utils.render.gif.GifRender;
import org.obsidian.client.utils.render.text.TextAlign;
import org.obsidian.client.utils.render.text.TextBox;
import org.joml.Vector2f;

@Getter
@Accessors(fluent = true)
public class ClickGuiScreen extends Screen implements IMinecraft, IWindow, IMouse {
    private boolean exit = false;
    private final Animation alpha = new Animation();
    private final Animation scale = new Animation();
    private final float categoryWidth = 110, categoryHeight = 20, categoryOffset = 10;
    private final Panel panel = new Panel(this);

    private final TextBox searchField = new TextBox(new Vector2f(), Fonts.SF_MEDIUM, 8, ColorUtil.getColor(255, 255, 255), TextAlign.CENTER, "Search: Ctrl + F", 0, false, false);
    private final GifRender gifRender = new GifRender(new Namespaced("texture/bat.gif"));

    public ClickGuiScreen() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        this.panel.resize(minecraft, width, height);
    }

    @Override
    protected void init() {
        super.init();
        SoundUtil.playSound("guiopen.wav", 0.25);
        Obsidian.inst().configManager().set();
        searchField.setText("");
        searchField.setSelected(false);

        alpha.set(0.0);
        scale.set(0.0);
        alpha.run(1.0, 0.5);
        scale.run(1.0, 0.5, Easings.EXPO_OUT);
        exit = false;

        panel.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;
        draw(matrixStack, finalMouseX, finalMouseY, partialTicks);
    }

    public void draw(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        alpha.update();
        scale.update();
        this.mouseCheck();
        this.closeCheck();
        ScaleMath.scalePre();
        if (!searchField.isSelected()) {
            renderSearchField(matrixStack);
        }
        this.drawPanel(matrixStack, mouseX, mouseY, partialTicks);
        if (searchField.isSelected()) {
            renderSearchField(matrixStack);
        }
        if (ClickGui.getInstance().bat().getValue()) {
            float gifWidth = 316;
            float gifHeight = 308;
            float gifMultiplier = 8;
            gifRender.draw(matrixStack, ScaleMath.getScaled(mw.getScaledWidth()) - (gifWidth / gifMultiplier), ScaleMath.getScaled(mw.getScaledHeight()) - (gifHeight / gifMultiplier) - (Hud.getInstance().isEnabled() && Hud.getInstance().checks().getValue("Information") ? 18 : 2), gifWidth / gifMultiplier, gifHeight / gifMultiplier, alpha.get(), 60F, false);
        }
        ScaleMath.scalePost();
    }

    private void renderSearchField(MatrixStack matrixStack) {
        searchField.setEmptyText("Search: Ctrl + F");
        float searchFieldHeight = searchField.getFontSize() * 2F;

        double searchFieldX = (scaled().x / 2F);
        double searchFieldY = (scaled().y - (scaled().y / 8F));

        float searchWidth = isSearching() ? searchField.getFont().getWidth(searchField.getText(), searchField.getFontSize()) + 10F : searchField.getFont().getWidth(searchField.getEmptyText(), searchField.getFontSize()) + 10F;

        searchField.position.set(searchFieldX, searchFieldY + ((searchFieldHeight / 2F) - (searchField.getFontSize() / 2F)));
        searchField.setWidth(100);
        searchField.setColor(ColorUtil.getColor(128, alpha.get()));

        int color = ColorUtil.getColor(0, 0, 0, alpha.get() / 2F);
        int shadowColor = ColorUtil.getColor(0, 0, 0, alpha.get() / 4F);
        boolean bloom = false;
        float round = 2, shadow = 6;

        RectUtil.drawRoundedRectShadowed(matrixStack, searchFieldX - (searchWidth / 2F), searchFieldY, searchWidth, searchFieldHeight, round, shadow, shadowColor, shadowColor, shadowColor, shadowColor, bloom, true, true, true);
        RectUtil.drawRoundedRectShadowed(matrixStack, searchFieldX - (searchWidth / 2F), searchFieldY, searchWidth, searchFieldHeight, round, 0.5F, color, color, color, color, false, false, true, true);

        searchField.draw(matrixStack);
    }

    private void drawPanel(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        panel.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;

        if (!exit) {
            if (searchField.isSelected() && !isHover(mouseX, mouseY, searchField.position.x, searchField.position.y, searchField.getWidth(), searchField.getFontSize())) {
                searchField.setSelected(false);
                return super.mouseClicked(mouseX, mouseY, button);
            }
            if (!searchField.isSelected()) {
                panel.mouseClicked(finalMouseX, finalMouseY, button);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;
        if (!searchField.isSelected()) {
            panel.mouseReleased(finalMouseX, finalMouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!exit) {
            if (searchField.isSelected() && keyCode == Keyboard.KEY_ESCAPE.getKey()) {
                searchField.setSelected(false);
                return super.keyPressed(keyCode, scanCode, modifiers);
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LEFT_CONTROL.getKey()) && keyCode == Keyboard.KEY_F.getKey()) {
                searchField.setSelected(!searchField.isSelected());
            }
            searchField.keyPressed(keyCode);
            if (!searchField.isSelected()) {
                panel.keyPressed(keyCode, scanCode, modifiers);
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        panel.keyReleased(keyCode, scanCode, modifiers);
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!exit) {
            searchField.charTyped(codePoint);
            if (!searchField.isSelected()) {
                panel.charTyped(codePoint, modifiers);
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        boolean noneMatch = panel.getCategoryComponents().stream().noneMatch(category -> category.getModuleComponents().stream().anyMatch(ModuleComponent::isBinding));
        if (!exit && scale.getValue() > 0.0F && alpha.getValue() > 0.0F && noneMatch) {
            alpha.run(0.0, 0.5);
            scale.run(0.0, 0.5, Easings.BACK_IN);
            exit = true;
            SoundUtil.playSound("guiclose.wav", 0.25);
            mc.mouseHelper.forceGrabMouse(false);
        }
        return false;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        super.onClose();
        Obsidian.inst().configManager().set();
        panel.onClose();
        searchField.setText("");
        searchField.setSelected(false);
    }

    private void mouseCheck() {
        boolean noneMatch = panel.getCategoryComponents()
                .stream()
                .noneMatch(category -> category.getModuleComponents()
                        .stream()
                        .anyMatch(module -> module.settingComponents
                                .stream()
                                .anyMatch(settingComponent -> settingComponent instanceof StringSettingComponent component && component.textBox.selected)
                        )
                );

        if (!Minecraft.IS_RUNNING_ON_MAC && noneMatch) {
            KeyBinding.updateKeyBindState();
        }
        boolean alphaCheck = alpha.isFinished() && alpha.getValue() == 1.0D;
        boolean scaleCheck = scale.isFinished() && scale.getValue() == 1.0D;
        if (alphaCheck && scaleCheck && mc.mouseHelper.isMouseGrabbed()) {
            mc.mouseHelper.ungrabMouse();
        }
    }

    private void closeCheck() {
        boolean noneMatch = panel.getCategoryComponents().stream().noneMatch(category -> category.getModuleComponents().stream().anyMatch(ModuleComponent::isBinding));

        if (exit && scale.isFinished() && alpha.isFinished() && noneMatch) {
            closeScreen();
            exit = false;
        }
    }


    public boolean isSearching() {
        return !searchField.isEmpty();
    }

    public String getSearchText() {
        return searchField.getText();
    }

    public boolean searchCheck(String text) {
        return isSearching() && !text
                .replaceAll(" ", "")
                .trim()
                .toLowerCase()
                .contains(getSearchText()
                        .replaceAll(" ", "")
                        .trim()
                        .toLowerCase());
    }
}
