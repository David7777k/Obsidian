package org.obsidian.common.impl.chunkanimator;

import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.chunk.ChunkRenderDispatcher;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.mojang.blaze3d.platform.GlStateManager;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.module.impl.render.ChunkAnimator;

import java.util.Objects;
import java.util.WeakHashMap;

public class AnimationHandler implements IMinecraft {

    private final WeakHashMap<ChunkRenderDispatcher.ChunkRender, AnimationData> timeStamps = new WeakHashMap<>();

    public void preRender(final ChunkRenderDispatcher.ChunkRender chunkRender) {
        final AnimationData animationData = timeStamps.get(chunkRender);

        if (animationData == null) {
            return;
        }

        final int mode = ChunkAnimator.getInstance().animationMode().getValue().mode();
        final int animationDuration = ChunkAnimator.getInstance().animationDuration().getValue().intValue();

        long time = animationData.timeStamp;

        // If preRender hasn't been called on this chunk yet, prepare to start the animation.
        if (time == -1L) {
            time = System.currentTimeMillis();

            animationData.timeStamp = time;

            // If using mode 4, set chunkFacing.
            if (mode == 4) {
                animationData.chunkFacing = this.mc.player != null ?
                        this.getChunkFacing(this.getZeroedPlayerPos(this.mc.player).subtract(this.getZeroedCenteredChunkPos(chunkRender.getPosition()))) : Direction.NORTH;
            }
        }

        final long timeDif = System.currentTimeMillis() - time;

        if (timeDif < animationDuration) {
            final int chunkY = chunkRender.getPosition().getY();
            final int animationMode = mode == 2 ? (chunkY < Objects.requireNonNull(this.mc.world).getWorldInfo().getVoidFogHeight() ? 0 : 1) : mode == 4 ? 3 : mode;

            switch (animationMode) {
                case 0:
                    this.translate(0, -chunkY + ChunkAnimator.getInstance().getFunctionValue(timeDif, 0, chunkY, animationDuration), 0);
                    break;
                case 1:
                    this.translate(0, 256 - chunkY - ChunkAnimator.getInstance().getFunctionValue(timeDif, 0, 256 - chunkY, animationDuration), 0);
                    break;
                case 3:
                    Direction chunkFacing = animationData.chunkFacing;

                    if (chunkFacing != null) {
                        final Vector3i vec = chunkFacing.getDirectionVec();
                        final double mod = -(200 - ChunkAnimator.getInstance().getFunctionValue(timeDif, 0, 200, animationDuration));

                        this.translate(vec.getX() * mod, 0, vec.getZ() * mod);
                    }
                    break;
            }
        } else {
            this.timeStamps.remove(chunkRender);
        }
    }

    private void translate(final double x, final double y, final double z) {
        GlStateManager.translated(x, y, z);
    }

    public void setOrigin(final ChunkRenderDispatcher.ChunkRender renderChunk, final BlockPos position) {
        if (this.mc.player == null)
            return;

        final BlockPos zeroedPlayerPos = this.getZeroedPlayerPos(this.mc.player);
        final BlockPos zeroedCenteredChunkPos = this.getZeroedCenteredChunkPos(position);

        timeStamps.put(renderChunk, new AnimationData(-1L, ChunkAnimator.getInstance().animationMode().getValue().mode() == 3 ?
                this.getChunkFacing(zeroedPlayerPos.subtract(zeroedCenteredChunkPos)) : null));
    }

    private BlockPos getZeroedPlayerPos(final ClientPlayerEntity player) {
        final BlockPos playerPos = player.getPosition();
        return playerPos.add(0, -playerPos.getY(), 0);
    }

    private BlockPos getZeroedCenteredChunkPos(final BlockPos position) {
        return position.add(8, -position.getY(), 8);
    }

    private Direction getChunkFacing(final Vector3i dif) {
        int difX = Math.abs(dif.getX());
        int difZ = Math.abs(dif.getZ());

        return difX > difZ ? dif.getX() > 0 ? Direction.EAST : Direction.WEST : dif.getZ() > 0 ? Direction.SOUTH : Direction.NORTH;
    }

    public void clear() {
        this.timeStamps.clear();
    }

    private static class AnimationData {
        public long timeStamp;
        public Direction chunkFacing;

        public AnimationData(final long timeStamp, final Direction chunkFacing) {
            this.timeStamp = timeStamp;
            this.chunkFacing = chunkFacing;
        }
    }

}