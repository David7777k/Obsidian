package org.obsidian.client.managers.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.network.play.client.CEntityActionPacket;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.common.impl.taskript.Script;
import org.obsidian.lib.util.time.StopWatch;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Speed", category = Category.MOVEMENT)
public class Speed extends Module {
    private final ModeSetting mode = new ModeSetting(this, "Режим", "FunTime");
    private final StopWatch stopWatch = new StopWatch();
    private final Script script = new Script();

    @Override
    public void onDisable() {

    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (mc.player.isOnGround() && !mc.gameSettings.keyBindJump.isKeyDown()) {
            mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.PRESS_SHIFT_KEY));
        }
    }
}
