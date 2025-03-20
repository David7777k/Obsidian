package org.obsidian.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.OreBlock;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.events.render.Render3DLastEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.impl.client.Theme;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.render.draw.RenderUtil3D;

import java.util.Comparator;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Nuker", category = Category.PLAYER)
public class Nuker extends Module {
    public static Nuker getInstance() {
        return Instance.get(Nuker.class);
    }

    private final SliderSetting radiusXZ = new SliderSetting(this, "Радиус XZ", 3, 1, 5, 1);
    private final SliderSetting radiusY = new SliderSetting(this, "Радиус Y", 1, 1, 6, 1);
    private final BooleanSetting orePriority = new BooleanSetting(this, "Приоритет на руды", true);
    private final BooleanSetting fastBreak = new BooleanSetting(this, "ФастБрик", true);
    private AxisAlignedBB box;
    private BlockPos pos;

    @EventHandler
    public void onRender3D(Render3DLastEvent e) {
        if (pos != null) {
            RenderUtil3D.drawBoundingBox(e.getMatrix(), box, Theme.getInstance().clientColor(), 1.1f, false, false);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        updateNuker();
    }

    public void updateNuker() {
        pos = PlayerUtil.getCube(mc.player.getPosition(), radiusXZ.getValue(), radiusY.getValue()).stream()
                .filter(this::validBlock)
                .sorted(Comparator.comparing(pos -> mc.player.getDistanceSq(Vector3d.copyCentered(pos))))
                .min(Comparator.comparing(pos -> orePriority.getValue() && mc.world.getBlockState(pos).getBlock() instanceof OreBlock ? 0 : 1))
                .orElse(null);

        if (pos != null) {
            box = mc.world.getBlockState(pos).getShape(mc.world, pos).getBoundingBox().offset(pos);
            mc.playerController.onPlayerDamageBlock(pos, Direction.UP);
            mc.player.swingArm(Hand.MAIN_HAND);
        }
    }

    private boolean validBlock(BlockPos pos) {
        BlockState state = mc.world.getBlockState(pos);
        return !state.isAir() && state.getBlock() != Blocks.CAVE_AIR && state.getBlock() != Blocks.WATER && state.getBlock() != Blocks.LAVA && state.getBlock() != Blocks.BEDROCK && state.getBlock() != Blocks.BARRIER;
    }
}
