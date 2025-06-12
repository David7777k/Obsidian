package org.obsidian.client.ui.dropdown.components;


import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.vector.Vector4f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.lwjgl.glfw.GLFW;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.settings.Setting;
import org.obsidian.client.managers.module.settings.impl.*;
import org.obsidian.client.ui.dropdown.components.settings.*;
import org.obsidian.client.ui.dropdown.impl.Component;
import org.obsidian.client.ui.dropdown.utils.*;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.render.font.Fonts;

@Getter
public class ModuleComponent extends Component {
    private final Vector4f ROUNDING_VECTOR = new Vector4f(5, 5, 5, 5);

    private final Module function;
    public Animation animation = new Animation();
    public boolean open;
    private boolean bind;

    private final ObjectArrayList<Component> components = new ObjectArrayList<>();

    public ModuleComponent(Module function) {
        this.function = function;
        for (Setting<?> setting : function.getSettings()) {
            if (setting instanceof BooleanSetting bool) {
                components.add(new BooleanComponent(bool));
            }
            if (setting instanceof SliderSetting slider) {
                components.add(new SliderComponent(slider));
            }
            if (setting instanceof BindSetting bind) {
                components.add(new BindComponent(bind));
            }
            if (setting instanceof ModeSetting mode) {
                components.add(new ModeComponent(mode));
            }
            if (setting instanceof MultiBooleanSetting mode) {
                components.add(new MultiBoxComponent(mode));
            }
            if (setting instanceof StringSetting string) {
                components.add(new StringComponent(string));
            }

        }
        animation.run(open ? 1 : 0, 0.3);
    }

    public void drawComponents(MatrixStack stack, float mouseX, float mouseY) {
        if (animation.getValue() > 0) {
            if (animation.getValue() > 0.1 && components.stream().filter(Component::isVisible).count() >= 1) {
                DisplayUtils.drawRectVerticalW(getX() + 5, getY() + 20, getWidth() - 10, 0.5f, ColorUtils.rgb(42, 44, 50), ColorUtils.rgb(28, 28, 33));
            }
            Stencil.initStencilToWrite();
            DisplayUtils.drawRoundedRect(getX() + 0.5f, getY() + 0.5f, getWidth() - 1, getHeight() - 1, ROUNDING_VECTOR, ColorUtils.rgba(23, 23, 23, (int) (255 * 0.33)));
            Stencil.readStencilBuffer(1);
            float y = getY() + 20;
            for (Component component : components) {
                if (component.isVisible()) {
                    component.setX(getX());
                    component.setY(y);
                    component.setWidth(getWidth());
                    component.render(stack, mouseX, mouseY);
                    y += component.getHeight();
                }
            }
            Stencil.uninitStencilBuffer();

        }
    }

    @Override
    public void mouseRelease(float mouseX, float mouseY, int mouse) {
        for (Component component : components) {
            component.mouseRelease(mouseX, mouseY, mouse);
        }
        super.mouseRelease(mouseX, mouseY, mouse);
    }

    private boolean hovered = false;

    @Override
    public void render(MatrixStack stack, float mouseX, float mouseY) {
        int color = ColorUtils.interpolate(-1, ColorUtils.rgb(161, 164, 177), (float) animation.getValue());

        animation.update();

        drawOutlinedRect(mouseX, mouseY, color);

        drawText(stack, color);
        drawComponents(stack, mouseX, mouseY);
    }

    @Override
    public void mouseClick(float mouseX, float mouseY, int button) {
        if (isHovered(mouseX, mouseY, 20)) {
            if (button == 0) function.toggle();
            if (button == 1) {
                open = !open;
                animation.run(open ? 1 : 0, 0.2, Easings.CIRC_OUT);
            }
            if (button == 2) {
                bind = !bind;
            }
        }
        if (isHovered(mouseX, mouseY)) {
            if (open) {
                for (Component component : components) {
                    if (component.isVisible()) component.mouseClick(mouseX, mouseY, button);
                }
            }
        }
        super.mouseClick(mouseX, mouseY, button);
    }

    @Override
    public void charTyped(char codePoint, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.charTyped(codePoint, modifiers);
        }
        super.charTyped(codePoint, modifiers);
    }

    @Override
    public void keyPressed(int key, int scanCode, int modifiers) {
        for (Component component : components) {
            if (component.isVisible()) component.keyPressed(key, scanCode, modifiers);
        }
        if (bind) {
            if (key == GLFW.GLFW_KEY_DELETE) {
                function.setKey(0);
            } else function.setKey(key);
            bind = false;
        }
        super.keyPressed(key, scanCode, modifiers);
    }

    private void drawOutlinedRect(float mouseX, float mouseY, int color) {
        Stencil.initStencilToWrite();
        DisplayUtils.drawRoundedRect(getX() + 2.5f, getY() + 0.5f, getWidth() - 5, getHeight() - 1, ROUNDING_VECTOR, ColorUtils.rgba(25, 26, 40, (int) (255 * 0.33)));

        Stencil.readStencilBuffer(0);
        DisplayUtils.drawRoundedRect(getX() + 2, getY(), getWidth() - 5, getHeight(), ROUNDING_VECTOR, ColorUtils.rgba(25, 26, 40, 165));
        Stencil.uninitStencilBuffer();
        DisplayUtils.drawRoundedRect(getX() + 2, getY(), getWidth() - 5, getHeight() - 1, ROUNDING_VECTOR, new Vector4i(ColorUtils.rgba(25, 26, 40, (int) (255 * 0.33)), ColorUtils.rgba(25, 26, 40, (int) (255 * 0.33)), ColorUtils.rgba(25, 26, 40, (int) (255 * 0.33)), ColorUtils.rgba(25, 26, 40, (int) (255 * 0.33))));
        DisplayUtils.drawRoundedRect(getX() + 2, getY(), getWidth() - 5, getHeight() - 1, ROUNDING_VECTOR, ColorUtils.rgba(25, 26, 40, (int) (255 * 0.33)));

        if (MathUtil.isHovered(mouseX, mouseY, getX(), getY(), getWidth(), 20)) {
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

    private void drawText(MatrixStack stack, int color) {

        Fonts.montserrat.drawText(stack, function.getName(), getX() + 6, getY() + 6.5f, color, 7, 0.1f);
        if (components.stream().filter(Component::isVisible).count() >= 1) {
            if (bind) {
                Fonts.montserrat.drawText(stack, function.getKey() == 0 ? "..." : String.valueOf(function.getKey()), getX() + getWidth() - 6 - Fonts.montserrat.getWidth(function.getKey() == 0 ? "..." : String.valueOf(function.getKey()), 6, 0.1f), getY() + Fonts.montserrat.getHeight(6) + 1, ColorUtils.rgb(161, 164, 177), 6, 0.1f);
            } else
                Fonts.montserrat.drawText(stack, !open ? "B" : "C", getX() + getWidth() - 6 - Fonts.montserrat.getWidth(!open ? "B" : "C", 6), getY() + Fonts.montserrat.getHeight(6) + 1, ColorUtils.rgb(161, 164, 177), 6);
        } else {
            if (bind) {
                Fonts.montserrat.drawText(stack, function.getKey() == 0 ? "..." : String.valueOf(function.getKey()), getX() + getWidth() - 6 - Fonts.montserrat.getWidth(function.getKey() == 0 ? "..." : String.valueOf(function.getKey()), 6, 0.1f), getY() + Fonts.montserrat.getHeight(6) + 1, ColorUtils.rgb(161, 164, 177), 6, 0.1f);
            }
        }
    }
}
