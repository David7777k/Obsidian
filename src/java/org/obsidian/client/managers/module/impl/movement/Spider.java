package org.obsidian.client.managers.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.component.impl.rotation.Rotation;
import org.obsidian.client.managers.component.impl.rotation.RotationComponent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.utils.other.ViaUtil;
import org.obsidian.client.utils.rotation.RotationUtil;
import org.obsidian.common.impl.taskript.Script;

import java.util.List;
import java.util.stream.IntStream;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Spider", category = Category.MOVEMENT)
public class Spider extends Module {
    private final ModeSetting mode = new ModeSetting(this, "Режим", "Blocks");
    private final ModeSetting modeRotate = new ModeSetting(this, "Ротация", "Packet", "Client").setVisible(() -> mode.is("Blocks"));
    private final Script script = new Script();
    int prevSlot = -1;

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        script.update();
        int slotId = findBlockSlotId();
        if (slotId != -1) {
            Hand hand = (mc.player.getHeldItemOffhand().getItem() instanceof BlockItem bi && bi.getBlock().getDefaultState().isSolid()) ? Hand.OFF_HAND : Hand.MAIN_HAND;
            ItemStack itemStack = hand.equals(Hand.OFF_HAND) ? mc.player.getHeldItemOffhand() : mc.player.inventory.getStackInSlot(slotId);
            BlockPos pos = findPos(-1);
            if (canPlace(itemStack) && !pos.equals(BlockPos.ZERO)) {
                Vector3d vec = Vector3d.copyCentered(pos);
                Direction direction = Direction.getFacingFromVector(vec.x - mc.player.getPosX(), 0, vec.z - mc.player.getPosZ());
                float[] rotate = RotationUtil.calculateAngle(vec.subtract(new Vector3d(direction.toVector3f()).mul(0.5)));

                if (hand.equals(Hand.MAIN_HAND)) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(slotId));
                    prevSlot = mc.player.inventory.currentItem;
                    mc.player.inventory.currentItem = slotId;
                }
                if (modeRotate.is("Packet")) {
                    ViaUtil.sendPositionPacket(rotate[0], rotate[1], true);
                } else {
                    RotationComponent.update(new Rotation(rotate[0], rotate[1]), 360, 360, 0, 5);
                }
                if (!mc.player.isSneaking()) {
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
                }
                mc.playerController.rightClickBlock(mc.player, mc.world, hand, new BlockRayTraceResult(vec, direction.getOpposite(), pos, false));
                if (!mc.player.isSneaking()) {
                    mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
                }
                if (modeRotate.is("Packet")) {
                    ViaUtil.sendPositionPacket(mc.player.rotationYaw, mc.player.rotationPitch, true);
                }
                if (hand.equals(Hand.MAIN_HAND)) {
                    script.cleanup().addTickStep(0, () -> {
                        mc.player.connection.sendPacket(new CHeldItemChangePacket(prevSlot));
                        mc.player.inventory.currentItem = prevSlot;
                    }, 10);
                }
            }
        }
    }

    private int findBlockSlotId() {
        return IntStream.range(0, 9).filter(i -> mc.player.inventory.getStackInSlot(i).getItem() instanceof BlockItem).findFirst().orElse(-1);
    }

    private boolean canPlace(ItemStack stack) {
        return mc.player.getPosition().getY() + mc.player.motion.y < mc.player.getPosY() && mc.world.getBlockState(new BlockPos(mc.player.getPosition().getVec().add(0, -0.01f, 0))).getBlock().canSpawnInBlock();
    }

    private BlockPos findPos(int yOffset) {
        BlockPos blockPos = mc.player.getPosition().add(0, yOffset, 0);
        List<BlockPos> list = List.of(blockPos.west(), blockPos.east(), blockPos.south(), blockPos.north());
        return list.stream().filter(pos -> !mc.world.getBlockState(pos).isAir()).findFirst().orElse(BlockPos.ZERO);
    }
}
