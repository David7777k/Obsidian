package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effect;
import net.minecraft.util.Hand;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.StopUseItemEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.InvUtil;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoHeal", category = Category.COMBAT)
public class AutoHeal extends Module {
    public static AutoHeal getInstance() {
        return Instance.get(AutoHeal.class);
    }

    private final MultiBooleanSetting items = new MultiBooleanSetting(this, "Предметы",
            BooleanSetting.of("Исцеление", false),
            BooleanSetting.of("Чарка", true),
            BooleanSetting.of("Гэпл", false),
            BooleanSetting.of("Еда", false)
    );
    private final SliderSetting healthBaff = new SliderSetting(this, "ХП для Исцеления", 12, 0, 20, 1).setVisible(() -> items.getValue("Исцеление"));
    private final SliderSetting healthGappleEnchant = new SliderSetting(this, "ХП для Чарки", 8, 0, 20, 1).setVisible(() -> items.getValue("Чарка"));
    private final SliderSetting healthGapple = new SliderSetting(this, "ХП для Гэпла", 16, 0, 20, 1).setVisible(() -> items.getValue("Гэпл"));
    private final SliderSetting food = new SliderSetting(this, "Голод для Еды", 18, 0, 20, 1).setVisible(() -> items.getValue("Еда"));
    private Slot lastSwapSlot;
    private Hand lastHand;

    @Override
    public void onDisable() {
        swapBack();
    }

    @EventHandler
    public void onStopUseItemEvent(StopUseItemEvent e) {
        e.setCancelled(lastSwapSlot != null);
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        boolean findPlayer = mc.world.getPlayers().stream().anyMatch(p -> mc.player.getDistance(p) < 6 && p != mc.player);
        boolean hasTotem = InvUtil.getSlot(Items.TOTEM_OF_UNDYING) != null;
        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount() + mc.player.getFoodStats().getSaturationLevel() / 2;
        float foodLvl = mc.player.getFoodStats().getFoodLevel();
        Hand hand = health < 6 && hasTotem ? Hand.MAIN_HAND : Hand.OFF_HAND;

        if (!findPlayer) return;
        if (mc.player.isHandActive() && mc.player.getItemInUseMaxCount() > 10) return;
        if (mc.currentScreen instanceof ContainerScreen<?>) return;
        if (mc.player.isCreative()) return;

        if (items.getValue("Чарка") && !mc.player.getCooldownTracker().hasCooldown(Items.ENCHANTED_GOLDEN_APPLE) && health < healthGappleEnchant.getValue()) {
            swapAndEatItem(InvUtil.getSlot(Items.ENCHANTED_GOLDEN_APPLE), hand);
        } else if (items.getValue("Гэпл") && !mc.player.getCooldownTracker().hasCooldown(Items.GOLDEN_APPLE) && health < healthGapple.getValue()) {
            swapAndEatItem(InvUtil.getSlot(Items.GOLDEN_APPLE), hand);
        } else if (items.getValue("Исцеление") && !mc.player.getCooldownTracker().hasCooldown(Items.POTION) && health < healthBaff.getValue()) {
            swapAndEatItem(InvUtil.getSlotPotion(Effect.get(6)), hand);
        } else if (items.getValue("Еда") && foodLvl < food.getValue()) {
            swapAndEatItem(InvUtil.getFoodMaxSaturationSlot(), hand);
        } else swapBack();
    }

    public void swapAndEatItem(Slot slot, Hand hand) {
        ItemStack stackHand = hand.equals(Hand.MAIN_HAND) ? mc.player.getHeldItemMainhand() : mc.player.getHeldItemOffhand();
        if (!stackHand.equals(slot.getStack())) {
            InvUtil.swapHand(slot, hand, false);
            if (lastSwapSlot == null) {
                lastSwapSlot = slot;
                lastHand = hand;
            }
        } else {
            mc.playerController.processRightClick(mc.player, mc.world, hand);
        }
    }

    public void swapBack() {
        if (lastSwapSlot != null) {
            InvUtil.swapHand(lastSwapSlot, lastHand, false);
            lastSwapSlot = null;
        }
    }
}
