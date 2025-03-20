package org.obsidian.client.utils.render.draw;

import lombok.experimental.UtilityClass;
import net.minecraft.util.ResourceLocation;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.api.interfaces.IRender;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.framebuffer.CustomFramebuffer;
import org.obsidian.client.utils.render.shader.ShaderManager;

@UtilityClass
public class RenderUtil implements IRender, IMinecraft {
    public void drawShaderBackground(MatrixStack matrixStack, float alpha) {
        RectUtil.drawRect(matrixStack, 0, 0, mw.getScaledWidth(), mw.getScaledHeight(), ColorUtil.multAlpha(ColorUtil.multDark(Theme.getInstance().clientColor(), 0.025F), alpha));

        final ShaderManager shader = ShaderManager.mainmenuShader;
        shader.load();

        shader.setUniformf("time", (System.currentTimeMillis() - Obsidian.inst().loadTime()) / (100F * Theme.getInstance().getSpeed()));
        shader.setUniformf("alpha", alpha);
        shader.setUniformf("color", ColorUtil.getRGBAf(Theme.getInstance().clientColor()));
        shader.setUniformf("resolution", mw.getFramebufferWidth(), mw.getFramebufferHeight());

        RenderUtil.start();
        CustomFramebuffer.drawQuads(matrixStack, 0, 0, mw.getScaledWidth(), mw.getScaledHeight());
        RenderUtil.stop();
        shader.unload();
    }

    public void start() {
        RenderSystem.clearCurrentColor();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.disableAlphaTest();
        RenderSystem.disableDepthTest();
        RenderSystem.shadeModel(7425);
        defaultAlphaFunc();
    }

    public void stop() {
        RenderSystem.enableDepthTest();
        RenderSystem.enableAlphaTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
        RenderSystem.clearCurrentColor();
        RenderSystem.shadeModel(7424);
    }

    public void bindTexture(int texture) {
        RenderSystem.bindTexture(texture);
    }

    public void bindTexture(ResourceLocation texture) {
        mc.getTextureManager().bindTexture(texture);
    }

    public void resetColor() {
        RenderSystem.clearCurrentColor();
    }

    public void setColor(final int color, final float alpha) {
        final float red = (float) (color >> 16 & 255) / 255.0F;
        final float green = (float) (color >> 8 & 255) / 255.0F;
        final float blue = (float) (color & 255) / 255.0F;
        RenderSystem.color4f(red, green, blue, alpha);
    }

    public void setColor(int color) {
        setColor(color, (float) (color >> 24 & 255) / 255.0F);
    }

    public void defaultAlphaFunc() {
        RenderSystem.alphaFunc(GL11.GL_GREATER, 0);
    }

    public void drawImage(ResourceLocation location, MatrixStack matrix, float x, float y, float width, float height, int color) {
        drawImage(location, matrix, x, y, width, height, color, color, color, color);
    }

    public void drawImage(ResourceLocation location, MatrixStack matrix, float x, float y, float width, float height, int color1, int color2, int color3, int color4) {
        RenderSystem.pushMatrix();
        RenderSystem.clearCurrentColor();
        RenderSystem.disableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(7425);
        bindTexture(location);
        RectUtil.drawRect(matrix, x, y, width, height, color1, color2, color3, color4, false, true);
        RenderSystem.shadeModel(7424);
        RenderSystem.disableBlend();
        RenderSystem.enableAlphaTest();
        RenderSystem.clearCurrentColor();
        RenderSystem.popMatrix();
    }

    public void drawTriangle(float x, float y, float width, float height, int color) {
        GL11.glPushMatrix();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        enableSmoothLine();
        GL11.glRotatef(180 + 90, 0F, 0F, 1.0F);

        // fill.
        GL11.glBegin(9);
        RenderUtil.setColor(color);
        GL11.glVertex2f(x, y - 1);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x, y - 1);
        GL11.glEnd();

        GL11.glBegin(9);
        RenderUtil.setColor(color);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width * 2, y - 1);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();

        // line.
        GL11.glBegin(3);
        RenderUtil.setColor(color);
        GL11.glVertex2f(x, y - 1);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x, y - 1);
        GL11.glEnd();

        GL11.glBegin(3);
        RenderUtil.setColor(color);
        GL11.glVertex2f(x + width, y);
        GL11.glVertex2f(x + width, y + height);
        GL11.glVertex2f(x + width * 2, y - 1);
        GL11.glVertex2f(x + width, y);
        GL11.glEnd();

        disableSmoothLine();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glRotatef(-180 - 90, 0F, 0F, 1.0F);
        GL11.glPopMatrix();
    }

    public void enableSmoothLine() {
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
        GL11.glLineWidth(1F);
    }

    public void disableSmoothLine() {
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glDepthMask(true);
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
        GL11.glHint(GL11.GL_LINE_SMOOTH_HINT, GL11.GL_NICEST);
    }

    @UtilityClass
    public class Rounded {
        public void customRound(MatrixStack matrix, float x, float y, float width, float height, boolean shadow, float shadowAlpha, float offset, float value, float smoothness1, float smoothness2, int color1, int color2, int color3, int color4, int outlineColor, Round round) {
            if (width <= 0 || height <= 0) {
                return;
            }
            final ShaderManager shader = ShaderManager.roundedShader;
            shader.load();

            shader.setUniformf("size", width, height);
            shader.setUniformf("round", round.RT, round.RB, round.LT, round.LB);
            shader.setUniformf("smoothness", smoothness1, smoothness2);
            shader.setUniformf("value", value);
            shader.setUniformi("shadow", shadow ? 1 : 0);
            shader.setUniformf("shadowAlpha", shadow ? shadowAlpha : 0);
            shader.setUniformf("color1", ColorUtil.getRGBAf(color1));
            shader.setUniformf("color2", ColorUtil.getRGBAf(color2));
            shader.setUniformf("color3", ColorUtil.getRGBAf(color3));
            shader.setUniformf("color4", ColorUtil.getRGBAf(color4));

            start();
            resetColor();
            RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            CustomFramebuffer.drawQuads(matrix, x - offset, y - offset, width + (offset * 2), height + (offset * 2));
            stop();
            shader.unload();
        }

        public void roundedOutline(MatrixStack matrix, float x, float y, float width, float height, float outline, int color1, int color2, int color3, int color4, Round round) {
            if (width <= 0 || height <= 0) {
                return;
            }
            final ShaderManager shader = ShaderManager.roundedOutlineShader;
            shader.load();

            shader.setUniformf("size", width, height);
            shader.setUniformf("round", round.RT, round.RB, round.LT, round.LB);
            shader.setUniformf("smoothness", -outline, outline);
            shader.setUniformf("softness", -outline, outline);
            shader.setUniformf("thickness", 0, outline);
            shader.setUniformf("value", outline);
            shader.setUniformf("color1", ColorUtil.getRGBAf(color1));
            shader.setUniformf("color2", ColorUtil.getRGBAf(color2));
            shader.setUniformf("color3", ColorUtil.getRGBAf(color3));
            shader.setUniformf("color4", ColorUtil.getRGBAf(color4));

            start();
            resetColor();
            CustomFramebuffer.drawQuads(matrix, x - outline, y - outline, width + (outline * 2), height + (outline * 2));
            stop();
            shader.unload();
        }

        public void roundedOutline(MatrixStack matrix, float x, float y, float width, float height, float outline, int color, Round round) {
            roundedOutline(matrix, x, y, width, height, outline, color, color, color, color, round);
        }

        public void smooth(MatrixStack matrix, float x, float y, float width, float height, int color1, int color2, int color3, int color4, Round round) {
            customRound(matrix, x, y, width, height, false, 0F, 0F, 0F, -0.5F, 0.5F, color1, color2, color3, color4, 0, round);
        }

        public void smooth(MatrixStack matrix, float x, float y, float width, float height, int color, Round round) {
            customRound(matrix, x, y, width, height, false, 0F, 0F, 0F, -0.5F, 0.5F, color, color, color, color, 0, round);
        }

        public void roundedRect(MatrixStack matrix, float x, float y, float width, float height, int color1, int color2, int color3, int color4, Round round) {
            customRound(matrix, x, y, width, height, false, 0F, 0F, 0F, 0, 1, color1, color2, color3, color4, 0, round);
        }

        public void roundedRect(MatrixStack matrix, float x, float y, float width, float height, int color, Round round) {
            customRound(matrix, x, y, width, height, false, 0F, 0F, 0F, 0, 1, color, color, color, color, 0, round);
        }
    }

    @UtilityClass
    public class Shadow {
        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, int color) {
            drawShadow(matrix, x, y, width, height, radius, 1f, color, color, color, color);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, int color1, int color2, int color3, int color4) {
            Rounded.customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, 1f, 0, radius, -radius, radius, color1, color2, color3, color4, 0, Round.of(radius));
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, int color1, int color2, int color3, int color4, Round round) {
            Rounded.customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, 1f, 0, radius, -radius, radius, color1, color2, color3, color4, 0, round);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, int color, Round round) {
            drawShadow(matrix, x, y, width, height, radius, 1f, color, color, color, color, round);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha, int color) {
            drawShadow(matrix, x, y, width, height, radius, alpha, color, color, color, color);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha, int color1, int color2, int color3, int color4) {
            Rounded.customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, alpha, 0, radius, -radius, radius, color1, color2, color3, color4, 0, Round.of(radius));
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha, int color1, int color2, int color3, int color4, Round round) {
            Rounded.customRound(matrix, x - radius, y - radius, width + (radius * 2F), height + (radius * 2F), true, alpha, 0, radius, -radius, radius, color1, color2, color3, color4, 0, round);
        }

        public void drawShadow(MatrixStack matrix, float x, float y, float width, float height, float radius, float alpha, int color, Round round) {
            drawShadow(matrix, x, y, width, height, radius, alpha, color, color, color, color, round);
        }
    }

    @UtilityClass
    public class Texture {
        public void customRound(MatrixStack matrix, float x, float y, float width, float height, float value, float offset, float alpha, float smoothness1, float smoothness2, Round round) {
            final ShaderManager shader = ShaderManager.roundedTextureShader;
            shader.load();

            shader.setUniformi("textureIn", 0);
            shader.setUniformf("size", width, height);
            shader.setUniformf("round", round.LB, round.LT, round.RB, round.RT);
            shader.setUniformf("smoothness", smoothness1, smoothness2);
            shader.setUniformf("value", value);
            shader.setUniformf("alpha", alpha);

            start();
            resetColor();
            CustomFramebuffer.drawQuads(matrix, x - offset, y - offset, width + (offset * 2), height + (offset * 2));
            stop();
            shader.unload();
        }

        public void smooth(MatrixStack matrix, float x, float y, float width, float height, float alpha, Round round) {
            customRound(matrix, x, y, width, height, 0, 0, alpha, -0.5F, 0.5F, round);
        }

    }
}
