package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.EnchantmentScreen;
import net.minecraft.inventory.container.EnchantmentContainer;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.utils.other.Instance;

import java.util.HashSet;
import java.util.Set;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoEnchant", category = Category.PLAYER)
public class AutoEnchant extends Module {

    final BooleanSetting automaticEnchantingEnabled = new BooleanSetting(this, "AutoOnOpen", true);

    int lastContainerId = -1;
    final Set<Integer> alreadyEnchantedSlots = new HashSet<>();

    public static AutoEnchant getInstance() {
        return Instance.get(AutoEnchant.class);
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (!automaticEnchantingEnabled.getValue()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (!(mc.currentScreen instanceof EnchantmentScreen enchantmentScreen)) {
            return;
        }

        processEnchanting(enchantmentScreen);
    }

    private void processEnchanting(EnchantmentScreen screen) {
        EnchantmentContainer menu = screen.getContainer();

        if (menu.windowId != lastContainerId) {
            lastContainerId = menu.windowId;
            alreadyEnchantedSlots.clear();
        }

        int[] levels = menu.enchantLevels;
        sendEnchantmentPackets(menu.windowId, levels);
    }

    private void sendEnchantmentPackets(int windowId, int[] levels) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.playerController == null) {
            return;
        }

        for (int i = 0; i < levels.length; i++) {
            if (levels[i] <= 0 || !alreadyEnchantedSlots.add(i)) {
                continue;
            }
            mc.playerController.sendEnchantPacket(windowId, i);
        }
    }
}
