package org.obsidian.client.utils.rotation;

import lombok.experimental.UtilityClass;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import org.obsidian.client.api.interfaces.IMinecraft;

import java.util.Optional;
import java.util.function.Predicate;

@UtilityClass
public class RayTraceUtil implements IMinecraft {

    public Entity getTargetedEntity(Entity target, float targetYaw, float targetPitch, double distance) {
        Entity viewerEntity = mc.getRenderViewEntity();
        if (viewerEntity == null || mc.world == null) {
            return null;
        }

        Vector3d startVector = viewerEntity.getEyePosition(mc.getRenderPartialTicks());
        Vector3d directionVector = getVectorForRotation(targetPitch, targetYaw);
        Vector3d endVector = startVector.add(directionVector.scale(distance));

        AxisAlignedBB targetBoundingBox = target.getBoundingBox().grow(target.getCollisionBorderSize());
        EntityRayTraceResult entityRayTraceResult = traceEntities(viewerEntity, startVector, endVector, targetBoundingBox,
                (entity) -> !entity.isSpectator() && entity.canBeCollidedWith(), distance);

        return entityRayTraceResult != null ? entityRayTraceResult.getEntity() : null;
    }

    public EntityRayTraceResult traceEntities(Entity shooter, Vector3d startVector, Vector3d endVector, AxisAlignedBB boundingBox, Predicate<Entity> filter, double distance) {
        World world = shooter.world;
        double closestDistance = distance;
        Entity closestEntity = null;
        Vector3d closestHitVector = null;

        for (Entity entity : world.getEntitiesInAABBexcluding(shooter, boundingBox, filter)) {
            AxisAlignedBB entityBoundingBox = entity.getBoundingBox().grow(entity.getCollisionBorderSize());
            Optional<Vector3d> optional = entityBoundingBox.rayTrace(startVector, endVector);

            if (entityBoundingBox.contains(startVector) || optional.isPresent()) {
                double distanceToHit = optional.map(startVector::distanceTo).orElse(0.0D);
                if (distanceToHit < closestDistance || closestDistance == 0.0D) {
                    if (entity.getLowestRidingEntity() != shooter.getLowestRidingEntity()) {
                        closestEntity = entity;
                        closestHitVector = optional.orElse(startVector);
                        closestDistance = distanceToHit;
                    }
                }
            }
        }

        return closestEntity == null ? null : new EntityRayTraceResult(closestEntity, closestHitVector);
    }

    public RayTraceResult calculateRayTrace(double distance, float yaw, float pitch, Entity entity, boolean ignoreBlocks) {
        Vector3d startVector = mc.player.getEyePosition(mc.getRenderPartialTicks());
        Vector3d directionVector = getVectorForRotation(pitch, yaw);
        Vector3d endVector = startVector.add(directionVector.scale(distance));

        RayTraceResult blockResult = traceBlock(startVector, endVector, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE);
        double entityDistance = blockResult.getHitVec().squareDistanceTo(startVector);

        AxisAlignedBB entityBoundingBox = entity.getBoundingBox().expand(directionVector.scale(distance)).grow(1.0D);
        EntityRayTraceResult entityRayTraceResult = ProjectileHelper.rayTraceEntities(entity, startVector, endVector, entityBoundingBox,
                (e) -> !e.isSpectator() && e.isAlive() && e.canBeCollidedWith(), distance);

        if (entityRayTraceResult != null && (ignoreBlocks || entityRayTraceResult.getHitVec().squareDistanceTo(startVector) < entityDistance)) {
            return entityRayTraceResult;
        }

        return blockResult;
    }

    public boolean rayTraceEntity(float yaw, float pitch, double distance, Entity entity) {
        Vector3d eyeVec = mc.player.getEyePosition(mc.getRenderPartialTicks());
        Vector3d lookVec = getVectorForRotation(pitch, yaw);
        Vector3d endVec = eyeVec.add(lookVec.scale(distance));

        AxisAlignedBB entityBox = entity.getBoundingBox();
        return entityBox.contains(eyeVec) || entityBox.rayTrace(eyeVec, endVec).isPresent();
    }

    public Vector3d getVectorForRotation(float pitch, float yaw) {
        float yawRadians = -yaw * ((float) Math.PI / 180) - (float) Math.PI;
        float pitchRadians = -pitch * ((float) Math.PI / 180);

        float cosYaw = MathHelper.cos(yawRadians);
        float sinYaw = MathHelper.sin(yawRadians);
        float cosPitch = -MathHelper.cos(pitchRadians);
        float sinPitch = MathHelper.sin(pitchRadians);

        return new Vector3d(sinYaw * cosPitch, sinPitch, cosYaw * cosPitch);
    }

    public RayTraceResult traceBlock(Vector3d startVec, Vector3d endVec, RayTraceContext.BlockMode blockMode, RayTraceContext.FluidMode fluidMode) {
        return mc.world.rayTraceBlocks(new RayTraceContext(
                startVec,
                endVec,
                blockMode,
                fluidMode,
                mc.player)
        );
    }
}
