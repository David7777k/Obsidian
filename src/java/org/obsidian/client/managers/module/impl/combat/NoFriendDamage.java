package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.AttackEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.other.Instance;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "NoFriendDamage", category = Category.COMBAT)
public class NoFriendDamage extends Module {
    public static NoFriendDamage getInstance() {
        return Instance.get(NoFriendDamage.class);
    }

    @EventHandler
    public void onEvent(AttackEvent event) {
        if (event.getTarget() instanceof PlayerEntity player && Obsidian.inst().friendManager().isFriend(TextFormatting.removeFormatting(player.getGameProfile().getName()))) {
            event.cancel();
        }
    }
}
