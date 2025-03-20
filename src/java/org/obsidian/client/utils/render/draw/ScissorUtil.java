package org.obsidian.client.utils.render.draw;

import lombok.experimental.UtilityClass;
import net.minecraft.client.MainWindow;
import org.lwjgl.opengl.GL11;

@UtilityClass
public class ScissorUtil {
    public void enable() {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
    }

    public void disable() {
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void scissor(MainWindow window, double x, double y, double width, double height) {
        if (x + width == x || y + height == y || x < 0 || y + height < 0) return;
        final double scaleFactor = window.getScaleFactor();
        GL11.glScissor((int) Math.round(x * scaleFactor), (int) Math.round((window.getScaledHeight() - (y + height)) * scaleFactor), Math.max(1, (int) Math.round(width * scaleFactor)), Math.max(1, (int) Math.round(height * scaleFactor)));
    }
}