package org.obsidian.client.ui.dropdown.components.settings;


import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.ui.dropdown.impl.Component;
import org.obsidian.client.ui.dropdown.utils.ColorUtils;
import org.obsidian.client.ui.dropdown.utils.Cursors;
import org.obsidian.client.ui.dropdown.utils.DisplayUtils;
import org.obsidian.client.ui.dropdown.utils.MathUtil;
import org.obsidian.client.utils.render.font.Fonts;

public class MultiBoxComponent extends Component {

    final MultiBooleanSetting setting;

    float width = 0;
    float heightPadding = 0;
    float spacing = 2;

    public MultiBoxComponent(MultiBooleanSetting setting) {
        this.setting = setting;
        setHeight(22);
    }

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 2, ColorUtils.rgb(255, 255, 255), 5.5f, 0.05f);
        DisplayUtils.drawShadow(getX() + 5, getY() + 9, width + 5, 10 + heightPadding, 10, ColorUtils.rgba(25, 26, 40, 45));

        DisplayUtils.drawRoundedRect(getX() + 5, getY() + 9, width + 5, 10 + heightPadding, 2, ColorUtils.rgba(25, 26, 40, 45));

        float offset = 0;
        float heightoff = 0;
        boolean plused = false;
        boolean anyHovered = false;
        for (BooleanSetting text : setting.getValues()) {
            float textWidth = Fonts.montserrat.getWidth(text.getName(), 5.5f, 0.05f) + 2;
            float textHeight = Fonts.montserrat.getHeight(5.5f) + 1;
            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing;
                plused = true;
            }
            if (MathUtil.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 11.5f + heightoff, textWidth, textHeight)) {
                anyHovered = true;
            }
            if (text.getValue()) {
                Fonts.montserrat.drawText(stack, text.getName(), getX() + 8 + offset, getY() + 11.5f + heightoff,
                        ColorUtils.rgba(255, 255, 255, 255), 5.5f, 0.07f);
                DisplayUtils.drawRoundedRect(getX() + 7 + offset, getY() + 10.5f + heightoff, textWidth + 0.8f, textHeight + 0.8f, 1.5f, ColorUtils.rgba(129, 135, 255, 165));
            } else {
                Fonts.montserrat.drawText(stack, text.getName(), getX() + 8 + offset, getY() + 11.5f + heightoff,
                        ColorUtils.rgba(255, 255, 255, 255), 5.5f, 0.05f);
                DisplayUtils.drawRoundedRect(getX() + 7 + offset, getY() + 10.5f + heightoff, textWidth + 0.8f, textHeight + 0.8f, 1.5f, ColorUtils.rgba(25, 26, 40, 165));

            }
            offset += textWidth + spacing;
        }
        if (isHovered(mouseX, mouseY)) {
            if (anyHovered) {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
            } else {
                GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
            }
        }
        width = plused ? getWidth() - 15 : offset;
        setHeight(22 + heightoff);
        heightPadding = heightoff;
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {

        float offset = 0;
        float heightoff = 0;
        for (BooleanSetting text : setting.getValues()) {
            float textWidth = Fonts.montserrat.getWidth(text.getName(), 5.5f, 0.05f) + 2;
            float textHeight = Fonts.montserrat.getHeight(5.5f) + 1;
            if (offset + textWidth + spacing >= (getWidth() - 10)) {
                offset = 0;
                heightoff += textHeight + spacing;
            }
            if (MathUtil.isHovered(mouseX, mouseY, getX() + 8 + offset, getY() + 11.5f + heightoff, textWidth, textHeight)) {
                text.setValue(!text.getValue());
            }
            offset += textWidth + spacing;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.getVisible().get();
    }

}
