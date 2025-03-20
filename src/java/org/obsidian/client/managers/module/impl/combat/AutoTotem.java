package org.obsidian.client.managers.module.impl.combat;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EnderCrystalEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.item.minecart.TNTMinecartEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.server.SEntityStatusPacket;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.InvUtil;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.lib.util.time.StopWatch;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoTotem", category = Category.COMBAT)
public class AutoTotem extends Module {
    public static AutoTotem getInstance() {
        return Instance.get(AutoTotem.class);
    }

    private final SliderSetting health = new SliderSetting(this, "Уровень здоровья", 4, 4, 20, 1);
    private final BooleanSetting swapBack = new BooleanSetting(this, "Возвращать предмет", true);
    private final BooleanSetting noBallSwitch = new BooleanSetting(this, "Не сменять шар", false);
    private final BooleanSetting saveEnchanted = new BooleanSetting(this, "Сохранять зачарованный", true);
    private final MultiBooleanSetting mode = new MultiBooleanSetting(this, "Проверки на",
            BooleanSetting.of("Золотые сердца", true),
            BooleanSetting.of("Кристаллы", true),
            BooleanSetting.of("Якорь возрождения", true),
            BooleanSetting.of("Падение", true));

    private int nonEnchantedTotems;
    private int oldItem = -1;
    private int totemCount = 0;
    private boolean totemIsUsed;
    public boolean isActive;
    private final StopWatch time = new StopWatch();

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        totemCount = InvUtil.getInventoryCount(Items.TOTEM_OF_UNDYING);
        nonEnchantedTotems = (int) mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().equals(Items.TOTEM_OF_UNDYING) && !s.getStack().isEnchanted()).count();
        Slot slot = mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().equals(Items.TOTEM_OF_UNDYING) && this.isNotSaveEnchanted(s.getStack())).findFirst().orElse(null);

        if (time.finished(400)) {
            if (shouldToSwapTotem()) {
                if (slot != null && !isTotemInHands()) {
                    if (!mc.player.getHeldItemOffhand().isEmpty() && oldItem == -1) oldItem = slot.slotNumber;
                    InvUtil.swapHand(slot.slotNumber, Hand.OFF_HAND, false);
                    time.reset();
                }
            } else if (oldItem != -1 && swapBack.getValue()) {
                InvUtil.swapHand(oldItem, Hand.OFF_HAND, false);
                oldItem = -1;
                time.reset();
            }
        }
    }

    @EventHandler
    public void onPacket(PacketEvent event) {
        if (event.isReceive()) {
            if (event.getPacket() instanceof SEntityStatusPacket statusPacket && statusPacket.getOpCode() == 35 && statusPacket.getEntity(mc.world) == mc.player) {
                this.totemIsUsed = true;
            }
        }
    }

    private boolean isTotemInHands() {
        Hand[] hands = Hand.values();

        for (Hand hand : hands) {
            ItemStack heldItem = mc.player.getHeldItem(hand);
            if (heldItem.getItem() == Items.TOTEM_OF_UNDYING && this.isNotSaveEnchanted(heldItem)) {
                return true;
            }
        }

        return false;
    }

    private boolean isNotSaveEnchanted(ItemStack itemStack) {
        return !this.saveEnchanted.getValue() || !itemStack.isEnchanted() || this.nonEnchantedTotems <= 0;
    }

    private boolean shouldToSwapTotem() {
        final float absorptionAmount = mc.player.isPotionActive(Effects.ABSORPTION) ? mc.player.getAbsorptionAmount() : 0.0f;
        float currentHealth = mc.player.getHealth();
        if (mode.getValue("Золотые сердца")) {
            currentHealth += absorptionAmount;
        }
        if (!isOffhandItemBall()) {
            if (isInDangerousSituation()) {
                return true;
            }
        }
        return currentHealth <= this.health.getValue();
    }

    private boolean isInDangerousSituation() {
        return checkCrystal() || checkAnchor() || checkFall();
    }

    private boolean checkFall() {
        if (!this.mode.getValue("Падение")) {
            return false;
        }
        if (mc.player.isInWater()) {
            return false;
        }
        if (mc.player.isElytraFlying()) {
            return false;
        }
        return mc.player.fallDistance > 10.0f;
    }


    private boolean checkAnchor() {
        if (!mode.getValue("Якорь возрождения")) return false;
        return PlayerUtil.getBlock(6.0F, Blocks.RESPAWN_ANCHOR) != null;
    }

    private boolean checkCrystal() {
        if (!mode.getValue("Кристаллы")) {
            return false;
        }
        for (Entity entity : mc.world.getAllEntities()) {
            if (isDangerousEntityNearPlayer(entity)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOffhandItemBall() {
        boolean isFallingConditionMet = this.mode.getValue("Падение") && mc.player.fallDistance > 5.0f;
        if (isFallingConditionMet) {
            return false;
        }
        return this.noBallSwitch.getValue() && mc.player.getHeldItemOffhand().getItem() == Items.PLAYER_HEAD;
    }

    private boolean isDangerousEntityNearPlayer(Entity entity) {
        return (entity instanceof TNTEntity || entity instanceof TNTMinecartEntity || entity instanceof EnderCrystalEntity) && mc.player.getDistance(entity) <= 6.0F;
    }

    public void reset() {
        this.oldItem = -1;
    }

    @Override
    public void toggle() {
        super.toggle();
        reset();
    }
}
