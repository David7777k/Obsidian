package org.obsidian.client.utils.player;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import org.obsidian.client.api.interfaces.IMinecraft;
import org.obsidian.common.impl.taskript.Script;

import java.util.List;
import java.util.function.BooleanSupplier;

public class ClickScript extends Script implements IMinecraft {

    public ClickScript() {
        cleanup();
    }

    public void addStep(Slot slot) {
        addStep(slot.delay(), slot.slot(), slot.clickType(), slot.condition());
    }

    public void addStep(Slot slot, boolean condition) {
        addStep(slot.delay(), slot.slot(), slot.clickType(), condition ? slot.condition() : () -> isValidSlot(slot.slot()));
    }

    public void addStep(List<Slot> slots) {
        for (Slot slot : slots) {
            addStep(slot.delay(), slot.slot(), slot.clickType(), slot.condition());
        }
    }

    public void addStep(List<Slot> slots, boolean condition) {
        for (Slot slot : slots) {
            addStep(slot.delay(), slot.slot(), slot.clickType(), condition ? slot.condition() : () -> isValidSlot(slot.slot()));
        }
    }

    public void addStep(int delay, int slot, ClickType clickType) {
        addStep(delay, slot, clickType, () -> isValidSlot(slot));
    }

    public void addStep(int delay, int slot, ClickType clickType, BooleanSupplier condition) {
        super.addStep(delay, () -> clickSlot(slot, clickType), condition);
    }

    public void addStep(int delay, List<Integer> slots, ClickType clickType) {
        for (Integer slot : slots) {
            addStep(delay, slots, clickType, () -> isValidSlot(slot));
        }
    }

    public void addStep(int delay, List<Integer> slots, ClickType clickType, BooleanSupplier condition) {
        for (Integer slot : slots) {
            super.addStep(delay, () -> clickSlot(slot, clickType), condition);
        }
    }

    private void clickSlot(int slot, ClickType clickType) {
        if (mc.player != null && mc.player.container != null && mc.currentScreen instanceof ContainerScreen<?> container) {
            mc.playerController.windowClick(
                    container.getContainer().windowId,
                    slot,
                    0,
                    clickType,
                    mc.player
            );
        }
    }

    private boolean isValidSlot(int slot) {
        return mc.player != null && mc.player.container != null && slot >= 0 && slot < mc.player.container.inventorySlots.size();
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    @Accessors(fluent = true)
    public static class Slot {
        private final int delay;
        private final int slot;
        private final ClickType clickType;
        private final BooleanSupplier condition;
    }
}
