package org.obsidian.client.managers.component.impl.aura;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Namespaced;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.component.Component;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.impl.combat.KillAura;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.math.Interpolator;
import org.obsidian.client.utils.math.Mathf;
import org.obsidian.client.utils.render.color.ColorUtil;
import org.obsidian.client.utils.render.draw.GLUtil;
import org.obsidian.client.utils.render.draw.Project;
import org.obsidian.client.utils.render.draw.RectUtil;
import org.obsidian.client.utils.render.draw.RenderUtil3D;
import org.obsidian.client.utils.rotation.AuraUtil;
import org.joml.Vector2f;

public class AuraComponent extends Component {
    private final Namespaced markerLocation = new Namespaced("texture/marker.png");
    public LivingEntity target;
    public final Animation markerAnimation = new Animation();
    private final Vector2f markerPosition = new Vector2f();

    @EventHandler
    public void onEvent(Render2DEvent event) {
        KillAura aura = KillAura.getInstance();
        markerAnimation.update();
        if (aura.target() != null) target = aura.target();
        markerAnimation.run(aura.target() == null ? 0 : 1, 1, Easings.LINEAR, true);
        if (markerAnimation.getValue() == 0.0) target = null;
        if (target == null) return;

        auraProcess(event);
    }

    private void auraProcess(Render2DEvent event) {
        KillAura aura = KillAura.getInstance();
        if (aura.checks().getValue("Таргет есп")) {
            drawMarker(event.getMatrix());
        }
    }

    public void drawMarker(MatrixStack matrix) {
        KillAura aura = KillAura.getInstance();
        matrix.push();
        float alphaPC = markerAnimation.get();
        Vector3d vec = RenderUtil3D.interpolate(target, mc.getRenderPartialTicks());
        double x = vec.x;
        double y = vec.y;
        double z = vec.z;

        Vector2f marker = Project.project2D(x, y + ((target.getEyeHeight() + 0.4F) * 0.5F), z);
        if (marker.x == Float.MAX_VALUE && marker.y == Float.MAX_VALUE) return;
        markerPosition.lerp(marker, target.isAlive() ? 0.5F : 0.05F);

        final float size = 50;

        long currentTimeMillis = System.currentTimeMillis();
        float angle = (float) Mathf.clamp(0, 90, ((Math.sin(currentTimeMillis / 500D) + 1F) / 2F) * 90);
        float scale = (float) Mathf.clamp(0.9, 1.1, ((Math.sin(currentTimeMillis / 500D) + 1F) / 2F) * 1.25);
        float rotate = (float) Mathf.clamp(0, 360, ((Math.sin(currentTimeMillis / 1000D) + 1F) / 2F) * 360);

        matrix.translate(markerPosition.x, markerPosition.y, 0.0F);
        matrix.scale(scale, scale, 1F);
        matrix.translate(-markerPosition.x, -markerPosition.y, 0.0F);

        float distanceFactor = (float) (1F - AuraUtil.getStrictDistance(target) / aura.attackDistance());
        float sc = Mathf.clamp(0.75F, 1F, distanceFactor);
        sc = Interpolator.lerp(scale, sc, 0.5F);

        matrix.translate(markerPosition.x, markerPosition.y, 0.0F);
        matrix.scale(sc, sc, sc);
        matrix.translate(-markerPosition.x, -markerPosition.y, 0.0F);

        float hurtPC = (float) Math.sin(target.hurtTime * (18F * Math.PI / 180F));

        int red = ColorUtil.getColor(255, 0, 0, alphaPC);

        int color1 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(0), alphaPC), red, hurtPC);
        int color2 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(90), alphaPC), red, hurtPC);
        int color3 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(180), alphaPC), red, hurtPC);
        int color4 = ColorUtil.overCol(ColorUtil.multAlpha(ColorUtil.fade(270), alphaPC), red, hurtPC);

        GLUtil.rotate(matrix, markerPosition.x, markerPosition.y, Vector3f.ZN.rotationDegrees(45F - (angle - 45) + rotate), () -> {
            if (alphaPC != 0.0F) {
                mc.getTextureManager().bindTexture(markerLocation);
                RectUtil.drawRect(matrix, markerPosition.x - size, markerPosition.y - size, size * 2F, size * 2F, color1, color2, color3, color4, true, true);
            }
        });
        matrix.pop();
    }

}
