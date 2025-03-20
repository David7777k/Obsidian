package org.obsidian.client.utils.render.draw;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.minecraft.client.shader.Framebuffer;
import net.mojang.blaze3d.platform.GlStateManager;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL30;

@UtilityClass
public class StencilUtil implements IMinecraft {

    public boolean enabled;

    private void recreate(final Framebuffer framebuffer) {
        GL30.glDeleteRenderbuffers(framebuffer.depthBuffer);
        final int depthBuffer = GL30.glGenRenderbuffers();
        GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL30.GL_DEPTH_STENCIL, mc.getMainWindow().getWidth(), mc.getMainWindow().getHeight());
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_STENCIL_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
        GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthBuffer);
    }

    public void checkSetupFBO(final Framebuffer framebuffer) {
        if (framebuffer != null) {
            if (framebuffer.depthBuffer > -1) {
                recreate(framebuffer);
                framebuffer.depthBuffer = -1;
            }
        }
    }

    public void enable() {
        mc.getFramebuffer().bindFramebuffer(false);
        checkSetupFBO(mc.getFramebuffer());
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);
        GL11.glEnable(GL11.GL_STENCIL_TEST);

        bind();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.enableAlphaTest();
        GlStateManager.alphaFunc(GL11.GL_GREATER, 0.0F);
        enabled = true;
    }

    public void bind() {
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 1);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE);
        GL11.glColorMask(false, false, false, false);
    }

    public void read(final int ref) {
        GL11.glColorMask(true, true, true, true);
        GL11.glStencilFunc(GL11.GL_EQUAL, ref, 1);
        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP);
    }

    public void disable() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
        GlStateManager.disableAlphaTest();
        GlStateManager.disableBlend();
        enabled = false;
    }

    @Getter
    @AllArgsConstructor
    public enum Action {
        INSIDE(0),
        OUTSIDE(1);
        private final int action;
    }

}