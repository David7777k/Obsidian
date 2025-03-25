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
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
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
    // Настройка для разлаживания (распределения) предметов вместо их выбрасывания
    private final BooleanSetting arrangeStackedItems = new BooleanSetting(this, "Разлаживать предметы", true);

    // Константы
    private static final long SELL_INTERVAL = 10000L; // 10 секунд
    // Используем 9 слотов хотбара (0–8) для продажи
    private static final int HOTBAR_SLOTS = 9;
    private static final int MENU_TIMEOUT = 5000; // 5 секунд ожидания меню
    private static final int AUCTION_COLLECT_ROWS = 3; // Количество рядов для сбора

    // Планировщик для отложенных команд
    private ScheduledExecutorService scheduler;

    // Состояние автопродажи
    private enum SellState {
        READY,      // Готов к продаже
        SELLING,    // В процессе продажи
        COLLECTING, // Сбор предметов
        WAITING_CONFIRMATION // Ожидание подтверждения продажи
    }

    // Для отслеживания слотов, с которых производится продажа
    private final List<Integer> lastSoldSlots = new ArrayList<>();
    private SellState currentState = SellState.READY;
    private long lastSellTime = 0L;
    private long menuOpenTime = 0L;

    // Флаг, показывающий, что инвентарь уже перераспределён (перемещены предметы в хотбар)
    private boolean inventoryArranged = false;

    // Статистика
    private int totalSoldItems = 0;
    private long totalIncome = 0L;
    private int successfulSells = 0;
    private int failedSells = 0;
    private final Pattern sellConfirmPattern = Pattern.compile("Ваш предмет выставлен на аукцион за ([\\d,]+)");
    private final Pattern sellErrorPattern = Pattern.compile("(Ошибка|Недостаточно|Нельзя)");
    private boolean waitingForSellResponse = false;
    private long sellResponseTimeout = 0L;

    @Override
    public void onEnable() {
        super.onEnable();
        scheduler = Executors.newScheduledThreadPool(1);
        currentState = SellState.READY;
        lastSellTime = 0L;
        lastSoldSlots.clear();
        inventoryArranged = false; // сбрасываем флаг при включении
        ChatUtil.sendMessage("§a=============================================");
        ChatUtil.sendMessage("§a              АвтоСелл включен              ");
        ChatUtil.sendMessage("§a      Цена: §e" + price.getValue() + " §a| Разлаживать предметы: §e" +
                (arrangeStackedItems.getValue() ? "Да" : "Нет"));
        ChatUtil.sendMessage("§a=============================================");
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
        lastSoldSlots.clear();
        displayStatistics();
        ChatUtil.sendMessage("§c=============================================");
        ChatUtil.sendMessage("§c             АвтоСелл выключен              ");
        ChatUtil.sendMessage("§c=============================================");
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        // Если scheduler равен null (например, модуль отключён), прекращаем выполнение
        if (scheduler == null) return;
        if (mc.player == null || mc.world == null) return;

        long currentTime = System.currentTimeMillis();

        // Проверка нахождения на хабе и переход на сервер Анархии при необходимости
        checkHubAndConnect();

        // Проверка таймаута ожидания подтверждения продажи
        if (waitingForSellResponse && currentTime > sellResponseTimeout) {
            waitingForSellResponse = false;
            failedSells++;
            ChatUtil.sendMessage("§cВремя ожидания подтверждения продажи истекло");
        }

        // Проверка таймаута открытия меню
        if (menuOpenTime > 0 && currentTime - menuOpenTime > MENU_TIMEOUT &&
                (currentState == SellState.COLLECTING || currentState == SellState.WAITING_CONFIRMATION)) {
            currentState = SellState.READY;
            menuOpenTime = 0L;
        }

        // Если игрок вручную открыл контейнер – не запускаем авто-операции
        if (mc.currentScreen instanceof ContainerScreen<?>) {
            processContainerScreens();
            return;
        }

        // Перераспределяем инвентарь: переносим предметы из основной части (слоты 9–35) в хотбар (слоты 0–8)
        if (currentState == SellState.READY && !inventoryArranged) {
            reorderInventoryForSale();
            inventoryArranged = true;
        }

        // Основной цикл автопродажи: если истёк интервал, начинаем продажу
        if (currentTime - lastSellTime >= SELL_INTERVAL && currentState == SellState.READY && !waitingForSellResponse) {
            inventoryArranged = false; // сброс флага для следующего цикла
            startSellProcess();
            lastSellTime = currentTime;
        }
    }

    /**
     * Перемещает saleable предметы из основной части инвентаря (слоты 9–35) в хотбар (слоты 0–8).
     */
    private void reorderInventoryForSale() {
        for (int targetSlot = 0; targetSlot < HOTBAR_SLOTS; targetSlot++) {
            if (mc.player.inventory.getStackInSlot(targetSlot).isEmpty()) {
                // Ищем первый непустой слот в основной части инвентаря
                for (int sourceSlot = HOTBAR_SLOTS; sourceSlot < 36; sourceSlot++) {
                    if (!mc.player.inventory.getStackInSlot(sourceSlot).isEmpty()) {
                        final int finalSourceSlot = sourceSlot;
                        scheduler.schedule(() -> {
                            if (mc.player != null && mc.playerController != null) {
                                mc.playerController.windowClick(0, finalSourceSlot + 36, 0, ClickType.PICKUP, mc.player);
                            }
                        }, 50, TimeUnit.MILLISECONDS);
                        final int finalTargetSlot = targetSlot;
                        scheduler.schedule(() -> {
                            if (mc.player != null && mc.playerController != null) {
                                mc.playerController.windowClick(0, finalTargetSlot, 0, ClickType.PICKUP, mc.player);
                            }
                        }, 100, TimeUnit.MILLISECONDS);
                        scheduler.schedule(() -> clearCursor(), 150, TimeUnit.MILLISECONDS);
                        break;
                    }
                }
            }
        }
    }

    /**
     * Ищет первый пустой слот в основной части инвентаря (слоты 9–35).
     */
    private int findFirstEmptyMainSlot() {
        for (int i = HOTBAR_SLOTS; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty()) return i;
        }
        return -1;
    }

    /**
     * Начинает процесс продажи предметов из хотбара (слоты 0–8).
     */
    private void startSellProcess() {
        currentState = SellState.SELLING;
        lastSoldSlots.clear();

        if (arrangeStackedItems.getValue()) {
            distributeStackedItems();
        }

        // Для каждого непустого слота хотбара отправляем пакет смены активного предмета и команду продажи
        for (int i = 0; i < HOTBAR_SLOTS; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty()) continue;
            lastSoldSlots.add(i);
            final int slot = i;
            scheduler.schedule(() -> {
                if (mc.player != null) {
                    mc.player.connection.sendPacket(new CHeldItemChangePacket(slot));
                }
            }, 300 * i, TimeUnit.MILLISECONDS);
            scheduler.schedule(() -> {
                if (mc.player != null) {
                    ChatUtil.sendText("/ah sell " + price.getValue());
                    waitingForSellResponse = true;
                    sellResponseTimeout = System.currentTimeMillis() + 3000;
                    currentState = SellState.WAITING_CONFIRMATION;
                }
            }, 300 * i + 200, TimeUnit.MILLISECONDS);
        }

        scheduler.schedule(() -> {
            if (mc.player != null && currentState == SellState.WAITING_CONFIRMATION) {
                waitingForSellResponse = false;
                ChatUtil.sendText("/ah " + mc.session.getUsername());
                menuOpenTime = System.currentTimeMillis();
            }
        }, 300 * HOTBAR_SLOTS + 1000, TimeUnit.MILLISECONDS);
    }

    /**
     * Раскладывает стакающиеся предметы из хотбара: если в стеке больше одной единицы,
     * лишние единицы переносятся в пустые слоты основной части инвентаря (слоты 9–35).
     */
    private void distributeStackedItems() {
        for (int i = 0; i < HOTBAR_SLOTS; i++) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getCount() > 1) {
                List<Integer> emptySlots = findEmptySlots();
                if (emptySlots.isEmpty()) break;
                final int sourceSlot = i;
                int distributeCount = Math.min(stack.getCount() - 1, emptySlots.size());
                for (int j = 0; j < distributeCount; j++) {
                    final int targetSlot = emptySlots.get(j);
                    scheduler.schedule(() -> {
                        if (mc.player != null && mc.playerController != null) {
                            // Забираем одну единицу из слота хотбара
                            mc.playerController.windowClick(0, sourceSlot + 36, 0, ClickType.PICKUP, mc.player);
                            // Перемещаем её в пустой слот основной части (targetSlot + 36)
                            mc.playerController.windowClick(0, targetSlot + 36, 1, ClickType.PICKUP, mc.player);
                            // Возвращаем оставшуюся часть стека обратно в слот хотбара
                            mc.playerController.windowClick(0, sourceSlot + 36, 0, ClickType.PICKUP, mc.player);
                            clearCursor();
                        }
                    }, j * 200L, TimeUnit.MILLISECONDS);
                }
            }
        }
        scheduler.schedule(() -> {
            if (mc.player != null && !mc.player.inventory.getItemStack().isEmpty()) {
                int emptySlot = findFirstEmptyMainSlot();
                if (emptySlot != -1) {
                    mc.playerController.windowClick(0, emptySlot + 36, 0, ClickType.PICKUP, mc.player);
                }
            }
        }, 200 * HOTBAR_SLOTS, TimeUnit.MILLISECONDS);
    }

    /**
     * Находит пустые слоты в основной части инвентаря (слоты 9–35).
     */
    private List<Integer> findEmptySlots() {
        List<Integer> emptySlots = new ArrayList<>();
        for (int i = HOTBAR_SLOTS; i < 36; i++) {
            if (mc.player.inventory.getStackInSlot(i).isEmpty()) {
                emptySlots.add(i);
                if (emptySlots.size() >= 9) break;
            }
        }
        return emptySlots;
    }

    /**
     * Обрабатывает открытые контейнерные экраны для работы с аукционами.
     */
    private void processContainerScreens() {
        if (!(mc.currentScreen instanceof ContainerScreen<?> screen)) return;
        Container container = screen.getContainer();
        if (!(container instanceof ChestContainer)) return;
        String title = (screen.getTitle() != null) ? screen.getTitle().getString() : "";
        if (title.contains("Аукционы (" + mc.session.getUsername())) {
            currentState = SellState.COLLECTING;
            collectAuctionItems(container);
        } else if (title.contains("Аукционы") && !title.contains(mc.session.getUsername())) {
            scheduler.schedule(() -> {
                if (mc.player != null && mc.playerController != null && mc.currentScreen instanceof ContainerScreen<?>) {
                    mc.playerController.windowClick(container.windowId, 46, 0, ClickType.PICKUP, mc.player);
                }
            }, 300, TimeUnit.MILLISECONDS);
        }
    }

    /**
     * Собирает предметы с аукциона и обрабатывает стакающиеся предметы.
     */
    private void collectAuctionItems(Container container) {
        // Если scheduler равен null, просто выходим
        if (scheduler == null) return;

        int totalSlots = container.inventorySlots.size();
        int playerInventoryStart = totalSlots - 36;

        List<Integer> slotsToCollect = new ArrayList<>();
        Map<Item, List<Integer>> stackableItems = new HashMap<>();

        int rowSize = 9;
        int scanRows = AUCTION_COLLECT_ROWS;

        for (int row = 0; row < scanRows; row++) {
            for (int col = 0; col < rowSize; col++) {
                int slotIndex = row * rowSize + col;
                if (slotIndex < playerInventoryStart && slotIndex < container.inventorySlots.size() &&
                        container.inventorySlots.get(slotIndex) != null &&
                        container.inventorySlots.get(slotIndex).getHasStack()) {

                    ItemStack itemStack = container.inventorySlots.get(slotIndex).getStack();
                    if (itemStack == null || itemStack.isEmpty()) continue;

                    if (itemStack.isStackable() && itemStack.getMaxStackSize() > 1) {
                        Item item = itemStack.getItem();
                        stackableItems.computeIfAbsent(item, k -> new ArrayList<>()).add(slotIndex);
                    } else {
                        slotsToCollect.add(slotIndex);
                    }
                }
            }
        }

        int delay = 0;
        for (Integer slot : slotsToCollect) {
            final int currentSlot = slot;
            delay += 200;
            scheduler.schedule(() -> {
                if (isValidContainerScreen(container)) {
                    mc.playerController.windowClick(container.windowId, currentSlot, 0, ClickType.QUICK_MOVE, mc.player);
                }
            }, delay, TimeUnit.MILLISECONDS);
        }

        for (Map.Entry<Item, List<Integer>> entry : stackableItems.entrySet()) {
            for (Integer slot : entry.getValue()) {
                final int currentSlot = slot;
                delay += 300;
                scheduler.schedule(() -> {
                    if (!isValidContainerScreen(container)) return;
                    mc.playerController.windowClick(container.windowId, currentSlot, 0, ClickType.PICKUP, mc.player);
                    redistributeStackedItem(container);
                }, delay, TimeUnit.MILLISECONDS);
            }
        }

        scheduler.schedule(() -> {
            if (mc.player != null) {
                mc.player.closeScreen();
                currentState = SellState.READY;
                menuOpenTime = 0L;
                displayStatistics();
            }
        }, delay + 800, TimeUnit.MILLISECONDS);
    }

    /**
     * Раскладывает стакающийся предмет с курсора по разным слотам основной части инвентаря.
     */
    private void redistributeStackedItem(Container container) {
        int totalSlots = container.inventorySlots.size();
        int playerInventoryStart = totalSlots - 36;

        List<Integer> emptySlots = new ArrayList<>();
        for (int i = playerInventoryStart; i < totalSlots - 9; i++) {
            if (container.inventorySlots.get(i) != null && !container.inventorySlots.get(i).getHasStack()) {
                emptySlots.add(i);
            }
        }

        if (emptySlots.isEmpty()) {
            Map<Integer, Integer> slotToItemCount = new HashMap<>();
            Item currentItem = null;
            if (mc.player.inventory.getItemStack() != null && !mc.player.inventory.getItemStack().isEmpty()) {
                currentItem = mc.player.inventory.getItemStack().getItem();
                for (int i = playerInventoryStart; i < totalSlots - 9; i++) {
                    ItemStack slotStack = container.inventorySlots.get(i).getStack();
                    if (!slotStack.isEmpty() && slotStack.getItem() == currentItem) {
                        slotToItemCount.put(i, slotStack.getCount());
                    }
                }
                List<Map.Entry<Integer, Integer>> sortedSlots = new ArrayList<>(slotToItemCount.entrySet());
                sortedSlots.sort(Map.Entry.comparingByValue());
                for (int i = 0; i < Math.min(3, sortedSlots.size()); i++) {
                    emptySlots.add(sortedSlots.get(i).getKey());
                }
            }
        }

        int slotIndex = 0;
        for (Integer targetSlot : emptySlots) {
            final int slot = targetSlot;
            scheduler.schedule(() -> {
                if (isValidContainerScreen(container) && !mc.player.inventory.getItemStack().isEmpty()) {
                    mc.playerController.windowClick(container.windowId, slot, 1, ClickType.PICKUP, mc.player);
                    clearCursor();
                }
            }, 150L * slotIndex, TimeUnit.MILLISECONDS);
            slotIndex++;
            if (slotIndex >= 5) break;
        }

        scheduler.schedule(() -> {
            if (isValidContainerScreen(container) && !mc.player.inventory.getItemStack().isEmpty()) {
                Item cursorItem = mc.player.inventory.getItemStack().getItem();
                boolean placed = false;
                for (int i = playerInventoryStart; i < totalSlots - 9; i++) {
                    ItemStack slotStack = container.inventorySlots.get(i).getStack();
                    if (!slotStack.isEmpty() && slotStack.getItem() == cursorItem &&
                            slotStack.getCount() < slotStack.getMaxStackSize()) {
                        mc.playerController.windowClick(container.windowId, i, 0, ClickType.PICKUP, mc.player);
                        placed = true;
                        break;
                    }
                }
                if (!placed) {
                    List<Integer> freeSlots = findEmptySlots();
                    if (!freeSlots.isEmpty()) {
                        mc.playerController.windowClick(container.windowId, freeSlots.get(0), 0, ClickType.PICKUP, mc.player);
                    }
                }
                clearCursor();
            }
        }, 150L * (slotIndex + 1), TimeUnit.MILLISECONDS);
    }

    /**
     * Проверяет, что контейнерный экран всё ещё валиден.
     */
    private boolean isValidContainerScreen(Container container) {
        return mc.player != null &&
                mc.playerController != null &&
                mc.currentScreen instanceof ContainerScreen<?> &&
                ((ContainerScreen<?>) mc.currentScreen).getContainer() == container;
    }

    /**
     * Проверяет нахождение игрока на хабе и выполняет переход на сервер Анархии при необходимости.
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

        Object unformatted = TextFormatting.getTextWithoutFormattingCodes(headerRaw);
        String header = (unformatted != null) ? unformatted.toString() : headerRaw;

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
     * Извлекает номер сервера Анархии из заголовка таба.
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

    /**
     * Обрабатывает сообщения из чата для отслеживания успешных/неуспешных продаж.
     */
    public void handleChatMessage(String message) {
        if (waitingForSellResponse) {
            Matcher confirmMatcher = sellConfirmPattern.matcher(message);
            if (confirmMatcher.find()) {
                waitingForSellResponse = false;
                successfulSells++;
                totalSoldItems++;
                String priceStr = confirmMatcher.group(1).replace(",", "");
                try {
                    long sellPrice = Long.parseLong(priceStr);
                    totalIncome += sellPrice;
                } catch (NumberFormatException e) {
                    totalIncome += Long.parseLong(price.getValue());
                }
                ChatUtil.sendMessage("§aУспешно выставлен предмет на продажу!");
                return;
            }
            Matcher errorMatcher = sellErrorPattern.matcher(message);
            if (errorMatcher.find()) {
                waitingForSellResponse = false;
                failedSells++;
                ChatUtil.sendMessage("§cОшибка при выставлении предмета на продажу: " + message);
            }
        }
    }

    /**
     * Отображает статистику в чате.
     */
    private void displayStatistics() {
        ChatUtil.sendMessage("§6=============================================");
        ChatUtil.sendMessage("§6               Статистика АвтоСелла          ");
        ChatUtil.sendMessage("§6=============================================");
        ChatUtil.sendMessage("§e Всего продано предметов: §a" + totalSoldItems);
        ChatUtil.sendMessage("§e Успешных продаж: §a" + successfulSells);
        ChatUtil.sendMessage("§e Неудачных продаж: §c" + failedSells);
        ChatUtil.sendMessage("§e Общий доход: §a" + formatNumber(totalIncome) + " монет");
        ChatUtil.sendMessage("§e Средний доход за предмет: §a" +
                (totalSoldItems > 0 ? formatNumber(totalIncome / totalSoldItems) : "0") + " монет");
        ChatUtil.sendMessage("§6=============================================");
    }

    /**
     * Форматирует число с разделителями.
     */
    private String formatNumber(long number) {
        return String.format("%,d", number).replace(",", " ");
    }

    /**
     * Получает имя предмета для отображения.
     */
    private String getItemName(ItemStack stack) {
        if (stack.isEmpty()) return "Пустой слот";
        return stack.getDisplayName().getString();
    }

    /**
     * Если на курсоре находится предмет, пытается вернуть его в первый найденный пустой слот основной части инвентаря.
     */
    private void clearCursor() {
        if (mc.player != null && mc.player.inventory.getItemStack() != null && !mc.player.inventory.getItemStack().isEmpty()) {
            int emptySlot = findFirstEmptyMainSlot();
            if (emptySlot != -1) {
                mc.playerController.windowClick(0, emptySlot + 36, 0, ClickType.PICKUP, mc.player);
            } else {
                ChatUtil.sendMessage("§cНет пустого слота для возврата предмета с курсора!");
            }
        }
    }
}
