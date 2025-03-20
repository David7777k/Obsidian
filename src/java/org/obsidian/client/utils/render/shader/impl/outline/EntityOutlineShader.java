package org.obsidian.client.utils.render.shader.impl.outline;

import net.mojang.blaze3d.systems.RenderSystem;
import net.optifine.render.Blender;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.utils.render.draw.RenderUtil;
import org.obsidian.client.utils.render.framebuffer.CustomFramebuffer;
import org.obsidian.client.utils.render.shader.ShaderManager;
import org.lwjgl.opengl.GL30;

public class EntityOutlineShader implements IMinecraft {

    private static final ShaderManager outline = ShaderManager.entityOutlineShader;
    private static final CustomFramebuffer outFrameBuffer = new CustomFramebuffer(false);


    public static void draw(int radius, int texture) {
        outFrameBuffer.setup();
        outFrameBuffer.bindFramebuffer(true);

        outline.load();
        outline.setUniformf("size", (float) radius);
        outline.setUniformi("textureIn", 0);
        outline.setUniformi("textureToCheck", 20);
        outline.setUniformf("texelSize", 1.0F / (float) (mw.getScaledWidth() * mw.getScaleFactor()), 1.0F / (float) (mw.getScaledHeight() * mw.getScaleFactor()));

        outline.setUniformf("direction", 1.0F, 0.0F);
        int blend = 8;
        RenderSystem.enableBlend();
        Blender.setupBlend(blend, 1f);

        RenderUtil.defaultAlphaFunc();

        RenderUtil.bindTexture(texture);
        CustomFramebuffer.drawQuads();

        mc.getFramebuffer().bindFramebuffer(false);
        Blender.setupBlend(blend, 1f);

        outline.setUniformf("direction", 0.0F, 1.0F);

        outFrameBuffer.bindFramebufferTexture();
        GL30.glActiveTexture(GL30.GL_TEXTURE20);
        RenderUtil.bindTexture(texture);
        GL30.glActiveTexture(GL30.GL_TEXTURE0);
        CustomFramebuffer.drawQuads();

        outline.unload();
        RenderSystem.bindTexture(0);
        RenderSystem.disableBlend();
    }

}
