package org.obsidian.client.utils.render.gif;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.platform.GlStateManager;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.texture.TextureUtil;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_QUADS;

public class GifRender implements IMinecraft {
    private final List<Integer> frames = new ArrayList<>();

    public GifRender(ResourceLocation resourceLocation) {
        try (InputStream inputStream = mc.getResourceManager().getResource(resourceLocation).getInputStream()) {
            GifImage gifImage = new GifImage();
            gifImage.loadFrom(inputStream);

            for (BufferedImage frame : gifImage.getFrames()) {
                frames.add(TextureUtil.loadTexture(frame));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load GIF from resource: " + resourceLocation, e);
        }
    }

    public void draw(MatrixStack stack, float x, float y, float width, float height, float alpha, double speed, boolean bloom) {
        bindTexture(speed);

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        if (bloom) GlStateManager.blendFunc(770, 1);
        GlStateManager.disableAlphaTest();

        int color = ColorUtil.replAlpha(-1, (int) (alpha * 255));
        BufferBuilder buffer = Tessellator.getInstance().getBuffer();
        buffer.begin(GL_QUADS, DefaultVertexFormats.POSITION_COLOR_TEX);
        buffer.pos(stack.getLast().getMatrix(), x, y, 0)
                .color(color)
                .tex(0, 0)
                .endVertex();
        buffer.pos(stack.getLast().getMatrix(), x, y + height, 0)
                .color(color)
                .tex(0, 1)
                .endVertex();
        buffer.pos(stack.getLast().getMatrix(), x + width, y + height, 0)
                .color(color)
                .tex(1, 1)
                .endVertex();
        buffer.pos(stack.getLast().getMatrix(), x + width, y, 0)
                .color(color)
                .tex(1, 0)
                .endVertex();
        Tessellator.getInstance().draw();

        GlStateManager.enableAlphaTest();
        GlStateManager.popMatrix();
    }

    public void bindTexture(double speed) {
        int frameIndex = (int) (System.currentTimeMillis() / speed % frames.size());
        int textureID = frames.get(frameIndex);
        GlStateManager.bindTexture(textureID);
    }
}