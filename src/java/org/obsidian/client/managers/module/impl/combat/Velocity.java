package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.common.impl.taskript.Script;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Velocity", category = Category.COMBAT)
public class Velocity extends Module {
    private final ModeSetting mode = new ModeSetting(this, "Режим", "FunTime", "Default");
    private final Script script = new Script();

    public static Velocity getInstance() {
        return Instance.get(Velocity.class);
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (PlayerUtil.nullCheck()) return;
        final IPacket<?> packet = e.getPacket();

        if (packet instanceof SEntityVelocityPacket wrapper && wrapper.getEntityID() == mc.player.getEntityId()) {
            switch (mode.getValue()) {
                case "FunTime" -> {
                    if (script.isFinished() && !mc.player.isHandActive()) {
                        BlockPos.getAllInBox(mc.player.getBoundingBox().offset(0, -1e-4, 0)).forEach(pos -> {
                            mc.player.connection.sendPacket(new CPlayerDiggingPacket(CPlayerDiggingPacket.Action.STOP_DESTROY_BLOCK, pos, Direction.UP));
                        });
                        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
                        e.cancel();
                        script.cleanup().addTickStep(0, () -> mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY)));
                    }
                }
                case "Default" -> e.cancel();
            }
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        script.update();
    }
}