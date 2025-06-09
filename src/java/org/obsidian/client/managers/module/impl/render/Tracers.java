package org.obsidian.client.managers.module.impl.render;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.vector.Vector3d;
import org.lwjgl.opengl.GL11;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.render.Render3DLastEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.ColorSetting;
import org.obsidian.client.utils.math.Interpolator;
import org.obsidian.client.utils.render.color.ColorUtil;

import static org.lwjgl.opengl.GL11.*;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Tracers", category = Category.RENDER)
public class Tracers extends Module {

    final ColorSetting tracerColor = new ColorSetting(this, "Цвет", ColorUtil.getColor(255, 50, 50));
    final BooleanSetting ignoreNaked = new BooleanSetting(this, "Игнорировать голых", true);
    final BooleanSetting distanceColors = new BooleanSetting(this, "Цвет по дистанции", false);

    final Tessellator tessellator = Tessellator.getInstance();
    final BufferBuilder buffer = tessellator.getBuffer();

    public Tracers() {
        getSettings().add(tracerColor);
        getSettings().add(ignoreNaked);
        getSettings().add(distanceColors);
    }

    @EventHandler
    public void onRender3D(Render3DLastEvent event) {
        if (mc.world == null || mc.player == null) return;

        glPushMatrix();

        glDisable(GL_TEXTURE_2D);
        glDisable(GL_DEPTH_TEST);

        glEnable(GL_BLEND);
        glEnable(GL_LINE_SMOOTH);

        glLineWidth(1.5f);

        // Статичная камера (без анимации) - как в оригинале
        Vector3d cam = new Vector3d(0, 0, 150)
                .rotatePitch((float) -(Math.toRadians(mc.gameRenderer.getActiveRenderInfo().getPitch())))
                .rotateYaw((float) -Math.toRadians(mc.gameRenderer.getActiveRenderInfo().getYaw()));

        for (PlayerEntity player : mc.world.getPlayers()) {
            if (!isValidTarget(player)) continue;
            if (ignoreNaked.get() && player.getTotalArmorValue() == 0.0f) continue;

            // Расчет цвета
            int color = getPlayerColor(player);
            ColorUtil.setColor(color);

            // Интерполированная позиция игрока
            Vector3d pos = getInterpolatedPosition(player)
                    .subtract(mc.gameRenderer.getActiveRenderInfo().getProjectedView());

            // Рисуем линию от центра экрана к игроку
            buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
            buffer.pos(cam.x, cam.y, cam.z).endVertex();
            buffer.pos(pos.x, pos.y, pos.z).endVertex();
            tessellator.draw();
        }

        glDisable(GL_BLEND);
        glDisable(GL_LINE_SMOOTH);

        glEnable(GL_TEXTURE_2D);
        glEnable(GL_DEPTH_TEST);

        glPopMatrix();

        // Сбрасываем цвет
        glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private Vector3d getInterpolatedPosition(PlayerEntity player) {
        double x = Interpolator.lerp(player.lastTickPosX, player.getPosX(), mc.getRenderPartialTicks());
        double y = Interpolator.lerp(player.lastTickPosY, player.getPosY(), mc.getRenderPartialTicks());
        double z = Interpolator.lerp(player.lastTickPosZ, player.getPosZ(), mc.getRenderPartialTicks());
        return new Vector3d(x, y, z);
    }

    private int getPlayerColor(PlayerEntity player) {
        if (distanceColors.get()) {
            double distance = mc.player.getPositionVec().distanceTo(player.getPositionVec());
            float red = (float) Math.min(1.0, Math.max(0, (50.0 - distance) / 50.0));
            float green = 1.0f - red;

            int redColor = (int) (red * 255);
            int greenColor = (int) (green * 255);
            int blueColor = 0;

            return (255 << 24) | (redColor << 16) | (greenColor << 8) | blueColor;
        }
        return tracerColor.getValue();
    }

    private boolean isValidTarget(PlayerEntity player) {
        return player != mc.player && player.isAlive();
    }

    @Override
    public boolean isStarred() {
        return true;
    }
}