package org.obsidian.client.utils.math;

import lombok.experimental.UtilityClass;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.joml.Vector2f;

@UtilityClass
public class ScaleMath implements IMinecraft {
    private final int SCALE = 2;

    public void scalePre() {
        mc.gameRenderer.setupOverlayRendering(SCALE);
    }

    public Vector2f getMouse(double mouseX, double mouseY) {
        return new Vector2f((float) (mouseX * mc.getMainWindow().getScaleFactor() / SCALE), (float) (mouseY * mc.getMainWindow().getScaleFactor() / SCALE));
    }

    public void scalePre(float scale) {
        mc.gameRenderer.setupOverlayRendering(scale);
    }

    public float getScaled(double value) {
        return (float) (value * mc.getMainWindow().getScaleFactor() / SCALE);
    }

    public Vector2f getMouse(double mouseX, double mouseY, float scale) {
        return new Vector2f((float) (mouseX * mc.getMainWindow().getScaleFactor() / scale), (float) (mouseY * mc.getMainWindow().getScaleFactor() / scale));
    }

    public float getScaled(double value, float scale) {
        return (float) (value * mc.getMainWindow().getScaleFactor() / scale);
    }

    public void scalePost() {
        mc.gameRenderer.setupOverlayRendering();
    }
}
