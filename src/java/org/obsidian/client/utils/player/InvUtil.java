package org.obsidian.client.utils.player;

import lombok.experimental.UtilityClass;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.AxeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.client.CPlayerPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.potion.Effect;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.Hand;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.client.managers.component.impl.inventory.InvComponent;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.ViaUtil;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.IntStream;

@UtilityClass
public class InvUtil implements IMinecraft {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void moveItem(int from, int to) {
        if (from == to || from == -1)
            return;

        from = from < 9 ? from + 36 : from;
        if (PlayerUtil.isFuntime()) {
            int finalFrom = from;
            InvComponent.addTask(() -> {
                clickSlotId(finalFrom, 0, ClickType.SWAP, false);
                clickSlotId(to, 0, ClickType.SWAP, false);
                clickSlotId(finalFrom, 0, ClickType.SWAP, false);
            }, 0);
        } else {
            clickSlotId(from, 0, ClickType.SWAP, false);
            clickSlotId(to, 0, ClickType.SWAP, false);
            clickSlotId(from, 0, ClickType.SWAP, false);
        }
    }

    public void swapHand(Slot slot, Hand hand, boolean packet) {
        if (slot != null) swapHand(slot.slotNumber, hand, packet);
    }

    public void swapHand(int slotId, Hand hand, boolean packet) {
        if (slotId == -1 || !InvComponent.getInstance().script().isFinished()) return;
        int button = hand.equals(Hand.MAIN_HAND) ? mc.player.inventory.currentItem : 40;

        if (PlayerUtil.isFuntime() && MoveUtil.isMoving())
            InvComponent.addTask(() -> clickSlotId(slotId, button, ClickType.SWAP, packet), 0);
        else clickSlotId(slotId, button, ClickType.SWAP, packet);
    }

    public Slot getSlot(Item item) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().equals(item)).findFirst().orElse(null);
    }

    public Slot getInventorySlot(Item item) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().equals(item) && s.slotNumber >= mc.player.openContainer.inventorySlots.size() - 37).findFirst().orElse(null);
    }

    public Slot getInventorySlot(List<Item> item) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> item.contains(s.getStack().getItem()) && s.slotNumber >= mc.player.openContainer.inventorySlots.size() - 37).findFirst().orElse(null);
    }

    public int getInventoryCount(Item item) {
        return IntStream.range(0, 45).filter(i -> mc.player.inventory.getStackInSlot(i).getItem().equals(item)).map(i -> mc.player.inventory.getStackInSlot(i).getCount()).sum();
    }

    public Slot getFoodMaxSaturationSlot() {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().getFood() != null && !s.getStack().getItem().getFood().canEatWhenFull()).max(Comparator.comparingDouble(s -> s.getStack().getItem().getFood().getSaturation())).orElse(null);
    }

    public Slot getSlot(List<Item> item) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> item.contains(s.getStack().getItem())).findFirst().orElse(null);
    }

    public Slot getSlotPotion(Effect effect) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().equals(Items.POTION) && PotionUtils.getEffectsFromStack(s.getStack()).stream().filter(e -> e.getPotion().equals(effect)).isParallel()).findFirst().orElse(null);
    }

    public int getCount(Item item) {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem().equals(item)).mapToInt(s -> s.getStack().getCount()).sum();
    }

    public int getItemInHotBar(Item item) {
        return IntStream.range(0, 9).filter(i -> mc.player.inventory.getStackInSlot(i).getItem().equals(item)).findFirst().orElse(-1);
    }

    public Slot getAxeSlot() {
        return mc.player.openContainer.inventorySlots.stream().filter(s -> s.getStack().getItem() instanceof AxeItem).findFirst().orElse(null);
    }

    public void findItemAndThrow(Item item, float yaw, float pitch) {
        if (mc.player.getCooldownTracker().hasCooldown(item)) {
            ChatUtil.addTextWithError(item.getName().getString() + " - в кд");
            return;
        }

        Slot slot = InvUtil.getSlot(item);
        if (slot == null) {
            ChatUtil.addTextWithError(item.getName().getString() + " - нету");
            return;
        }

        findItemAndThrow(slot, yaw, pitch);
    }

    public void findItemAndThrow(Slot slot, float yaw, float pitch) {
        if (PlayerUtil.isFuntime()) {
            InvComponent.addTask(() -> {
                swapHand(slot, Hand.MAIN_HAND, false);
                useItem(Hand.MAIN_HAND, yaw, pitch);
                swapHand(slot, Hand.MAIN_HAND, false);
            }, 0, true);
        } else {
            swapHand(slot, Hand.MAIN_HAND, true);
            useItem(Hand.MAIN_HAND, yaw, pitch);
            swapHand(slot, Hand.MAIN_HAND, true);
        }
    }

    public void clickSlot(Slot slot, int button, ClickType clickType, boolean packet) {
        if (slot != null) clickSlotId(slot.slotNumber, button, clickType, packet);
    }

    public void clickSlotId(int slotId, int buttonId, ClickType clickType, boolean packet) {
        clickSlotId(mc.player.openContainer.windowId, slotId, buttonId, clickType, packet);
    }

    public void clickSlotId(int windowId, int slotId, int buttonId, ClickType clickType, boolean packet) {
        if (packet) {
            mc.player.connection.sendPacket(new CClickWindowPacket(windowId, slotId, buttonId, clickType, ItemStack.EMPTY, mc.player.openContainer.getNextTransactionID(mc.player.inventory)));
        } else {
            mc.playerController.windowClick(windowId, slotId, buttonId, clickType, mc.player);
        }
    }

    public void useItem(Hand hand, float yaw, float pitch) {
        if (ViaUtil.allowedBypass()) {
            mc.player.connection.sendPacketWithoutEvent(new CPlayerPacket.PositionRotationPacket(mc.player.getPosX(), mc.player.getPosY(), mc.player.getPosZ(), yaw, pitch, mc.player.isOnGround()));
        }
        mc.player.connection.sendPacketWithoutEvent(new CPlayerTryUseItemPacket(hand));
    }

    public long noEmptyHotBarSlots() {
        return getHotBarSlots().stream().filter(s -> s.getStack().isEmpty()).count();
    }

    public List<Slot> getHotBarSlots() {
        List<Slot> slots = mc.player.openContainer.inventorySlots;
        return new ArrayList<>(slots.stream().filter(i -> i.slotNumber > slots.size() - 10).toList());
    }

    public List<Slot> getMainInventorySlots() {
        List<Slot> list = new ArrayList<>();
        for (int i = mc.player.openContainer.inventorySlots.size() - 37; i < mc.player.openContainer.inventorySlots.size(); i++) {
            list.add(mc.player.openContainer.getSlot(i));
        }
        return list;
    }
}
