package org.obsidian.client.ui.dropdown.components.settings;


import net.minecraft.client.Minecraft;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.obsidian.client.managers.module.settings.impl.BindSetting;
import org.obsidian.client.ui.dropdown.impl.Component;
import org.obsidian.client.ui.dropdown.utils.*;
import org.obsidian.client.utils.render.font.Fonts;

public class BindComponent extends Component {

    final BindSetting setting;

    public BindComponent(BindSetting setting) {
        this.setting = setting;
        this.setHeight(16);
    }

    boolean activated;
    boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        Fonts.montserrat.drawText(stack, setting.getName(), getX() + 5, getY() + 6.5f / 2f + 1, ColorUtils.rgb(160, 163, 175), 6.5f, 0.05f);
        String bind = KeyStorage.getKey(setting.getKey());

        if (bind == null || setting.getKey() == -1) {
            bind = "Нету";
        }
        boolean next = Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) >= 16;
        float x = next ? getX() + 5 : getX() + getWidth() - 7 - Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f);
        float y = getY() + 5.5f / 2f + (5.5f / 2f) + (next ? 8 : 0);
        DisplayUtils.drawShadow(getX() + 5, getY() + 9, Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) + 4, 5.5f + 4, 10, ColorUtils.rgba(25, 26, 40, 45));

        DisplayUtils.drawRoundedRect(getX() + 5, getY() + 9, Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) + 4, 5.5f + 4, 2, ColorUtils.rgba(25, 26, 40, 45));
        Fonts.montserrat.drawText(stack, bind, x, y, activated ? -1 : ColorUtils.rgb(255, 255, 255), 5.5f, activated ? 0.1f : 0.05f);

        if (isHovered(mouseX, mouseY)) {
            if (MathUtil.isHovered(mouseX, mouseY, x - 2 + 0.5F, y - 2, Fonts.montserrat.getWidth(bind, 5.5f, activated ? 0.1f : 0.05f) + 4, 5.5f + 4)) {
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
        setHeight(next ? 20 : 16);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        if (activated) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                setting.setKey(-1);
                activated = false;
                return;
            }
            setting.setKey(key);
            activated = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }


    @Override
    public void mouseClick(float mouseX, float mouseY, int mouse) {
        if (isHovered(mouseX, mouseY) && mouse == 0) {
            activated = !activated;
        }

        if (activated && mouse >= 1) {
            setting.setKey(-100 + mouse);
            activated = false;
        }

        super.mouseClick(mouseX, mouseY, mouse);
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    @Override
    public boolean isVisible() {
        return setting.getVisible().get();
    }
}
