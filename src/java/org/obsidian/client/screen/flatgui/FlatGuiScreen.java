package org.obsidian.client.screen.flatgui;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.util.text.StringTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IMouse;
import org.obsidian.client.api.interfaces.IWindow;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.screen.flatgui.impl.CategoryWidget;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.keyboard.Keyboard;
import org.obsidian.client.utils.math.ScaleMath;
import org.obsidian.client.utils.render.color.ColorFormatting;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;
import org.obsidian.client.utils.render.font.Fonts;
import org.joml.Vector2f;

@Getter
@Setter
@Accessors(fluent = true)
public class FlatGuiScreen extends Screen implements IMinecraft, IWindow, IMouse {
    private boolean exit = false;
    private final Animation alpha = new Animation();
    private final Animation scale = new Animation();
    private final Vector2f position = new Vector2f(0, 0);
    private final Vector2f size = new Vector2f(540, 320);

    private final CategoryWidget categoryWidget = new CategoryWidget();
    private Category selectedCategory = Category.COMBAT;

    public FlatGuiScreen() {
        super(StringTextComponent.EMPTY);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
        super.resize(minecraft, width, height);
        categoryWidget.resize(minecraft, width, height);
    }

    @Override
    protected void init() {
        super.init();

        alpha.set(0.0);
        scale.set(0.0);
        alpha.run(1.0, 0.25, Easings.SINE_OUT);
        scale.set(0.75F);
        scale.run(1.0, 0.25, Easings.SINE_OUT);
        exit = false;
        categoryWidget.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;
        alpha.update();
        scale.update();
        this.mouseCheck();
        this.closeCheck();

        float panelX = width / 2F - size.x / 2F;
        float panelY = height / 2F - size.y / 2F;

        float scale = scale().get();

        float margin = 5F;
        float iconSize = 24F;
        float fontSize = 16F;

        ScaleMath.scalePre();
        matrixStack.push();
        matrixStack.translate(width / 2F, height / 2F, 0);
        matrixStack.scale(scale, scale, 0);
        matrixStack.translate(-(width / 2F), -(height / 2F), 0);

        RenderUtil.Rounded.smooth(matrixStack, panelX, panelY, size.x, size.y, ColorUtil.getColor(10, 10, 15, alpha.get()), Round.of(8));
        Fonts.CLICKGUI.drawCenter(matrixStack, "g", panelX + margin + iconSize / 2F, panelY + margin + iconSize / 2F - fontSize / 2F, categoryWidget().themeColor(), fontSize);

        Fonts.SF_BOLD.draw(matrixStack, ColorFormatting.getColor(32, 32, 32) + "| " + ColorFormatting.reset() + selectedCategory.getName(), panelX + margin + Fonts.CLICKGUI.getWidth("g", iconSize) + margin * 2F, panelY + margin * 2F, categoryWidget().themeColor(), 8F);

        categoryWidget.position().set(panelX, panelY);
        categoryWidget.size().set(margin + 24F + margin, size.y);
        categoryWidget.render(matrixStack, finalMouseX, finalMouseY, partialTicks);

        matrixStack.pop();
        ScaleMath.scalePost();
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;
        if (!exit) {
            categoryWidget.mouseClicked(finalMouseX, finalMouseY, button);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        Vector2f mouse = ScaleMath.getMouse(mouseX, mouseY);
        int finalMouseX = (int) mouse.x;
        int finalMouseY = (int) mouse.y;
        if (!exit) {
            categoryWidget.mouseReleased(finalMouseX, finalMouseY, button);
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!exit) {
            if (keyCode == Keyboard.KEY_GRAVE.getKey()) {
                return shouldCloseOnEsc();
            }
            categoryWidget.keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (!exit) {
            categoryWidget.keyReleased(keyCode, scanCode, modifiers);
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (!exit) {
            categoryWidget.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }


    @Override
    public boolean shouldCloseOnEsc() {
        boolean noneMatch = true; // TODO
        if (!exit && scale.getValue() > 0.0F && alpha.getValue() > 0.0F) {
            alpha.run(0.0, 0.25, Easings.SINE_IN);
            scale.run(0.0, 0.5, Easings.SINE_IN);
            exit = true;
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
        categoryWidget.onClose();
    }

    private void mouseCheck() {
        boolean noneMatch = true; // TODO

        if (!Minecraft.IS_RUNNING_ON_MAC) {
            KeyBinding.updateKeyBindState();
        }
        boolean alphaCheck = alpha.isFinished() && alpha.getValue() == 1.0D;
        boolean scaleCheck = scale.isFinished() && scale.getValue() == 1.0D;
        if (alphaCheck && scaleCheck && mc.mouseHelper.isMouseGrabbed()) {
            mc.mouseHelper.ungrabMouse();
        }
    }

    private void closeCheck() {
        boolean noneMatch = true; // TODO
        if (exit && scale.isFinished() && alpha.isFinished()) {
            closeScreen();
            exit = false;
        }
    }
}
