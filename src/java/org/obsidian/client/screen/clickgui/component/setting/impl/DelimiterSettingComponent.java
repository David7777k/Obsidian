package org.obsidian.client.screen.clickgui.component.setting.impl;

import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.settings.impl.DelimiterSetting;
import org.obsidian.client.screen.clickgui.component.setting.SettingComponent;
import org.obsidian.client.utils.render.draw.RectUtil;

public class DelimiterSettingComponent extends SettingComponent {
    private final DelimiterSetting value;

    public DelimiterSettingComponent(DelimiterSetting value) {
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
        float lineWidth = 5;

//        font.draw(matrix, valueName, position.x + lineWidth + margin, position.y + margin, getWhite(), fontSize);

        float valueHeight = drawName(matrix, mouseX, mouseY, size.x - margin - lineWidth - margin);


        float textWidth = font.getWidth(value.getName(), fontSize);
        float line = 0.5f;

        boolean lineCheck = textWidth > (size.x - margin - lineWidth - margin);

        RectUtil.drawRect(matrix, position.x + size.x - margin - (lineCheck ? lineWidth : (size.x - margin - textWidth - margin)), position.y + (margin + fontSize + margin) / 2F - line / 2F, (lineCheck ? lineWidth : (size.x - margin - textWidth - margin)) + margin, line, accentColor());

        size.y = margin + valueHeight + margin;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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
