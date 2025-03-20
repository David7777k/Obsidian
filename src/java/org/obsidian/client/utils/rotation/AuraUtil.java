package org.obsidian.client.utils.rotation;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.component.impl.rotation.Rotation;
import org.joml.Vector4f;

@UtilityClass
public class AuraUtil implements IMinecraft {

    public Vector3d getClosestVec(Vector3d vec, AxisAlignedBB AABB) {
        return new Vector3d(
                MathHelper.clamp(vec.getX(), AABB.minX, AABB.maxX),
                MathHelper.clamp(vec.getY(), AABB.minY, AABB.maxY),
                MathHelper.clamp(vec.getZ(), AABB.minZ, AABB.maxZ)
        );
    }

    public Vector3d getClosestVec(Vector3d vec, Entity entity) {
        return getClosestVec(vec, entity.getBoundingBox());
    }

    public Vector3d getClosestVec(Entity entity) {
        Vector3d eyePosVec = mc.player.getEyePosition(mc.getRenderPartialTicks());
        return getClosestVec(eyePosVec, entity).subtract(eyePosVec);
    }

    public double getStrictDistance(Entity entity) {
        return getClosestVec(entity).length();
    }

    public double getStrictDistance(LivingEntity entity) {
        return getClosestVec(entity).length();
    }

    public Vector3d getClosestTargetPoint(Entity entity) {
        return getClosestTargetPoint(mc.player.getEyePosition(mc.getRenderPartialTicks()), entity, Math.min(entity.getWidth(), entity.getHeight()) / 4F);
    }

    public Vector3d getClosestTargetPoint(Vector3d vec, Entity entity, float point) {
        if (entity == null) {
            return Vector3d.ZERO;
        }

        AxisAlignedBB boundingBox = entity.getBoundingBox().grow(-point);
        Vector3d center = boundingBox.getCenter();
        Vector3d closestPoint = Vector3d.ZERO;
        double closestDistance = Double.MAX_VALUE;

        for (double offsetX = 0; offsetX <= (boundingBox.maxX - boundingBox.minX) / 2; offsetX += 0.1) {
            for (double offsetY = 0; offsetY <= (boundingBox.maxY - boundingBox.minY) / 2; offsetY += 0.1) {
                for (double offsetZ = 0; offsetZ <= (boundingBox.maxZ - boundingBox.minZ) / 2; offsetZ += 0.1) {
                    for (int signX : new int[]{-1, 1}) {
                        for (int signY : new int[]{-1, 1}) {
                            for (int signZ : new int[]{-1, 1}) {
                                double x = center.x + signX * offsetX;
                                double y = center.y + signY * offsetY;
                                double z = center.z + signZ * offsetZ;
                                Vector3d potentialPoint = new Vector3d(x, y, z);
                                Vector2f rotation = RotationUtil.calculate(potentialPoint);
                                RayTraceResult result = RayTraceUtil.calculateRayTrace(
                                        mc.playerController.extendedReach() ? 6.0D : 3.0D,
                                        rotation.x,
                                        rotation.y,
                                        mc.player,
                                        false
                                );

                                if (result instanceof EntityRayTraceResult entityTrace && entityTrace.getEntity().equals(entity)) {
                                    double distance = vec.distanceTo(potentialPoint);
                                    if (distance < closestDistance) {
                                        closestDistance = distance;
                                        closestPoint = potentialPoint;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (!closestPoint.equals(Vector3d.ZERO)) {
            return closestPoint;
        }

        double closestX = MathHelper.clamp(vec.x, boundingBox.minX, boundingBox.maxX);
        double closestY = MathHelper.clamp(vec.y, boundingBox.minY, boundingBox.maxY);
        double closestZ = MathHelper.clamp(vec.z, boundingBox.minZ, boundingBox.maxZ);

        return new Vector3d(closestX, closestY, closestZ);
    }


    public Vector4f calculateRotation(Entity target) {
        Vector3d vec = getClosestTargetPoint(target).subtract(mc.player.getEyePosition(mc.getRenderPartialTicks()));

        float rawYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90F);
        float rawPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, Math.sqrt(Math.pow(vec.x, 2) + Math.pow(vec.z, 2)))));
        float yawDelta = MathHelper.wrapDegrees(rawYaw - mc.player.rotationYaw);
        float pitchDelta = rawPitch - mc.player.rotationPitch;

        return new Vector4f(rawYaw, rawPitch, yawDelta, pitchDelta);
    }

    public Vector4f calculateRotationFromCamera(LivingEntity target) {
        Vector3d vec = getClosestTargetPoint(target).subtract(mc.player.getEyePosition(mc.getRenderPartialTicks()));

        float rawYaw = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(vec.z, vec.x)) - 90F);
        float rawPitch = (float) (-Math.toDegrees(Math.atan2(vec.y, Math.sqrt(Math.pow(vec.x, 2) + Math.pow(vec.z, 2)))));
        float yawDelta = MathHelper.wrapDegrees(rawYaw - Rotation.cameraYaw());
        float pitchDelta = rawPitch - Rotation.cameraPitch();

        return new Vector4f(rawYaw, rawPitch, yawDelta, pitchDelta);
    }

    public double calculateFOV(LivingEntity target) {
        Vector4f rotation = calculateRotation(target);
        float yawDelta = rotation.z;
        float pitchDelta = rotation.w;

        return Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
    }

    public double calculateFOVFromCamera(LivingEntity target) {
        Vector4f rotation = calculateRotationFromCamera(target);
        float yawDelta = rotation.z;
        float pitchDelta = rotation.w;

        return Math.sqrt(yawDelta * yawDelta + pitchDelta * pitchDelta);
    }

}