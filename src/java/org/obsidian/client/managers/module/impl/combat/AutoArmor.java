package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.IArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.MoveUtil;
import org.obsidian.lib.util.time.StopWatch;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoArmor", category = Category.COMBAT)
public class AutoArmor extends Module {
    public static AutoArmor getInstance() {
        return Instance.get(AutoArmor.class);
    }

    private final SliderSetting delay = new SliderSetting(this, "Задержка", 100, 0, 1000, 1);
    private final StopWatch time = new StopWatch();

    @EventHandler
    public void onEvent(UpdateEvent event) {
        if (mc.currentScreen != null && !(mc.currentScreen instanceof InventoryScreen))
            return;
        if (MoveUtil.isMoving()) {
            return;
        }
        PlayerInventory inventoryPlayer = AutoArmor.mc.player.inventory;
        int[] bestIndexes = new int[4];
        int[] bestValues = new int[4];

        for (int i = 0; i < 4; ++i) {
            bestIndexes[i] = -1;
            ItemStack stack = inventoryPlayer.armorItemInSlot(i);

            if (!isItemValid(stack) || !(stack.getItem() instanceof ArmorItem armorItem)) {
                continue;
            }

            bestValues[i] = calculateArmorValue(armorItem, stack);
        }

        for (int i = 0; i < 36; ++i) {
            Item item;
            ItemStack stack = inventoryPlayer.getStackInSlot(i);

            if (!isItemValid(stack) || !((item = stack.getItem()) instanceof ArmorItem)) continue;

            ArmorItem armorItem = (ArmorItem) item;
            int armorTypeIndex = armorItem.getSlot().getIndex();
            int value = calculateArmorValue(armorItem, stack);

            if (value <= bestValues[armorTypeIndex]) continue;

            bestIndexes[armorTypeIndex] = i;
            bestValues[armorTypeIndex] = value;
        }

        ArrayList<Integer> randomIndexes = new ArrayList<>(Arrays.asList(0, 1, 2, 3));
        Collections.shuffle(randomIndexes);

        for (int index : randomIndexes) {
            int bestIndex = bestIndexes[index];

            if (bestIndex == -1 || (isItemValid(inventoryPlayer.armorItemInSlot(index)) && inventoryPlayer.getFirstEmptyStack() == -1))
                continue;

            if (bestIndex < 9) {
                bestIndex += 36;
            }

            if (!time.finished(delay.getValue().longValue())) break;

            ItemStack armorItemStack = inventoryPlayer.armorItemInSlot(index);

            if (isItemValid(armorItemStack)) {
                AutoArmor.mc.playerController.windowClick(0, 8 - index, 0, ClickType.QUICK_MOVE, AutoArmor.mc.player);
            }

            AutoArmor.mc.playerController.windowClick(0, bestIndex, 0, ClickType.QUICK_MOVE, AutoArmor.mc.player);
            time.reset();
            break;
        }
    }

    private boolean isItemValid(ItemStack stack) {
        return stack != null && !stack.isEmpty();
    }

    private int calculateArmorValue(final ArmorItem armor, final ItemStack stack) {
        final int protectionLevel = EnchantmentHelper.getEnchantmentLevel(Enchantments.PROTECTION, stack);
        final IArmorMaterial armorMaterial = armor.getArmorMaterial();
        final int damageReductionAmount = armorMaterial.getDamageReductionAmount(armor.getEquipmentSlot());
        return ((armor.getDamageReduceAmount() * 20 + protectionLevel * 12 + (int) (armor.getToughness() * 2) + damageReductionAmount * 5) >> 3);
    }
}