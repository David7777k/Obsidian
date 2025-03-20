package org.obsidian.client.utils.render.draw;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.module.impl.render.NoRender;
import org.joml.Vector2f;

@UtilityClass
public class Project implements IMinecraft {
    public Vector2f project2D(Vector3d vec) {
        return project2D(vec.x, vec.y, vec.z);
    }

    public Vector2f project2D(double x, double y, double z) {
        Vector3d camera_pos = mc.getRenderManager().info.getProjectedView();
        Quaternion cameraRotation = mc.getRenderManager().getCameraOrientation().copy();
        cameraRotation.conjugate();

        Vector3f result3f = new Vector3f((float) (camera_pos.x - x), (float) (camera_pos.y - y), (float) (camera_pos.z - z));
        result3f.transform(cameraRotation);

        Entity renderViewEntity = mc.getRenderViewEntity();
        if (renderViewEntity instanceof PlayerEntity playerentity) {
            final NoRender noRender = NoRender.getInstance();
            if (!noRender.isEnabled() || !noRender.elements().getValue("Тряска"))
                hurtCameraEffect(playerentity, result3f);
            if (mc.gameSettings.viewBobbing) {
                calculateViewBobbing(playerentity, result3f);
            }
        }

        double fov = mc.gameRenderer.getFOVModifier(mc.getRenderManager().info, mc.getRenderPartialTicks(), true);

        return calculateScreenPosition(result3f, fov);
    }

    private void calculateViewBobbing(PlayerEntity playerentity, Vector3f result3f) {
        float walked = playerentity.distanceWalkedModified;
        float f = walked - playerentity.prevDistanceWalkedModified;
        float f1 = -(walked + f * mc.getRenderPartialTicks());
        float f2 = MathHelper.lerp(mc.getRenderPartialTicks(), playerentity.prevCameraYaw, playerentity.cameraYaw);

        Quaternion quaternion = new Quaternion(Vector3f.XP, Math.abs(MathHelper.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F, true);
        quaternion.conjugate();
        result3f.transform(quaternion);

        Quaternion quaternion1 = new Quaternion(Vector3f.ZP, MathHelper.sin(f1 * (float) Math.PI) * f2 * 3.0F, true);
        quaternion1.conjugate();
        result3f.transform(quaternion1);

        Vector3f bobTranslation = new Vector3f((MathHelper.sin(f1 * (float) Math.PI) * f2 * 0.5F), (-Math.abs(MathHelper.cos(f1 * (float) Math.PI) * f2)), 0.0f);
        bobTranslation.setY(-bobTranslation.getY());
        result3f.add(bobTranslation);
    }

    private void hurtCameraEffect(PlayerEntity playerentity, Vector3f result3f) {
        float partialTicks = mc.getRenderPartialTicks();
        float f = (float) playerentity.hurtTime - partialTicks;

        if (playerentity.getShouldBeDead()) {
            float f1 = Math.min((float) playerentity.deathTime + partialTicks, 20.0F);
            Quaternion quaternion1 = new Quaternion(Vector3f.ZP, 40.0F - 8000.0F / (f1 + 200.0F), true);
            quaternion1.conjugate();
            result3f.transform(quaternion1);
        }

        if (f < 0.0F) {
            return;
        }

        f = f / (float) playerentity.maxHurtTime;
        f = MathHelper.sin(f * f * f * f * (float) Math.PI);
        float f2 = playerentity.attackedAtYaw;

        Quaternion quaternion1 = new Quaternion(Vector3f.YP, -f2, true);
        quaternion1.conjugate();
        result3f.transform(quaternion1);

        Quaternion quaternion2 = new Quaternion(Vector3f.ZP, -f * 14.0F, true);
        quaternion2.conjugate();
        result3f.transform(quaternion2);

        Quaternion quaternion3 = new Quaternion(Vector3f.ZP, f2, true);
        quaternion3.conjugate();
        result3f.transform(quaternion3);
    }

    private Vector2f calculateScreenPosition(Vector3f result3f, double fov) {
        float halfHeight = mw.getScaledHeight() / 2.0F;
        float scaleFactor = halfHeight / (result3f.getZ() * (float) Math.tan(Math.toRadians(fov / 2.0F)));
        if (result3f.getZ() < 0.0F) {
            return new Vector2f(-result3f.getX() * scaleFactor + mw.getScaledWidth() / 2.0F, mw.getScaledHeight() / 2.0F - result3f.getY() * scaleFactor);
        }
        return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }


}