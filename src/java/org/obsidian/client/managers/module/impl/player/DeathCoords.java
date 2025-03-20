package org.obsidian.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.DeathEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "DeathCoords", category = Category.PLAYER)
public class DeathCoords extends Module {
    public static DeathCoords getInstance() {
        return Instance.get(DeathCoords.class);
    }

    @EventHandler
    public void onEvent(DeathEvent event) {
        if (mc.player == null) return;
        int x = (int) mc.player.getPosX();
        int y = (int) mc.player.getPosY();
        int z = (int) mc.player.getPosZ();
        ChatUtil.addText("Координаты смерти -> " + TextFormatting.RED + "X: " + x + ", Y: " + y + ", Z: " + z);
    }
}