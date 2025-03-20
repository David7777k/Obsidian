package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.mojang.blaze3d.matrix.MatrixStack;
import net.mojang.blaze3d.systems.RenderSystem;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.events.orbit.EventPriority;
import org.obsidian.client.managers.component.impl.target.TargetComponent;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.events.render.Render3DPosedEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.Project;
import org.obsidian.client.utils.render.framebuffer.CustomFramebuffer;
import org.obsidian.client.utils.render.shader.ShaderManager;
import org.obsidian.client.utils.render.shader.impl.entity.EntityShader;
import org.obsidian.client.utils.render.shader.impl.outline.EntityOutlineShader;
import org.joml.Vector2f;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ShaderEsp", category = Category.RENDER)
public class ShaderEsp extends Module {
    public static ShaderEsp getInstance() {
        return Instance.get(ShaderEsp.class);
    }

    private final CustomFramebuffer buffer = new CustomFramebuffer(true);
    private final EntityShader bloom = new EntityShader();
    private final ModeSetting mode = new ModeSetting(this, "Режим", "Внешний", "Внутренний", "Внешний и внутренний");
    private final SliderSetting iterations = new SliderSetting(this, "Сила", 3, 1, 5, 1);
    private final SliderSetting divider = new SliderSetting(this, "Сила размытия", 8, 1, 8, 0.1F);
    private final BooleanSetting layers = new BooleanSetting(this, "Слои", true);
    private final BooleanSetting chams = new BooleanSetting(this, "Чамсы", false);
    private final BooleanSetting outline = new BooleanSetting(this, "Обводка", true).setVisible(() -> !chams.getValue());

    public void patch(PlayerEntity entity, Runnable runnable) {
        Vector3d interpolated = entity.getPositionVec().subtract(entity.getPositionVec(mc.getRenderPartialTicks()));

        AxisAlignedBB aabb = entity.getBoundingBox().offset(interpolated.inverse().add(interpolated.scale(mc.getRenderPartialTicks())));
        Vector2f center = Project.project2D(aabb.getCenter());

        if (center.x == Float.MAX_VALUE && center.y == Float.MAX_VALUE) {
            return;
        }

        float minX = center.x, minY = center.y, maxX = center.x, maxY = center.y;

        for (Vector3d corner : aabb.getCorners()) {
            Vector2f vec = Project.project2D(corner);

            if (vec.x == Float.MAX_VALUE && vec.y == Float.MAX_VALUE) {
                continue;
            }

            minX = Math.min(minX, vec.x);
            minY = Math.min(minY, vec.y);
            maxX = Math.max(maxX, vec.x);
            maxY = Math.max(maxY, vec.y);
        }

        float posX = minX, posY = minY, width = maxX - minX, height = maxY - minY;

        float hurtPC = (float) Math.sin(entity.hurtTime * (18F * Math.PI / 180F));

        boolean isFriend = Obsidian.inst().friendManager().isFriend(entity.getGameProfile().getName());

        int color = ColorUtil.overCol(isFriend ? ColorUtil.GREEN : Theme.getInstance().clientColor(), ColorUtil.RED, hurtPC);

        ShaderManager gradient = ShaderManager.entityChamsShader;
        gradient.load();
        gradient.setUniformi("tex", 0);
        gradient.setUniformf("location", posX, posY);
        gradient.setUniformf("rectSize", width, height);
        gradient.setUniformf("color", ColorUtil.getRGBAf(color));

        runnable.run();

        gradient.unload();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEvent(Render3DPosedEvent event) {
        MatrixStack stack = event.getMatrix();
        buffer.setup();

        TargetComponent.getTargets(128, this::isValid, false)
                .forEach(entity -> {
                    if (entity instanceof PlayerEntity player) {
                        patch(player, () -> {
                            EntityRendererManager rendererManager = mc.getRenderManager();
                            stack.push();
                            stack.translate(-rendererManager.renderPosX(), -rendererManager.renderPosY(), -rendererManager.renderPosZ());
                            RenderSystem.depthMask(true);
                            rendererManager.setRenderShadow(false);
                            rendererManager.setRenderName(false);
                            IRenderTypeBuffer.Impl irendertypebuffer$impl = mc.getRenderTypeBuffers().getBufferSource();
                            Vector3d pos = entity.getPositionVec(event.getPartialTicks());
                            EntityRenderer<?> renderer = rendererManager.getRenderer(entity);
                            boolean nameVisible = renderer.isRenderName();

                            if (nameVisible) renderer.setRenderName(false);
                            if (!layers.getValue()) renderer.setRenderLayers(false);
                            rendererManager.renderClearEntityStatic(entity, pos.getX(), pos.getY(), pos.getZ(), entity.rotationYaw, event.getPartialTicks(), stack, irendertypebuffer$impl, rendererManager.getPackedLight(entity, event.getPartialTicks()));
                            if (!layers.getValue()) renderer.setRenderLayers(true);
                            if (nameVisible) renderer.setRenderName(true);

                            irendertypebuffer$impl.finish();

                            rendererManager.setRenderName(true);
                            rendererManager.setRenderShadow(true);
                            RenderSystem.depthMask(false);
                            RenderSystem.enableDepthTest();
                            stack.pop();
                        });
                    }
                });

        buffer.stop();
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(Render2DEvent event) {
        if (outline.getValue()) {
            EntityOutlineShader.draw(1, buffer.framebufferTexture);
        }
        bloom.render(event.getMatrix(), buffer.framebufferTexture, iterations.getValue().intValue(), 1F, 4 + divider.getValue());
        if (chams.getValue()) buffer.draw();
        buffer.framebufferClear();
        mc.getFramebuffer().bindFramebuffer(true);
    }

    private boolean isValid(final Entity entity) {
        if (!entity.isAlive() || entity.isGlowing()) {
            return false;
        }
        if (mc.renderViewEntity != null && entity == mc.renderViewEntity && mc.gameSettings.getPointOfView().firstPerson()) {
            return false;
        }
        return isInView(entity) && entity instanceof PlayerEntity;
    }

    public boolean isInView(Entity entity) {
        if (mc.getRenderViewEntity() == null || mc.worldRenderer.getClippinghelper() == null) {
            return false;
        }
        return mc.worldRenderer.getClippinghelper().isBoundingBoxInFrustum(entity.getBoundingBox()) || entity.ignoreFrustumCheck;
    }
}
