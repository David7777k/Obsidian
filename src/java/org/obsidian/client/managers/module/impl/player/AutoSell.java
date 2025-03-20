package org.obsidian.client.managers.module.impl.player;

import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CHeldItemChangePacket;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.StringSetting;
import org.obsidian.client.utils.chat.ChatUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ModuleInfo(name = "AutoSell", category = org.obsidian.client.managers.module.Category.PLAYER)
public class AutoSell extends Module {

    // Основные настройки
    private final StringSetting price = new StringSetting(this, "Сумма", "5000000");

    // Константы
    private static final long SELL_INTERVAL = 10000L; // 10 секунд
    private static final int MAX_SLOTS = 3; // Количество слотов для продажи
    private static final int MENU_TIMEOUT = 5000; // 5 секунд ожидания меню
    private static final int AUCTION_COLLECT_ROWS = 2; // Количество рядов, содержащих предметы для сбора (обычно 2)

    // Планировщик для отложенных команд
    private ScheduledExecutorService scheduler;

    // Состояние автопродажи
    private enum SellState {
        READY,      // Готов к продаже
        SELLING,    // В процессе продажи
        COLLECTING  // Сбор предметов
    }

    // Для отслеживания позиций слотов выставленных предметов
    private final List<Integer> lastSoldSlots = new ArrayList<>();
    private SellState currentState = SellState.READY;
    private long lastSellTime = 0L;
    private long menuOpenTime = 0L;

    @Override
    public void onEnable() {
        super.onEnable();
        scheduler = Executors.newScheduledThreadPool(1);
        currentState = SellState.READY;
        lastSellTime = 0L;
        lastSoldSlots.clear();
        ChatUtil.sendMessage("§aАвтоСелл включен. Цена: " + price.getValue());
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        lastSoldSlots.clear();
        ChatUtil.sendMessage("§cАвтоСелл выключен.");
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.player == null || mc.world == null) return;

        long currentTime = System.currentTimeMillis();

        // Проверка нахождения на хабе и переход на сервер Анархии при необходимости
        checkHubAndConnect();

        // Проверка таймаута открытия меню
        if (menuOpenTime > 0 && currentTime - menuOpenTime > MENU_TIMEOUT && currentState != SellState.READY) {
            currentState = SellState.READY;
            menuOpenTime = 0L;
        }

        // Основной цикл автопродажи
        if (currentTime - lastSellTime >= SELL_INTERVAL && currentState == SellState.READY) {
            // Если окно не открыто – начинаем продажу
            if (!(mc.currentScreen instanceof ContainerScreen<?>)) {
                startSellProcess();
                lastSellTime = currentTime;
            }
        }

        // Обработка открытых контейнеров
        processContainerScreens();
    }

    /**
     * Начинает процесс продажи предметов и запоминает выставленные слоты
     */
    private void startSellProcess() {
        currentState = SellState.SELLING;
        lastSoldSlots.clear();

        // Продажа предметов из первых слотов
        for (int i = 0; i < MAX_SLOTS; i++) {
            final int slot = i;
            lastSoldSlots.add(slot); // Запоминаем слоты, с которых продали предметы

            // Переключение слота
            scheduler.schedule(() -> {
                if (mc.player != null) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
                }
            }, 200 * i, TimeUnit.MILLISECONDS);

            // Отправка команды продажи
            scheduler.schedule(() -> {
                if (mc.player != null) {
                    ChatUtil.sendText("/ah sell " + price.getValue());
                }
            }, 200 * i + 100, TimeUnit.MILLISECONDS);
        }

        // Открываем аукцион после продажи всех предметов
        scheduler.schedule(() -> {
            if (mc.player != null) {
                ChatUtil.sendText("/ah " + mc.session.getUsername());
                menuOpenTime = System.currentTimeMillis();
            }
        }, 200 * MAX_SLOTS + 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Обрабатывает открытые экраны контейнеров
     */
    private void processContainerScreens() {
        // Если экран не контейнер - выходим
        if (!(mc.currentScreen instanceof ContainerScreen<?> screen)) {
            return;
        }

        Container container = screen.getContainer();

        // Если контейнер не сундук - выходим
        if (!(container instanceof ChestContainer)) {
            return;
        }

        // Безопасно получаем заголовок окна
        String title = "";
        if (screen.getTitle() != null && screen.getTitle().getString() != null) {
            title = screen.getTitle().getString();
        }

        // Обработка меню аукционов игрока
        if (title.contains("Аукционы (" + mc.session.getUsername())) {
            currentState = SellState.COLLECTING;
            collectAuctionItems(container);
        }
        // Обработка общего меню аукционов
        else if (title.contains("Аукционы") && !title.contains(mc.session.getUsername())) {
            // Нажатие на кнопку "Мои аукционы"
            scheduler.schedule(() -> {
                if (mc.player != null && mc.playerController != null && mc.currentScreen instanceof ContainerScreen<?>) {
                    // Слот 46 соответствует кнопке "Мои аукционы"
                    mc.playerController.windowClick(container.windowId, 46, 0, ClickType.PICKUP, mc.player);
                }
            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Собирает только актуальные предметы с аукциона и распределяет стакающиеся предметы
     */
    private void collectAuctionItems(Container container) {
        // Определяем размер контейнера аукциона и наши позиции
        int totalSlots = container.inventorySlots.size();
        int playerInventoryStart = totalSlots - 36; // Начало инвентаря игрока

        // 1. Анализируем содержимое аукциона, чтобы найти предметы для сбора
        List<Integer> slotsToCollect = new ArrayList<>();
        Map<Item, List<Integer>> stackableItems = new HashMap<>();

        // Перебираем верхние строки контейнера, где обычно расположены предметы аукциона
        int rowSize = 9; // Стандартный размер строки инвентаря
        int scanRows = AUCTION_COLLECT_ROWS;

        for (int row = 0; row < scanRows; row++) {
            for (int col = 0; col < rowSize; col++) {
                int slotIndex = row * rowSize + col;

                // Проверяем, что слот существует и содержит предмет
                if (slotIndex < playerInventoryStart && container.inventorySlots.get(slotIndex).getHasStack()) {
                    ItemStack itemStack = container.inventorySlots.get(slotIndex).getStack();

                    // Если предмет стакается, добавляем его в отдельную карту для последующей обработки
                    if (itemStack.isStackable() && itemStack.getMaxStackSize() > 1) {
                        Item item = itemStack.getItem();
                        if (!stackableItems.containsKey(item)) {
                            stackableItems.put(item, new ArrayList<>());
                        }
                        stackableItems.get(item).add(slotIndex);
                    } else {
                        // Нестакающиеся предметы добавляем в список для прямого сбора
                        slotsToCollect.add(slotIndex);
                    }
                }
            }
        }

        // 2. Сначала собираем нестакающиеся предметы
        int delay = 0;
        for (Integer slot : slotsToCollect) {
            final int currentSlot = slot;
            delay += 200;

            scheduler.schedule(() -> {
                if (isValidContainerScreen(container)) {
                    // Забираем предмет быстрым перемещением
                    mc.playerController.windowClick(container.windowId, currentSlot, 0, ClickType.QUICK_MOVE, mc.player);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }

        // 3. Обрабатываем стакающиеся предметы - распределяем по разным слотам инвентаря
        for (Map.Entry<Item, List<Integer>> entry : stackableItems.entrySet()) {
            List<Integer> slots = entry.getValue();

            for (Integer slot : slots) {
                final int currentSlot = slot;
                delay += 300;

                int finalDelay = delay;
                scheduler.schedule(() -> {
                    if (!isValidContainerScreen(container)) return;

                    // Сначала берем предмет на курсор
                    mc.playerController.windowClick(container.windowId, currentSlot, 0, ClickType.PICKUP, mc.player);

                    // Затем распределяем по слотам инвентаря по одному
                    redistributeStackedItem(container, finalDelay);
                }, delay, TimeUnit.MILLISECONDS);
            }
        }

        // 4. Закрываем меню после сбора всех предметов
        scheduler.schedule(() -> {
            if (mc.player != null) {
                mc.player.closeScreen();
                currentState = SellState.READY;
                menuOpenTime = 0L;
            }
        }, delay + 600, TimeUnit.MILLISECONDS);
    }

    /**
     * Распределяет стакающийся предмет с курсора по разным слотам инвентаря
     */
    private void redistributeStackedItem(Container container, int baseDelay) {
        int totalSlots = container.inventorySlots.size();
        int playerInventoryStart = totalSlots - 36; // Начало инвентаря игрока

        // Распределяем предмет по слотам инвентаря, исключая хотбар
        // (последние 9 слотов, чтобы не мешать предметам для продажи)
        for (int i = playerInventoryStart; i < totalSlots - 9; i++) {
            final int targetSlot = i;

            scheduler.schedule(() -> {
                if (isValidContainerScreen(container)) {
                    // Кладем по одному предмету с курсора в слот инвентаря
                    mc.playerController.windowClick(container.windowId, targetSlot, 1, ClickType.PICKUP, mc.player);
                }
            }, 100, TimeUnit.MILLISECONDS);
        }

        // Если что-то осталось на курсоре, кладем остаток обратно
        scheduler.schedule(() -> {
            if (isValidContainerScreen(container) && mc.player.inventory.getItemStack().isEmpty()) {
                mc.playerController.windowClick(container.windowId, -999, 0, ClickType.PICKUP, mc.player);
            }
        }, 200, TimeUnit.MILLISECONDS);
    }

    /**
     * Проверяет, что контейнерный экран все еще валиден
     */
    private boolean isValidContainerScreen(Container container) {
        return mc.player != null &&
                mc.playerController != null &&
                mc.currentScreen instanceof ContainerScreen<?> &&
                ((ContainerScreen<?>) mc.currentScreen).getContainer() == container;
    }

    /**
     * Проверяет нахождение игрока на хабе и переходит на сервер Анархии при необходимости
     */
    private void checkHubAndConnect() {
        if (mc.ingameGUI == null || mc.ingameGUI.getTabList() == null ||
                mc.ingameGUI.getTabList().header == null) {
            return;
        }

        String headerRaw = mc.ingameGUI.getTabList().header.getString();
        if (headerRaw == null) {
            return;
        }

        String header = (String) TextFormatting.getTextWithoutFormattingCodes(headerRaw);
        if (header == null) {
            return;
        }

        if (header.contains("Хаб")) {
            int serverNum = getAnarchyServerNumber(header);
            if (serverNum > 0) {
                scheduler.schedule(() -> {
                    if (mc.player != null) {
                        ChatUtil.sendText("/an" + serverNum);
                    }
                }, 150, TimeUnit.MILLISECONDS);
            }
        }
    }

    /**
     * Извлекает номер сервера Анархии из заголовка таблицы игроков
     * При неудаче возвращает -1
     */
    private int getAnarchyServerNumber(String header) {
        Pattern pattern = Pattern.compile("Анархия-(\\d+)");
        Matcher matcher = pattern.matcher(header);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                return -1;
            }
        }
        return -1;
    }
}