package org.obsidian.client.managers.module.impl.player;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CEntityActionPacket;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.input.KeyboardPressEvent;
import org.obsidian.client.managers.events.input.MousePressEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BindSetting;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.player.InvUtil;
import org.obsidian.common.impl.taskript.Script;

import java.util.HashMap;
import java.util.Map;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ElytraHelper", category = Category.PLAYER)
public class ElytraHelper extends Module {
    private final Map<String, BindSetting> keyBindings = new HashMap<>();

    public ElytraHelper() {
        keyBindings.put("chest-swap", new BindSetting(this, "Свапнуть нагрудник"));
        keyBindings.put("firework", new BindSetting(this, "Использовать феерверк"));
    }

    private final BooleanSetting fastFly = new BooleanSetting(this, "Быстрый взлёт", false);
    private final Script script = new Script();

    @EventHandler
    public void onKeyboardPress(KeyboardPressEvent event) {
        if (event.getScreen() != null) return;

        keyBindings.forEach((key, bindSetting) -> {
            if (event.isKey(bindSetting.getValue())) {
                performAction(key);
            }
        });
    }

    @EventHandler
    public void onMousePress(MousePressEvent event) {
        if (event.getScreen() != null) return;

        keyBindings.forEach((key, bindSetting) -> {
            if (event.isKey(bindSetting.getValue())) {
                performAction(key);
            }
        });
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        script.update();
    }

    private void performAction(String action) {
        switch (action) {
            case "chest-swap":
                SwapChest();
                break;
            case "firework":
                if (mc.player.isElytraFlying())
                    InvUtil.findItemAndThrow(Items.FIREWORK_ROCKET, mc.player.rotationYaw, mc.player.rotationPitch);
                break;
        }
    }

    private void SwapChest() {
        boolean elytraOnChest = mc.player.getItemStackFromSlot(EquipmentSlotType.CHEST).getItem().equals(Items.ELYTRA);
        Slot chestPlate = elytraOnChest ? findChestPlate() : InvUtil.getSlot(Items.ELYTRA);

        if (chestPlate != null) {
            InvUtil.moveItem(chestPlate.slotNumber, 6);
            ChatUtil.addText("Свапаю на " + (elytraOnChest ? (TextFormatting.AQUA + "Нагрудник") : (TextFormatting.RED + "Элитры")));

            if (!mc.player.isElytraFlying() && mc.player.inventory.getStackInSlot(38).getItem() != Items.ELYTRA && fastFly.getValue()) {
                Slot firework = InvUtil.getSlot(Items.FIREWORK_ROCKET);
                if (firework != null) {
                    script.cleanup().addTickStep(10, () -> {
                        if (mc.player.isOnGround()) mc.player.jump();
                        mc.player.startFallFlying();
                        mc.player.connection.sendPacket(new CEntityActionPacket(mc.player, CEntityActionPacket.Action.START_FALL_FLYING));
                        InvUtil.findItemAndThrow(Items.FIREWORK_ROCKET, mc.player.rotationYaw, mc.player.rotationPitch);
                    }, 10);
                }
            }
        }
    }

    private Slot findChestPlate() {
        Item[] items = {Items.NETHERITE_CHESTPLATE, Items.DIAMOND_CHESTPLATE, Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE, Items.GOLDEN_CHESTPLATE, Items.LEATHER_CHESTPLATE};
        for (Item item : items) {
            Slot slot = InvUtil.getSlot(item);
            if (slot != null) {
                return slot;
            }
        }
        return null;
    }
}
