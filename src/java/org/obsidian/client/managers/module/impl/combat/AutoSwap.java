package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.input.KeyboardPressEvent;
import org.obsidian.client.managers.events.input.MousePressEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BindSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.InvUtil;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoSwap", category = Category.COMBAT)
public class AutoSwap extends Module {
    private final BindSetting keyToSwap = new BindSetting(this, "Кнопка");
    private final ModeSetting swapType1 = new ModeSetting(this, "Предмет", "Щит", "Геплы", "Тотем", "Шар").set("Тотем");
    private final ModeSetting swapType2 = new ModeSetting(this, "Свапать на", "Щит", "Геплы", "Тотем", "Шар").set("Геплы");

    public static AutoSwap getInstance() {
        return Instance.get(AutoSwap.class);
    }

    @Override
    public void toggle() {
        super.toggle();
    }

    @EventHandler
    public void onEvent(KeyboardPressEvent e) {
        if (e.isKey(keyToSwap.getValue()) || e.getScreen() != null) handleEvent();
    }

    @EventHandler
    public void onEvent(MousePressEvent e) {
        if (e.isKey(keyToSwap.getValue()) || e.getScreen() != null) handleEvent();
    }

    private void handleEvent() {
        Item offhandItem = mc.player.getHeldItemOffhand().getItem();
        Slot first = InvUtil.getSlot(getItemByType(swapType1.getValue()));
        Slot second = InvUtil.getSlot(getItemByType(swapType2.getValue()));
        Slot validSlot = offhandItem != first.getStack().getItem() ? first : second;
        InvUtil.swapHand(validSlot, Hand.OFF_HAND, false);
    }

    private Item getItemByType(String itemType) {
        return switch (itemType) {
            case "Щит" -> Items.SHIELD;
            case "Тотем" -> Items.TOTEM_OF_UNDYING;
            case "Геплы" -> Items.GOLDEN_APPLE;
            case "Шар" -> Items.PLAYER_HEAD;
            default -> Items.AIR;
        };
    }
}