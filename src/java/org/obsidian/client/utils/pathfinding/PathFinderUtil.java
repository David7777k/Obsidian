package org.obsidian.client.utils.pathfinding;

import lombok.experimental.UtilityClass;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.util.math.BlockPos;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.utils.player.PlayerUtil;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class PathFinderUtil implements IMinecraft {
    public List<Vec3> computePath(final Vec3 from, final Vec3 to, final boolean exact) {
        return computePath(from, to, exact, 1);
    }

    public List<Vec3> computePath(Vec3 from, final Vec3 to, final boolean exact, final double step) {
        final BlockPos blockPos = new BlockPos(from.mc());
        final BlockState state = mc.world.getBlockState(blockPos);
        final Block block = state.getBlock();

        if (!canPassThroughMaterial(block)) {
            from = from.addVector(0, 1, 0);
        }

        final PathFinder pathFinder = new PathFinder(from, to);
        pathFinder.compute();

        int i = 0;
        Vec3 lastLoc = null;
        Vec3 lastDashLoc = null;
        final ArrayList<Vec3> path = new ArrayList<>();
        final ArrayList<Vec3> pathFinderPath = pathFinder.getPath();
        for (final Vec3 pathElm : pathFinderPath) {
            if (i == 0 || i == pathFinderPath.size() - 1) {
                path.add(pathElm.addVector(0.5, 0, 0.5));
                lastDashLoc = pathElm;
            } else {
                boolean canContinue = true;
                if (pathElm.squareDistanceTo(lastDashLoc) > step * step) {
                    canContinue = false;
                } else {
                    final double smallX = Math.min(lastDashLoc.getX(), pathElm.getX());
                    final double smallY = Math.min(lastDashLoc.getY(), pathElm.getY());
                    final double smallZ = Math.min(lastDashLoc.getZ(), pathElm.getZ());
                    final double bigX = Math.max(lastDashLoc.getX(), pathElm.getX());
                    final double bigY = Math.max(lastDashLoc.getY(), pathElm.getY());
                    final double bigZ = Math.max(lastDashLoc.getZ(), pathElm.getZ());
                    if (!checkPositionValidity(smallX, smallY, smallZ, bigX, bigY, bigZ)) {
                        canContinue = false;
                    }
                }

                if (!canContinue) {
                    path.add(lastLoc.addVector(0.5, 0, 0.5));
                    lastDashLoc = lastLoc;
                }
            }
            lastLoc = pathElm;
            i++;
        }

        if (exact) {
            path.add(to);
        }

        return path;
    }

    private boolean canPassThroughMaterial(final Block block) {
        final Material material = block.getDefaultState().getMaterial();
        return material == Material.AIR
                || material == Material.PLANTS
                || material == Material.ORGANIC
                || block == Blocks.LADDER
                || block == Blocks.WATER
                || block instanceof AbstractSignBlock;
    }

    public static boolean checkPositionValidity(final double smallX, final double smallY, final double smallZ, final double bigX, final double bigY, final double bigZ) {
        for (double x = smallX; x <= bigX; x++) {
            for (double y = smallY; y <= bigY; y++) {
                for (double z = smallZ; z <= bigZ; z++) {
                    if (!checkBlockValidity(x, y, z)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean checkBlockValidity(final double x, final double y, final double z) {
        final BlockPos blockPos = new BlockPos(x, y, z);
        return !PlayerUtil.block(blockPos).getDefaultState().isSolid();
    }
}
