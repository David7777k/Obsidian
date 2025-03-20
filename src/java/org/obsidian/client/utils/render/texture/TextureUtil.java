package org.obsidian.client.utils.render.texture;

import lombok.experimental.UtilityClass;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.mojang.blaze3d.platform.GlStateManager;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

@UtilityClass
public class TextureUtil {
    public int loadTexture(BufferedImage image) {
        int[] pixels = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());
        ByteBuffer buffer = ByteBuffer.allocateDirect(pixels.length * 4);

        for (int pixel : pixels) {
            buffer.put((byte) ((pixel >> 16) & 0xFF));
            buffer.put((byte) ((pixel >> 8) & 0xFF));
            buffer.put((byte) (pixel & 0xFF));
            buffer.put((byte) ((pixel >> 24) & 0xFF));
        }
        buffer.flip();

        int textureID = GlStateManager.genTexture();
        GlStateManager.bindTexture(textureID);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, image.getWidth(), image.getHeight(), 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        GlStateManager.bindTexture(0);

        return textureID;
    }

    public BufferedImage toBufferedImage(DynamicTexture texture, int width, int height) {
        return toBufferedImage(texture.getTextureData(), width, height);
    }

    public BufferedImage toBufferedImage(NativeImage texture, int width, int height) {
        BufferedImage img = new BufferedImage(width, height, 2);
        img.setRGB(0, 0, width, height, texture.makePixelArray(), 0, Math.max(width, height));
        return img;
    }

    public BufferedImage loadImage(int textureID, int width, int height) {
        glBindTexture(GL_TEXTURE_2D, textureID);
        ByteBuffer buffer = ByteBuffer.allocateDirect(width * height * 4);
        glGetTexImage(GL_TEXTURE_2D, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        buffer.flip();

        int[] pixels = new int[width * height];
        for (int i = 0; i < pixels.length; i++) {
            int r = buffer.get() & 0xFF;
            int g = buffer.get() & 0xFF;
            int b = buffer.get() & 0xFF;
            int a = buffer.get() & 0xFF;
            pixels[i] = (a << 24) | (r << 16) | (g << 8) | b;
        }

        BufferedImage image = new BufferedImage(width, height, 2);
        image.setRGB(0, 0, width, height, pixels, 0, width);

        return image;
    }

}