package org.obsidian.client.ui.dropdown.components.settings;

import com.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.ui.dropdown.impl.Component;
import org.obsidian.client.ui.dropdown.utils.MathUtil;
import org.obsidian.client.ui.dropdown.utils.ColorUtils;
import org.obsidian.client.ui.dropdown.utils.Cursors;
import org.obsidian.client.ui.dropdown.utils.DisplayUtils;
import org.obsidian.client.utils.render.font.Fonts;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.glfw.GLFW;

public class SliderComponent extends Component {

    private final SliderSetting setting;

    public SliderComponent(SliderSetting setting) {
        this.setting = setting;
        this.setHeight(18);
    }

    private float anim;
    private boolean drag;
    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        super.render(stack, mouseX, mouseY);
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 4.5f / 2f + 1,
                ColorUtils.rgb(255, 255, 255), 5.5f, 0.05f);
        Fonts.montserrat.drawText(stack, String.valueOf(setting.getValue()), getX() + getWidth() - 5 - Fonts.montserrat.getWidth(String.valueOf(setting.getValue()), 5.5f), getY() + 4.5f / 2f + 1,
                ColorUtils.rgb(255, 255, 255), 5.5f, 0.05f);

        DisplayUtils.drawRoundedRect(getX() + 5, getY() + 11, getWidth() - 10, 2, 0.6f, ColorUtils.rgb(28, 28, 31));
        anim = MathUtil.fast(anim, (getWidth() - 10) * (setting.getValue() - setting.getMin()) / (setting.getMax() - setting.getMin()), 20);
        float sliderWidth = anim;
        DisplayUtils.drawRoundedRect(getX() + 5, getY() + 11, sliderWidth, 2, 0.6f, ColorUtils.rgb(25, 26, 50));
        DisplayUtils.drawCircle(getX() + 5 + sliderWidth, getY() + 12, 5, ColorUtils.rgb(129, 135, 255));
        DisplayUtils.drawShadowCircle(getX() + 5 + sliderWidth, getY() + 12, 6, ColorUtils.rgba(1129, 135, 255, 80));
        if (drag) {
            GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), GLFW.glfwCreateStandardCursor(GLFW.GLFW_HRESIZE_CURSOR));
            setting.setValue((float) MathHelper.clamp(MathUtil.round((mouseX - getX() - 5) / (getWidth() - 10) * (setting.getMax() - setting.getMin()) + setting.getMin(), setting.getIncrement()), setting.getMin(), setting.getMax()));
        }
        if (isHovered(mouseX, mouseY)) {
            if (MathUtil.isHovered(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10, 3)) {
                if (!hovered) {
                    GLFW.glfwSetCursor(Minecraft.getInstance().getMainWindow().getHandle(), Cursors.RESIZEH);
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
        if (MathUtil.isHovered(mouseX, mouseY, getX() + 5, getY() + 10, getWidth() - 10, 3)) {
            drag = true;
        }
        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        drag = false;
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.getVisible().get();
    }

}
