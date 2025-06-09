package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.annotations.Funtime;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.other.TickEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.lib.util.time.StopWatch;

import java.util.ArrayList;
import java.util.List;

@Funtime
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoEnchant", category = Category.MISC)
public class AutoEnchant extends Module {

    public static AutoEnchant getInstance() {
        return Instance.get(AutoEnchant.class);
    }

    // Константы для слотов зачарования
    static final int ENCHANT_INPUT_SLOT = 0;

    // Настройки модуля
    final SliderSetting delayBetweenActions = new SliderSetting(this, "Задержка между действиями", 250, 100, 1000, 50);

    // Состояние модуля
    final StopWatch actionTimer = new StopWatch();
    boolean isProcessing = false;
    int enchantedItems = 0;
    final List<Integer> itemSlots = new ArrayList<>();

    enum EnchantState {
        IDLE,
        SCANNING,
        MOVE_ITEM_TO_ENCHANT,
        PERFORM_ENCHANT,
        MOVE_ITEM_BACK
    }

    EnchantState currentState = EnchantState.IDLE;
    int selectedSlot = -1;

    @Override
    protected void onEnable() {
        super.onEnable();
        resetState();
        sendMessage(TextFormatting.GREEN + "AutoEnchant запущен");
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        resetState();
        sendMessage(TextFormatting.RED + "AutoEnchant остановлен");
    }

    /**
     * Сбрасывает состояние модуля
     */
    private void resetState() {
        isProcessing = false;
        enchantedItems = 0;
        itemSlots.clear();
        currentState = EnchantState.IDLE;
        selectedSlot = -1;
        actionTimer.reset();
    }

    @EventHandler
    public void onTick(TickEvent event) {
        if (!isEnabled() || !PlayerUtil.isFuntime() || mc.player == null)
            return;

        // Проверяем, что открыт экран зачарования
        if (mc.currentScreen instanceof ContainerScreen &&
                mc.player.openContainer instanceof EnchantmentContainer) {

            if (currentState == EnchantState.IDLE) {
                currentState = EnchantState.SCANNING;
                actionTimer.reset();
            }

            // Ждем задержку между действиями
            if (!actionTimer.elapsed(delayBetweenActions.getValue().longValue())) {
                return;
            }

            // Выполняем текущее состояние
            processCurrentState();
            actionTimer.reset();
        } else if (currentState != EnchantState.IDLE) {
            // Сброс состояния, если экран зачарования закрыт
            resetState();
        }
    }

    /**
     * Обрабатывает текущее состояние автоэнчанта
     */
    private void processCurrentState() {
        EnchantmentContainer container = (EnchantmentContainer) mc.player.openContainer;

        switch (currentState) {
            case SCANNING:
                scanInventory();
                if (itemSlots.isEmpty()) {
                    sendMessage("Не найдено предметов для зачарования");
                    setEnabled(false);
                } else {
                    sendMessage("Найдено предметов для зачарования: " + itemSlots.size());
                    currentState = EnchantState.MOVE_ITEM_TO_ENCHANT;
                }
                break;

            case MOVE_ITEM_TO_ENCHANT:
                if (itemSlots.isEmpty()) {
                    sendMessage("Все предметы зачарованы. Зачаровано: " + enchantedItems);
                    setEnabled(false);
                    return;
                }

                // Проверяем, нет ли уже предмета в слоте зачарования
                if (!container.inventorySlots.get(ENCHANT_INPUT_SLOT).getStack().isEmpty()) {
                    currentState = EnchantState.PERFORM_ENCHANT;
                    return;
                }

                // Перемещаем следующий предмет в слот зачарования
                selectedSlot = itemSlots.remove(0);
                windowClick(selectedSlot, 0, ClickType.PICKUP);
                windowClick(ENCHANT_INPUT_SLOT, 0, ClickType.PICKUP);
                sendMessage("Перемещаем предмет в слот зачарования");
                currentState = EnchantState.PERFORM_ENCHANT;
                break;

            case PERFORM_ENCHANT:
                // Если предмет в слоте зачарования и можно зачаровать
                if (!container.inventorySlots.get(ENCHANT_INPUT_SLOT).getStack().isEmpty()) {
                    if (container.enchantLevels[2] > 0) {
                        // Выбираем 3-й уровень зачарования (максимальный)
                        enchantItem(2);
                        enchantedItems++;
                        sendMessage("Зачарован предмет №" + enchantedItems);
                        currentState = EnchantState.MOVE_ITEM_BACK;
                    } else {
                        // Нельзя зачаровать, возвращаем предмет
                        windowClick(ENCHANT_INPUT_SLOT, 0, ClickType.QUICK_MOVE);
                        sendMessage("Нет доступных зачарований, возвращаем предмет");
                        currentState = EnchantState.MOVE_ITEM_TO_ENCHANT;
                    }
                } else {
                    // Если слот пуст, возможно предмет уже зачарован
                    currentState = EnchantState.MOVE_ITEM_TO_ENCHANT;
                }
                break;

            case MOVE_ITEM_BACK:
                // Быстро перемещаем зачарованный предмет обратно в инвентарь
                windowClick(ENCHANT_INPUT_SLOT, 0, ClickType.QUICK_MOVE);
                currentState = EnchantState.MOVE_ITEM_TO_ENCHANT;
                break;

            default:
                break;
        }
    }

    /**
     * Сканирует инвентарь на наличие предметов для зачарования
     */
    private void scanInventory() {
        itemSlots.clear();

        EnchantmentContainer container = (EnchantmentContainer) mc.player.openContainer;

        // Проверяем слоты инвентаря (27-63)
        for (int i = 27; i < 63; i++) {
            ItemStack stack = container.inventorySlots.get(i).getStack();

            // Пропускаем пустые слоты
            if (stack.isEmpty()) continue;

            // Добавляем предметы для зачарования
            if (stack.isEnchantable()) {
                itemSlots.add(i);
            }
        }
    }

    /**
     * Безопасный клик по слоту в инвентаре
     */
    private void windowClick(int slotId, int button, ClickType type) {
        if (mc.player != null && mc.player.openContainer != null) {
            int windowId = mc.player.openContainer.windowId;
            mc.playerController.windowClick(windowId, slotId, button, type, mc.player);
        }
    }

    /**
     * Безопасное зачарование
     */
    private void enchantItem(int enchantOption) {
        if (mc.player != null && mc.player.openContainer != null) {
            int windowId = mc.player.openContainer.windowId;
            mc.playerController.sendEnchantPacket(windowId, enchantOption);
        }
    }

    /**
     * Отправляет сообщение в чат
     */
    private void sendMessage(String msg) {
        if (mc.player != null) {
            mc.player.sendMessage(
                    new StringTextComponent(TextFormatting.AQUA + "[AutoEnchant] " + TextFormatting.RESET + msg),
                    mc.player.getUUID()
            );
        }
    }
}