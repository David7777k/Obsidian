package org.obsidian.client.ui.dropdown.components.settings;


import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.ui.dropdown.impl.Component;
import org.obsidian.client.ui.dropdown.utils.ColorUtils;
import org.obsidian.client.ui.dropdown.utils.Cursors;
import org.obsidian.client.ui.dropdown.utils.DisplayUtils;
import org.obsidian.client.ui.dropdown.utils.MathUtil;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.font.Fonts;

public class BooleanComponent extends Component {

    private final BooleanSetting setting;

    public BooleanComponent(BooleanSetting setting) {
        this.setting = setting;
        setHeight(16);
        animation = animation.run(setting.getValue() ? 1 : 0, 0.2, Easings.CIRC_OUT);
    }

    private Animation animation = new Animation();
    private float width, height;
    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        animation.update();
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 6.5f / 2f + 1, ColorUtils.rgb(255, 255, 255), 6.5f, 0.05f);

        width = 15;
        height = 7;
        DisplayUtils.drawRoundedRect(getX() + getWidth() - width - 7, getY() + getHeight() / 2f - height / 2f, width,
                height, 3f, ColorUtils.rgb(29, 29, 31));
        int color = ColorUtils.interpolate(ColorUtils.rgb(129, 135, 255), ColorUtils.rgb(129, 135, 255), 1 - (float) animation.getValue());
        DisplayUtils.drawCircle((float) (getX() + getWidth() - width - 7 + 4 + (7 * animation.getValue())),
                getY() + getHeight() / 2f - height / 2f + 3.5f,
                5f, color);

        DisplayUtils.drawShadowCircle((float) (getX() + getWidth() - width - 7 + 4 + (7 * animation.getValue())),
                getY() + getHeight() / 2f - height / 2f + 3.5f,
                7f, ColorUtils.setAlpha(color, (int) (128 * animation.getValue())));

        if (isHovered(mouseX, mouseY)) {
            if (MathUtil.isHovered(mouseX, mouseY, getX() + getWidth() - width - 7, getY() + getHeight() / 2f - height / 2f, width,
                    height)) {
                if (!hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.HAND);
                    hovered = true;
                }
            } else {
                if (hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.ARROW);
                    hovered = false;
                }
            }
        }
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (MathUtil.isHovered(mouseX, mouseY, getX() + getWidth() - width - 7, getY() + getHeight() / 2f - height / 2f, width,
                height)) {
            setting.setValue(!setting.getValue());
            animation = animation.run(setting.getValue() ? 1 : 0, 0.2, Easings.CIRC_OUT);
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.getVisible().get();
    }

}
