package org.obsidian.client.utils.rotation;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector2f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import org.obsidian.client.api.interfaces.IMinecraft;

@UtilityClass
public class RotationUtil implements IMinecraft {

    public Vector2f calculate(final Vector3d fromVec, final Vector3d toVec) {
        final double TO_DEGREES = 180.0F / Math.PI;
        final Vector3d diff = toVec.subtract(fromVec);
        final double distance = Math.hypot(diff.x(), diff.z());
        float yaw = (float) (MathHelper.atan2(diff.z(), diff.x()) * TO_DEGREES) - 90.0F;
        final float pitch = (float) (-(MathHelper.atan2(diff.y(), distance) * TO_DEGREES));
        return new Vector2f(yaw, pitch);
    }

    public Vector2f calculate(final org.joml.Vector3d fromVec, final org.joml.Vector3d toVec) {
        final double TO_DEGREES = 180.0F / Math.PI;
        final org.joml.Vector3d diff = toVec.sub(fromVec);
        final double distance = Math.hypot(diff.x(), diff.z());
        float yaw = (float) (MathHelper.atan2(diff.z(), diff.x()) * TO_DEGREES) - 90.0F;
        final float pitch = (float) (-(MathHelper.atan2(diff.y(), distance) * TO_DEGREES));
        return new Vector2f(yaw, pitch);
    }

    public Vector2f calculate(final Entity entity) {
        return calculate(entity.getPositionVec().add(0, entity.getEyeHeight(), 0));
    }

    public Vector2f calculate(final Vector3d toVec) {
        return calculate(mc.player.getPositionVec().add(0, mc.player.getEyeHeight(), 0), toVec);
    }

    public Vector2f calculate(final Vector3d toVec, final Direction dir) {
        double x = toVec.x() + 0.5D;
        double y = toVec.y() + 0.5D;
        double z = toVec.z() + 0.5D;

        x += (double) dir.getDirectionVec().getX() * 0.5D;
        y += (double) dir.getDirectionVec().getY() * 0.5D;
        z += (double) dir.getDirectionVec().getZ() * 0.5D;
        return calculate(new Vector3d(x, y, z));
    }

    public static float getAngleDifference(float dir, float yaw) {
        float f = Math.abs(yaw - dir) % 360.0f;
        return f > 180.0f ? 360.0f - f : f;
    }

    public static Vector3d getEyesPos(Entity entity) {
        return entity.getPositionVec().add(0, entity.getEyeHeight(entity.getPose()), 0);
    }

    public static float[] calculateAngle(Vector3d to) {
        return calculateAngle(getEyesPos(mc.player), to);
    }

    public static float[] calculateAngle(Vector3d from, Vector3d to) {
        double difX = to.x - from.x;
        double difY = (to.y - from.y) * -1.0;
        double difZ = to.z - from.z;
        double dist = MathHelper.sqrt((float) (difX * difX + difZ * difZ));
        float yD = (float) MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difZ, difX)) - 90.0);
        float pD = (float) MathHelper.clamp(MathHelper.wrapDegrees(Math.toDegrees(Math.atan2(difY, dist))), -90f, 90f);
        return new float[]{yD, pD};
    }

    public Vector3f getDirectionVector(float yaw, float pitch) {
        float yawRadians = (float) Math.toRadians(yaw);
        float pitchRadians = (float) Math.toRadians(pitch);

        float x = -MathHelper.cos(pitchRadians) * MathHelper.sin(yawRadians);
        float y = -MathHelper.sin(pitchRadians);
        float z = MathHelper.cos(pitchRadians) * MathHelper.cos(yawRadians);

        return new Vector3f(x, y, z);
    }

    public float calculateFov(float cameraYaw, float cameraPitch, float targetYaw, float targetPitch) {
        Vector3f cameraDirection = getDirectionVector(cameraYaw, cameraPitch);
        Vector3f targetDirection = getDirectionVector(targetYaw, targetPitch);

        float dotProduct = cameraDirection.dot(targetDirection);
        dotProduct = MathHelper.clamp(dotProduct, -1.0f, 1.0f);

        float angleRadians = (float) Math.acos(dotProduct);

        return (float) Math.toDegrees(angleRadians);
    }
}
