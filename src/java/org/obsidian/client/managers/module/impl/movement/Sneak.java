package org.obsidian.client.managers.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.client.CEntityActionPacket;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.MoveEvent;
import org.obsidian.client.managers.events.player.MoveInputEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.lib.util.time.StopWatch;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Sneak", category = Category.MOVEMENT)
public class Sneak extends Module {
    private final BooleanSetting onlySneakPress = new BooleanSetting(this, "Только на шифте", true);

    public static Sneak getInstance() {
        return Instance.get(Sneak.class);
    }

    private final StopWatch time = new StopWatch();


    @EventHandler
    public void onDisable() {
        if (!mc.player.isSneaking()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
        }
    }

    @EventHandler
    public void onMove(MoveEvent e) {
        if (!onlySneakPress.getValue() || mc.gameSettings.keyBindSneak.isKeyDown()) {
            if ((NoSlow.getInstance().isBlockUnderWithMotion() || mc.gameSettings.keyBindJump.isKeyDown()) && mc.player.isOnGround()) {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.RELEASE_SHIFT_KEY));
            } else {
                mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
            }
            mc.player.movementInput.sneaking = true;
        }
    }

    @EventHandler
    public void onMoveInput(MoveInputEvent event) {
        event.setSneakSlow(1);
    }
}
