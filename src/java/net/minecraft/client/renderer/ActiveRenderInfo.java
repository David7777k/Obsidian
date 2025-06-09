package net.minecraft.client.renderer;

import lombok.Getter;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.IBlockReader;
import net.optifine.reflect.Reflector;
import org.obsidian.client.managers.events.other.CameraClipEvent;
import org.obsidian.client.managers.events.player.RotationEvent;
import org.obsidian.client.utils.animation.Animation;
import org.obsidian.client.utils.animation.util.Easings;
import org.obsidian.client.utils.math.Mathf;

public class ActiveRenderInfo {
    private boolean valid;
    private IBlockReader world;
    private Entity renderViewEntity;
    private Vector3d pos = Vector3d.ZERO;
    private final BlockPos.Mutable blockPos = new BlockPos.Mutable();
    private final Vector3f look = new Vector3f(0.0F, 0.0F, 1.0F);
    private final Vector3f up = new Vector3f(0.0F, 1.0F, 0.0F);
    private final Vector3f left = new Vector3f(1.0F, 0.0F, 0.0F);
    private float pitch;
    private float yaw;
    private final Quaternion rotation = new Quaternion(0.0F, 0.0F, 0.0F, 1.0F);
    private boolean thirdPerson;
    @Getter
    private boolean thirdPersonReverse;
    private float height;
    private float previousHeight;
    private final Animation zoomAnimation = new Animation();
    public double zoomWheel = 0.0F;

    private double getUpdatedSmoothZooming(boolean zoomActive) {
        zoomWheel = Mathf.clamp(0.0F, 50.0F, zoomWheel);
        if (!zoomActive) zoomWheel = 0.0F;

        float zoomValue = (zoomActive ? 4.0F : 1.0F);
        float to = (float) (zoomValue + zoomWheel);
        zoomAnimation.update();
        zoomAnimation.run(to, zoomActive ? 0.5F : 0.0F, Easings.SINE_OUT, true);
        return zoomAnimation.getValue();
    }

    public void update(IBlockReader worldIn, Entity renderViewEntity, boolean thirdPersonIn, boolean thirdPersonReverseIn, float partialTicks) {
        this.valid = true;
        this.world = worldIn;
        this.renderViewEntity = renderViewEntity;
        this.thirdPerson = thirdPersonIn;
        this.thirdPersonReverse = thirdPersonReverseIn;
        RotationEvent event = new RotationEvent(renderViewEntity.getYaw(partialTicks), renderViewEntity.getPitch(partialTicks), partialTicks);
        event.hook();

        // set base position and orientation
        this.setDirection(event.getYaw(), event.getPitch());
        this.setPosition(MathHelper.lerp(partialTicks, renderViewEntity.prevPosX, renderViewEntity.getPosX()), MathHelper.lerp(partialTicks, renderViewEntity.prevPosY, renderViewEntity.getPosY()) + (double) MathHelper.lerp(partialTicks, this.previousHeight, this.height), MathHelper.lerp(partialTicks, renderViewEntity.prevPosZ, renderViewEntity.getPosZ()));
        if (thirdPersonIn) {
            // distance to offset the camera in third-person mode
            double distance = this.getUpdatedSmoothZooming(!renderViewEntity.isSpectator());
            distance = this.calcCameraDistance(distance);

            if (thirdPersonReverseIn) {
                // calculate how far the camera should be from the player
                distance = this.getUpdatedSmoothZooming(!renderViewEntity.isSpectator());
                distance = this.calcCameraDistance(distance);
                // shift the camera backward to render the player model
                // front view - camera placed in front of the model
                this.movePosition(-distance, 0.0D, 0.0D);

                this.setDirection(event.getYaw() + 180.0F, -event.getPitch());
            } else {
                // back view - camera placed behind the model
                this.movePosition(distance, 0.0D, 0.0D);
            }

        } else if (renderViewEntity instanceof LivingEntity && ((LivingEntity) renderViewEntity).isSleeping()) {
            Direction direction = ((LivingEntity) renderViewEntity).getBedDirection();
            this.setDirection(direction != null ? direction.getHorizontalAngle() - 180.0F : 0.0F, 0.0F);
            this.movePosition(0.0D, 0.3D, 0.0D);
        }
    }

    public void interpolateHeight() {
        if (this.renderViewEntity != null) {
            this.previousHeight = this.height;
            this.height += (this.renderViewEntity.getEyeHeight() - this.height) * 0.5F;
        }
    }

    /**
     * Checks for collision of the third person camera and returns the distance
     */
    private double calcCameraDistance(double startingDistance) {
        CameraClipEvent event = new CameraClipEvent();
        event.hook();
        for (int i = 0; i < 8; ++i) {
            float f = ((i & 1) * 2 - 1) * 0.1F;
            float f1 = ((i >> 1 & 1) * 2 - 1) * 0.1F;
            float f2 = ((i >> 2 & 1) * 2 - 1) * 0.1F;
            Vector3d start = this.pos.add(f, f1, f2);
            Vector3d end = new Vector3d(
                    this.pos.x - this.look.getX() * startingDistance + f + f2,
                    this.pos.y - this.look.getY() * startingDistance + f1,
                    this.pos.z - this.look.getZ() * startingDistance + f2
            );
            RayTraceResult result = this.world.rayTraceBlocks(
                    new RayTraceContext(start, end, RayTraceContext.BlockMode.VISUAL, RayTraceContext.FluidMode.NONE, this.renderViewEntity)
            );

            if (!event.isCancelled() && result.getType() != RayTraceResult.Type.MISS) {
                double dist = result.getHitVec().distanceTo(this.pos);
                if (dist < startingDistance) {
                    startingDistance = dist;
                }
            }
        }
        return startingDistance;
    }

    /**
     * Moves the render position relative to the view direction, for third person camera
     */
    protected void movePosition(double distanceOffset, double verticalOffset, double horizontalOffset) {
        double dx = this.look.getX() * distanceOffset + this.up.getX() * verticalOffset + this.left.getX() * horizontalOffset;
        double dy = this.look.getY() * distanceOffset + this.up.getY() * verticalOffset + this.left.getY() * horizontalOffset;
        double dz = this.look.getZ() * distanceOffset + this.up.getZ() * verticalOffset + this.left.getZ() * horizontalOffset;
        this.setPosition(this.pos.x + dx, this.pos.y + dy, this.pos.z + dz);
    }

    protected void setDirection(float pitchIn, float yawIn) {
        this.pitch = yawIn;
        this.yaw = pitchIn;
        this.rotation.set(0.0F, 0.0F, 0.0F, 1.0F);
        this.rotation.multiply(Vector3f.YP.rotationDegrees(-pitchIn));
        this.rotation.multiply(Vector3f.XP.rotationDegrees(yawIn));
        this.look.set(0.0F, 0.0F, 1.0F);
        this.look.transform(this.rotation);
        this.up.set(0.0F, 1.0F, 0.0F);
        this.up.transform(this.rotation);
        this.left.set(1.0F, 0.0F, 0.0F);
        this.left.transform(this.rotation);
    }

    protected void setPosition(double x, double y, double z) {
        this.setPosition(new Vector3d(x, y, z));
    }

    protected void setPosition(Vector3d posIn) {
        this.pos = posIn;
        this.blockPos.setPos(posIn.x, posIn.y, posIn.z);
    }

    public Vector3d getProjectedView() {
        return this.pos;
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }

    public float getPitch() {
        return this.pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public Quaternion getRotation() {
        return this.rotation;
    }

    public Entity getRenderViewEntity() {
        return this.renderViewEntity;
    }

    public boolean isValid() {
        return this.valid;
    }

    public boolean isThirdPerson() {
        return this.thirdPerson;
    }

    public FluidState getFluidState() {
        if (!this.valid) {
            return Fluids.EMPTY.getDefaultState();
        } else {
            FluidState f = this.world.getFluidState(this.blockPos);
            if (!f.isEmpty() && this.pos.y >= this.blockPos.getY() + f.getActualHeight(this.world, this.blockPos)) {
                return Fluids.EMPTY.getDefaultState();
            }
            return f;
        }
    }

    public BlockState getBlockState() {
        return !this.valid ? Blocks.AIR.getDefaultState() : this.world.getBlockState(this.blockPos);
    }

    public void setAnglesInternal(float yawIn, float pitchIn) {
        this.yaw = yawIn;
        this.pitch = pitchIn;
    }

    public BlockState getBlockAtCamera() {
        if (!this.valid) {
            return Blocks.AIR.getDefaultState();
        }
        BlockState state = this.world.getBlockState(this.blockPos);
        if (Reflector.IForgeBlockState_getStateAtViewpoint.exists()) {
            state = (BlockState) Reflector.call(state, Reflector.IForgeBlockState_getStateAtViewpoint, this.world, this.blockPos, this.pos);
        }
        return state;
    }

    public final Vector3f getViewVector() {
        return this.look;
    }

    public final Vector3f getUpVector() {
        return this.up;
    }

    public void clear() {
        this.world = null;
        this.renderViewEntity = null;
        this.valid = false;
    }
}
