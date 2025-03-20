package org.obsidian.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.util.math.AxisAlignedBB;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "Parkour", category = Category.PLAYER)
public class Parkour extends Module {
    public static Parkour getInstance() {
        return Instance.get(Parkour.class);
    }

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (isBlockUnder() && mc.player.isOnGround()) {
            mc.player.jump();
        }
    }

    public boolean isBlockUnder() {
        AxisAlignedBB aab = mc.player.getBoundingBox().offset(0, -0.1, 0);
        return mc.world.getCollisionShapes(mc.player, aab).toList().isEmpty();
    }
}