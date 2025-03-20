package org.obsidian.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockRayTraceResult;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.InvUtil;

import java.util.Comparator;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoTool", category = Category.PLAYER)
public class AutoTool extends Module {
    public static AutoTool getInstance() {
        return Instance.get(AutoTool.class);
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public Slot itemSlot;
    boolean status;

    @Override
    public void toggle() {
        super.toggle();
        status = false;
        itemSlot = null;
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (e.getPacket() instanceof SSetSlotPacket slot && itemSlot != null) {
            int mainHandSlotNumber = mc.player.inventory.currentItem + mc.player.openContainer.inventorySlots.size() - 10;
            int slotNumber = slot.getSlot();
            if (slotNumber == mainHandSlotNumber) {
                itemSlot.getStack().setDamage(slot.getStack().getDamage());
                e.cancel();
            } else if (slotNumber == itemSlot.slotNumber) {
                e.cancel();
            }
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.player.isCreative()) {
            itemSlot = null;
            return;
        }

        boolean mousePress = isMousePressed();
        Slot currentBestSlot = findBestToolSlotInHotBar();
        if (mousePress && itemSlot != null && currentBestSlot != null && itemSlot != currentBestSlot && status) {
            mousePress = false;
        }

        if (mousePress) {
            if (!status && itemSlot == null) {
                itemSlot = findBestToolSlotInHotBar();
                if (itemSlot != null) {
                    InvUtil.swapHand(itemSlot, Hand.MAIN_HAND, true);
                    status = true;
                }
            }
        } else if (status) {
            InvUtil.swapHand(itemSlot, Hand.MAIN_HAND, true);
            scheduler.schedule(() -> itemSlot = null, 300, TimeUnit.MILLISECONDS);
            status = false;
        }
    }

    private Slot findBestToolSlotInHotBar() {
        if (mc.objectMouseOver instanceof BlockRayTraceResult blockRayTraceResult) {
            BlockState state = mc.world.getBlockState(Nuker.getInstance().isEnabled() && Nuker.getInstance().pos() != null ? Nuker.getInstance().pos() : blockRayTraceResult.getPos()).getBlock().getDefaultState();
            return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getDestroySpeed(state) != 1).max(Comparator.comparingDouble(s -> s.getStack().getDestroySpeed(state))).orElse(null);
        }
        return null;
    }

    private boolean isMousePressed() {
        return ((mc.objectMouseOver instanceof BlockRayTraceResult && mc.gameSettings.keyBindAttack.isKeyDown()) || (Nuker.getInstance().pos() != null));
    }
}
