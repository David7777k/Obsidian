package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.entity.model.IHasHead;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import net.optifine.util.MathUtils;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.interfaces.IRender;
import org.obsidian.client.managers.events.render.RenderHeadLayerEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.lwjgl.opengl.GL11;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ChinaHat", category = Category.RENDER)
public class ChinaHat extends Module implements IRender {
    public static ChinaHat getInstance() {
        return Instance.get(ChinaHat.class);
    }

    @EventHandler
    public void onEvent(RenderHeadLayerEvent event) {
        if (event.getModel() instanceof IHasHead head && event.getEntity() instanceof PlayerEntity player && ((player instanceof ClientPlayerEntity) || Obsidian.inst().friendManager().isFriend(player.getGameProfile().getName()))) {
            final MatrixStack matrix = event.getMatrix();
            float width = player.getWidth();
            final boolean light = GL11.glIsEnabled(GL11.GL_LIGHTING);
            RenderSystem.pushMatrix();
            if (light) {
                RenderSystem.disableLighting();
            }
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.disableTexture();
            RenderSystem.enableDepthTest();
            RenderSystem.disableCull();
            RenderSystem.shadeModel(GL11.GL_SMOOTH);
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            RenderSystem.lineWidth(1F);
            int multiplier = 2;
            matrix.push();
            float offset = player.inventory.armorInventory.get(3).isEmpty() ? 0.42F : 0.5F;
            head.getModelHead().translateRotate(matrix);
            matrix.translate(0, -offset, 0);
            matrix.rotate(Vector3f.ZN.rotationDegrees(180));
            matrix.rotate(Vector3f.YP.rotationDegrees(90));

            float alpha = 0.8F;

            BUFFER.begin(GL11.GL_TRIANGLE_FAN, DefaultVertexFormats.POSITION_COLOR);
            BUFFER.pos(matrix.getLast().getMatrix(), 0, 0.3f, 0).color(ColorUtil.multAlpha(Theme.getInstance().clientColor(), alpha)).endVertex();
            for (int i = 0, size = 360; i <= size; i++) {
                int color = ColorUtil.fade(i * multiplier);
                BUFFER.pos(matrix.getLast().getMatrix(), -MathHelper.sin(i * MathUtils.PI2 / size) * width, 0, MathHelper.cos(i * MathUtils.PI2 / size) * width).color(ColorUtil.multAlpha(color, alpha)).endVertex();
            }
            TESSELLATOR.draw();
            BUFFER.begin(GL11.GL_LINE_LOOP, DefaultVertexFormats.POSITION_COLOR);
            for (int i = 0, size = 360; i <= size; i++) {
                int color = ColorUtil.fade(i * multiplier);
                BUFFER.pos(matrix.getLast().getMatrix(), -MathHelper.sin(i * MathUtils.PI2 / size) * width, 0, MathHelper.cos(i * MathUtils.PI2 / size) * width).color(ColorUtil.multAlpha(color, alpha)).endVertex();
            }
            TESSELLATOR.draw();
            matrix.pop();

            RenderSystem.shadeModel(GL11.GL_FLAT);
            RenderSystem.enableCull();
            RenderSystem.enableDepthTest();
            RenderSystem.enableTexture();
            RenderSystem.disableBlend();
            if (light) {
                RenderSystem.enableLighting();
            }
            RenderSystem.popMatrix();
        }
    }

}