package org.obsidian.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.DeathEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoRespawn", category = Category.PLAYER)
public class AutoRespawn extends Module {
    public static AutoRespawn getInstance() {
        return Instance.get(AutoRespawn.class);
    }

    @EventHandler
    public void onEvent(DeathEvent event) {
        if (mc.player == null) return;
        mc.player.respawnPlayer();
        mc.displayScreen(null);
    }
}