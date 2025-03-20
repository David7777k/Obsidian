package org.obsidian.client.screen.clickgui.component.setting.impl;

import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.screen.clickgui.component.setting.SettingComponent;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.draw.Round;

public class BooleanSettingComponent extends SettingComponent {
    private final BooleanSetting value;

    public BooleanSettingComponent(BooleanSetting value) {
        super(value);
        this.value = value;
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {

    }

    @Override
    public void init() {

    }

    @Override
    public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
        super.render(matrix, mouseX, mouseY, partialTicks);
        value.getAnimation().update();

        float checkboxWidth = 14;
        float checkboxSize = 6;

        float valueHeight = drawName(matrix, mouseX, mouseY, size.x - checkboxWidth - margin);

        value.getAnimation().run(value.getValue() ? 1 : 0, 0.25, Easings.BACK_OUT, true);
        int backColorDark = ColorUtil.multDark(accentColor(), 0.25F);
        int backColor = ColorUtil.overCol(backColorDark, accentColor(), value.getAnimation().get());
        RenderUtil.Rounded.smooth(matrix, position.x + size.x - checkboxWidth, position.y + margin, checkboxWidth, checkboxSize, backColor, backColor, backColorDark, backColorDark, Round.of(checkboxSize / 2F));
        RenderUtil.Rounded.smooth(matrix, position.x + size.x - checkboxWidth + (value.getAnimation().get() * (checkboxWidth - checkboxSize)) + 0.5F, position.y + margin + 0.5F, checkboxSize - 1, checkboxSize - 1, getWhite(), Round.of(checkboxSize / 2F - 0.5F));

        size.y = margin + valueHeight + margin;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float checkboxWidth = 14;
        float checkboxSize = 6;

        if (isLClick(button) && isHover(mouseX, mouseY, position.x + size.x - checkboxWidth, position.y + margin, checkboxWidth, checkboxSize)) {
            value.set(!value.getValue());
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return false;
    }

    @Override
    public void onClose() {

    }
}