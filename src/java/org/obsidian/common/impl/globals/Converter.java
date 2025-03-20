package org.obsidian.common.impl.globals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;

public class Converter {
    private final static HashMap<String, ResourceLocation> images = new HashMap<>();

    public static ResourceLocation getResourceLocation(String link) {
        if (images.containsKey(link))
            return images.get(link);
        else {
            NativeImage nativeImage = null;

            try {
                URL url = new URL(link);

                try (InputStream inputStream = url.openStream();
                     ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                    byte[] buffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        outputStream.write(buffer, 0, bytesRead);
                    }

                    byte[] imageData = outputStream.toByteArray();
                    nativeImage = NativeImage.read(new ByteArrayInputStream(imageData));
                } catch (IOException e) {
                }
            } catch (IOException e) {
            }

            TextureManager textureManager = Minecraft.getInstance().getTextureManager();
            DynamicTexture dynamicTexture = new DynamicTexture(nativeImage);
            ResourceLocation res = textureManager.getDynamicTextureLocation("custom", dynamicTexture);
            images.put(link, res);
            return res;
        }
    }
}