package org.obsidian.client.managers.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.item.UseAction;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.AxisAlignedBB;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.SlowWalkingEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.lib.util.time.StopWatch;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoSlow", category = Category.MOVEMENT)
public class NoSlow extends Module {
    public static NoSlow getInstance() {
        return Instance.get(NoSlow.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Mode", "Grim", "ReallyWorld");
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final StopWatch stopWatch = new StopWatch();

    @EventHandler
    public void onSlowWalk(SlowWalkingEvent e) {
        if (mc.player == null || mc.player.isElytraFlying()) return;

        switch (mode.getValue()) {
            case "Grim" -> {
                if (mc.player.getHeldItemOffhand().getUseAction() == UseAction.BLOCK && mc.player.getActiveHand() == Hand.MAIN_HAND || mc.player.getHeldItemOffhand().getUseAction() == UseAction.EAT && mc.player.getActiveHand() == Hand.MAIN_HAND) {
                    return;
                }

                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(mc.player.getActiveHand()));
                mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(mc.player.getActiveHand() == Hand.MAIN_HAND ? Hand.OFF_HAND : Hand.MAIN_HAND));
                e.cancel();
            }
            case "ReallyWorld" -> {
                mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.ABORT_DESTROY_BLOCK, mc.player.getPosition().up(), Direction.NORTH));
                e.cancel();
            }
        }
    }

    public boolean isBlockUnderWithMotion() {
        AxisAlignedBB aab = mc.player.getBoundingBox().offset(mc.player.getMotion().x, -0.1, mc.player.getMotion().z);
        return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
    }
}