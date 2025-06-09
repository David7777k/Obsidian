package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.util.InputMappings;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import org.lwjgl.glfw.GLFW;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.other.Instance;

import java.util.*;

/**
 * Модуль «ChestSorter» с ручным запуском сортировки и улучшениями:
 * • Неблокирующий обмен через состояние (без Thread.sleep)
 * • Улучшенный поиск пустого слота
 * • Корректный переход к следующему слоту после завершения обмена
 */
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ChestSorter", category = Category.MISC)
public class ChestSorter extends Module {
    public static ChestSorter getInstance() {
        return Instance.get(ChestSorter.class);
    }

    private final Minecraft mc = Minecraft.getInstance();

    // Общий таймер между действиями (переключение слотов)
    private static final long ACTION_DELAY_MS = 500;

    // Клавиша для запуска сортировки (R)
    private static final int BIND_KEY_GLFW = GLFW.GLFW_KEY_R;

    // Состояние сортировки
    private boolean sorting = false;
    private boolean keyPressed = false;
    private int currentSlot = 0;
    private final List<ItemInfo> targetLayout = new ArrayList<>();

    // Таймер для общего действия (переход между слотами)
    private final SimpleTimer actionTimer = new SimpleTimer();

    // Таймер для обмена предметов
    private final SimpleTimer swapTimer = new SimpleTimer();

    // Состояние обмена
    private enum SwapState {
        IDLE,
        PICKING_UP,      // Поднимаем предмет из fromSlot
        PLACING,         // Кладём предмет в toSlot
        RETURNING        // Возвращаем «лишние» из fromSlot, если остались
    }

    private SwapState swapState = SwapState.IDLE;
    private int swapFrom = -1;
    private int swapTo = -1;

    // Простая структура для хранения информации о предмете
    private static class ItemInfo {
        final ItemStack stack;
        final String category;
        final String name;

        ItemInfo(ItemStack stack) {
            this.stack = stack.copy();
            this.category = getItemCategory(stack);
            this.name = getItemName(stack);
        }

        private static String getItemCategory(ItemStack stack) {
            if (stack.isEmpty()) return "EMPTY";

            String registryName = Registry.ITEM.getKey(stack.getItem()).toString().toLowerCase();
            String displayName = stack.getDisplayName().getString().toLowerCase();

            // Простая категоризация по ключевым словам
            if (registryName.contains("tnt") || displayName.contains("tnt")) return "EXPLOSIVES";
            if (registryName.contains("_ore") && !registryName.contains("_block")) return "ORES";
            if (registryName.contains("ingot") || registryName.contains("nugget")) return "INGOTS";
            if (registryName.contains("_block") || registryName.contains("planks") || registryName.contains("stone"))
                return "BLOCKS";
            if (registryName.contains("pickaxe") || registryName.contains("shovel") || registryName.contains("axe"))
                return "TOOLS";
            if (registryName.contains("sword") || registryName.contains("bow")) return "WEAPONS";
            if (registryName.contains("helmet") || registryName.contains("chestplate")
                    || registryName.contains("leggings") || registryName.contains("boots")) return "ARMOR";
            if (registryName.contains("apple") || registryName.contains("bread")
                    || registryName.contains("meat") || registryName.contains("beef")) return "FOOD";
            if (registryName.contains("redstone") || registryName.contains("repeater")) return "REDSTONE";
            if (registryName.contains("potion")) return "POTIONS";
            if (registryName.contains("book")) return "BOOKS";
            return "MISC";
        }

        private static String getItemName(ItemStack stack) {
            if (stack.isEmpty()) return "EMPTY";
            return stack.getDisplayName().getString();
        }
    }

    // Простой таймер на основе System.currentTimeMillis()
    private static class SimpleTimer {
        private long lastMS;

        public SimpleTimer() {
            reset();
        }

        public void reset() {
            lastMS = System.currentTimeMillis();
        }

        public boolean hasTimePassed(long ms) {
            return System.currentTimeMillis() - lastMS >= ms;
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        // Если открыт не ChestScreen, то прекращаем сортировку (если она шла)
        if (!(mc.currentScreen instanceof ChestScreen screen)) {
            if (sorting) {
                System.out.println("[ChestSorter] Сундук закрыт, прерываем сортировку");
                resetSorting();
            }
            return;
        }

        // Проверяем нажатие клавиши R
        boolean isRPressed = InputMappings.isKeyDown(mc.getMainWindow().getHandle(), BIND_KEY_GLFW);

        if (isRPressed && !keyPressed && !sorting) {
            keyPressed = true;
            startSorting(screen);
        } else if (!isRPressed) {
            keyPressed = false;
        }

        // Если сейчас идёт обмен, обрабатываем его независимо от основного таймера
        if (swapState != SwapState.IDLE) {
            handleSwap(screen.getContainer());
            return;
        }

        // Если сортировка активна, обрабатываем процесс сортировки
        if (sorting) {
            processSorting(screen);
        }
    }

    /**
     * Запускает сбор предметов и вычисление целевого макета
     */
    private void startSorting(ChestScreen screen) {
        System.out.println("[ChestSorter] ========== ЗАПУСК СОРТИРОВКИ ==========");

        ChestContainer container = screen.getContainer();
        int chestSlotsCount = container.getNumRows() * 9;

        System.out.println("[ChestSorter] Размер сундука: " + chestSlotsCount + " слотов");

        // Собираем все предметы из сундука
        List<ItemInfo> allItems = new ArrayList<>();
        for (int i = 0; i < chestSlotsCount; i++) {
            ItemStack stack = container.getSlot(i).getStack();
            ItemInfo info = new ItemInfo(stack);
            allItems.add(info);

            if (!stack.isEmpty()) {
                System.out.println("[ChestSorter] Слот " + i + ": " + info.name + " (категория: " + info.category + ")");
            }
        }

        // Группируем по категориям
        Map<String, List<ItemInfo>> grouped = new LinkedHashMap<>();
        for (ItemInfo info : allItems) {
            if (!info.stack.isEmpty()) {
                grouped.computeIfAbsent(info.category, k -> new ArrayList<>()).add(info);
            }
        }

        // Создаём целевой макет
        targetLayout.clear();
        String[] categoryOrder = {
                "EXPLOSIVES", "ORES", "INGOTS", "BLOCKS",
                "TOOLS", "WEAPONS", "ARMOR", "FOOD",
                "REDSTONE", "POTIONS", "BOOKS", "MISC"
        };

        for (String category : categoryOrder) {
            List<ItemInfo> items = grouped.get(category);
            if (items != null) {
                items.sort(Comparator.comparing(info -> info.name.toLowerCase()));
                targetLayout.addAll(items);
                System.out.println("[ChestSorter] Категория " + category + ": " + items.size() + " предметов");
            }
        }

        // Дополняем пустыми слотами
        while (targetLayout.size() < chestSlotsCount) {
            targetLayout.add(new ItemInfo(ItemStack.EMPTY));
        }

        currentSlot = 0;
        sorting = true;
        actionTimer.reset();

        int actualItemsCount = (int) targetLayout.stream()
                .filter(info -> !info.stack.isEmpty())
                .count();
        System.out.println("[ChestSorter] Целевой макет создан. Всего предметов для сортировки: " + actualItemsCount);
    }

    /**
     * Основной цикл сортировки: проверяем таймер, текущий/целевой предмет, и запускаем обмен, если нужно
     */
    private void processSorting(ChestScreen screen) {
        ChestContainer container = screen.getContainer();
        int chestSlotsCount = container.getNumRows() * 9;

        // Проверяем, завершена ли сортировка
        if (currentSlot >= chestSlotsCount) {
            System.out.println("[ChestSorter] ========== СОРТИРОВКА ЗАВЕРШЕНА ==========");
            resetSorting();
            return;
        }

        // Ждём задержку между обработкой слотов
        if (!actionTimer.hasTimePassed(ACTION_DELAY_MS)) {
            return;
        }

        ItemStack currentStack = container.getSlot(currentSlot).getStack();
        ItemStack targetStack = targetLayout.get(currentSlot).stack;

        System.out.println("[ChestSorter] Обрабатываем слот " + currentSlot);
        System.out.println("[ChestSorter] Текущий предмет: " +
                (currentStack.isEmpty() ? "ПУСТО" : currentStack.getDisplayName().getString()));
        System.out.println("[ChestSorter] Нужный предмет: " +
                (targetStack.isEmpty() ? "ПУСТО" : targetStack.getDisplayName().getString()));

        // Если предметы совпадают (по типу) — просто переходим к следующему слоту
        if (areItemsEqual(currentStack, targetStack)) {
            System.out.println("[ChestSorter] Слот " + currentSlot + " уже содержит правильный предмет");
            currentSlot++;
            actionTimer.reset();
            return;
        }

        // Ищем слот с нужным предметом (или пустой, если target пуст)
        int sourceSlot = findItemInChest(container, targetStack, chestSlotsCount);

        if (sourceSlot != -1) {
            System.out.println("[ChestSorter] Перемещаем предмет из слота " +
                    sourceSlot + " в слот " + currentSlot);
            startSwap(sourceSlot, currentSlot, container);
            // После запуска обмена ждём завершения в handleSwap
        } else {
            System.out.println("[ChestSorter] ОШИБКА: Не найден предмет для перемещения!");
            currentSlot++;
            actionTimer.reset();
        }
    }

    /**
     * Сравнивает предметы только по типу (Item), без учёта количества/зачарований
     */
    private boolean areItemsEqual(ItemStack stack1, ItemStack stack2) {
        if (stack1.isEmpty() && stack2.isEmpty()) return true;
        if (stack1.isEmpty() || stack2.isEmpty()) return false;
        return stack1.getItem() == stack2.getItem();
    }

    /**
     * Находит слот в сундуке, где лежит нужный предмет (или — если нужен EMPTY — ищет пустой слот).
     * При поиске пустого сначала смотрит вправо, потом — влево.
     */
    private int findItemInChest(ChestContainer container, ItemStack target, int chestSize) {
        if (target.isEmpty()) {
            // Ищем пустой слот после currentSlot
            for (int i = currentSlot + 1; i < chestSize; i++) {
                if (container.getSlot(i).getStack().isEmpty()) {
                    return i;
                }
            }
            // Если не нашли вправо — ищем влево
            for (int i = 0; i < currentSlot; i++) {
                if (container.getSlot(i).getStack().isEmpty()) {
                    return i;
                }
            }
        } else {
            // Ищем нужный предмет (по типу)
            for (int i = 0; i < chestSize; i++) {
                if (i == currentSlot) continue;
                ItemStack stack = container.getSlot(i).getStack();
                if (!stack.isEmpty() && stack.getItem() == target.getItem()) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Запускает процесс обмена: сразу поднимает предмет из fromSlot,
     * а остальные этапы происходят в handleSwap через таймеры.
     */
    private void startSwap(int fromSlot, int toSlot, ChestContainer container) {
        swapFrom = fromSlot;
        swapTo = toSlot;
        swapState = SwapState.PICKING_UP;
        // Поднимаем предмет (ClickType.PICKUP — захват курсором)
        mc.playerController.clickSlot(container.windowId, swapFrom, 0, ClickType.PICKUP, mc.player);
        swapTimer.reset();
    }

    /**
     * Обрабатывает каждый этап обмена, используя swapState и swapTimer.
     * Как только обмен завершён, возвращает swapState в IDLE,
     * и тогда основной процесс увеличивает currentSlot и сбрасывает actionTimer.
     */
    private void handleSwap(ChestContainer container) {
        switch (swapState) {
            case PICKING_UP:
                if (swapTimer.hasTimePassed(50)) {
                    swapState = SwapState.PLACING;
                    swapTimer.reset();
                    // Кладём предмет в целевой слот
                    mc.playerController.clickSlot(container.windowId, swapTo, 0, ClickType.PICKUP, mc.player);
                }
                break;

            case PLACING:
                if (swapTimer.hasTimePassed(50)) {
                    swapState = SwapState.RETURNING;
                    swapTimer.reset();
                    // Возвращаем «лишние» (если они есть) из fromSlot
                    mc.playerController.clickSlot(container.windowId, swapFrom, 0, ClickType.PICKUP, mc.player);
                }
                break;

            case RETURNING:
                if (swapTimer.hasTimePassed(50)) {
                    // Завершаем обмен
                    System.out.println("[ChestSorter] Обмен выполнен: слот " + swapFrom + " -> слот " + swapTo);
                    swapState = SwapState.IDLE;
                    swapFrom = -1;
                    swapTo = -1;
                    // После завершения обмена — переход к следующему слоту
                    currentSlot++;
                    actionTimer.reset();
                }
                break;

            default:
                break;
        }
    }

    /**
     * Сбрасывает всё состояние сортировщика
     */
    private void resetSorting() {
        sorting = false;
        keyPressed = false;
        currentSlot = 0;
        targetLayout.clear();
        swapState = SwapState.IDLE;
        swapFrom = swapTo = -1;
    }

    @Override
    public boolean isStarred() {
        return true;
    }
}
