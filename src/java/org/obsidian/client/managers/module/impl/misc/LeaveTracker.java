package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.EntityRemoveEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.Instance;

import java.text.DecimalFormat;

/**
 * Tracks when players leave the game and logs their last known position.
 * Useful for monitoring player movements in multiplayer environments.
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "LeaveTracker", category = Category.MISC)
public class LeaveTracker extends Module {

    // Constants
    private static final double MAX_TRACKING_DISTANCE = 100.0;
    private static final DecimalFormat COORDINATE_FORMAT = new DecimalFormat("#.#");

    // Settings (could be expanded with configurable options)
    private final boolean showNotifications = true;

    /**
     * Singleton instance getter
     *
     * @return The LeaveTracker instance
     */
    public static LeaveTracker getInstance() {
        return Instance.get(LeaveTracker.class);
    }

    @EventHandler
    public void onEntityRemove(EntityRemoveEvent event) {
        if (!(event.getEntity() instanceof AbstractClientPlayerEntity playerEntity)) {
            return;
        }

        // Skip if it's the local player or too close
        if (playerEntity instanceof ClientPlayerEntity ||
                mc.player == null ||
                mc.player.getDistance(playerEntity) >= MAX_TRACKING_DISTANCE) {

            // Format coordinates with consistent precision
            String formattedX = COORDINATE_FORMAT.format(playerEntity.getPosX());
            String formattedY = COORDINATE_FORMAT.format(playerEntity.getPosY());
            String formattedZ = COORDINATE_FORMAT.format(playerEntity.getPosZ());

            // Get player name
            String playerName = playerEntity.getGameProfile().getName();

            // Send notification
            ChatUtil.addText(String.format("Player %s teleported to coordinates XYZ: %s, %s, %s",
                    playerName, formattedX, formattedY, formattedZ));
        }
    }

    @Override
    public void onEnable() {
        super.onEnable();
        if (showNotifications) {
            ChatUtil.addText("§aLeaveTracker enabled - now tracking player teleportations");
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (showNotifications) {
            ChatUtil.addText("§cLeaveTracker disabled");
        }
    }
}