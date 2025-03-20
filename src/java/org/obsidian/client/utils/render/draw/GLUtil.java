package org.obsidian.client.utils.render.draw;

import lombok.experimental.UtilityClass;
import net.minecraft.util.math.vector.Quaternion;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.IRenderCall;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;

@UtilityClass
public class GLUtil {

    public void enableDepth() {
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
    }

    public void disableDepth() {
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
    }

    public void startBlend() {
        RenderSystem.enableBlend();
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
        RenderSystem.defaultBlendFunc();
    }

    public void endBlend() {
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
        RenderSystem.disableBlend();
    }


    public void startRotate(float x, float y, float rotate) {
        RenderSystem.pushMatrix();
        RenderSystem.translatef(x, y, 0);
        RenderSystem.rotatef(rotate, 0, 0, -1);
        RenderSystem.translatef(-x, -y, 0);
    }

    public void endRotate() {
        RenderSystem.popMatrix();
    }


    public void rotate(float x, float y, float rotate, IRenderCall render) {
        startRotate(x, y, rotate);
        render.execute();
        endRotate();
    }

    public void rotate(MatrixStack matrix, float x, float y, Quaternion rotation, IRenderCall render) {
        matrix.push();
        matrix.translate(x, y, 0);
        matrix.rotate(rotation);
        matrix.translate(-x, -y, 0);
        render.execute();
        matrix.pop();
    }

    public void startScale(float x, float y, float scale) {
        RenderSystem.pushMatrix();
        RenderSystem.translated(x, y, 0);
        RenderSystem.scaled(scale, scale, 1);
        RenderSystem.translated(-x, -y, 0);
    }

    public void endScale() {
        RenderSystem.popMatrix();
    }

    public void scale(float x, float y, float scale, IRenderCall render) {
        startScale(x, y, scale);
        render.execute();
        endScale();
    }

    public void scale(MatrixStack matrix, float x, float y, float scale, IRenderCall render) {
        matrix.push();
        matrix.translate(x, y, 0);
        matrix.scale(scale, scale, 0);
        matrix.translate(-x, -y, 0);
        render.execute();
        matrix.pop();
    }

}