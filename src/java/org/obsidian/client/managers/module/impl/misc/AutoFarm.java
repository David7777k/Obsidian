package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.StringTextComponent;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.component.impl.rotation.Rotation;
import org.obsidian.client.managers.component.impl.rotation.RotationComponent;
import org.obsidian.client.managers.events.player.StopUseItemEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.utils.autobuy.AutoBuyUtils;
import org.obsidian.client.utils.player.InvUtil;
import org.obsidian.client.utils.player.MoveUtil;
import org.obsidian.lib.util.time.StopWatch;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "AutoFarm", category = Category.MISC)
public class AutoFarm extends Module {
    // Constants
    private static final List<Item> HOE_ITEMS = Arrays.asList(Items.NETHERITE_HOE, Items.DIAMOND_HOE);
    private static final List<Item> PLANT_ITEMS = Arrays.asList(Items.CARROT, Items.POTATO);
    private static final int CLOSE_DELAY_MS = 400;
    private static final int ACTION_DELAY_BASE_MS = 500;
    private static final int BUYER_COMMAND_DELAY_MS = 1000;
    private static final double HOE_REPAIR_THRESHOLD = 0.05;
    private static final double EXP_BOTTLE_TO_DAMAGE_RATIO = 6.5;

    // Timers
    private final StopWatch containerCloseTimer = new StopWatch();
    private final StopWatch actionTimer = new StopWatch();
    private final StopWatch statsTimer = new StopWatch();
    private final StopWatch sessionTimer = new StopWatch();

    // State tracking
    private boolean autoRepair;
    private State currentState = State.IDLE;

    // Stats tracking
    private int harvestedItems = 0;
    private int previousHarvestedItems = 0;
    private int soldItems = 0;
    private int totalProfit = 0;
    private int expBottlesBought = 0;
    private int totalExpCost = 0;
    private final Map<Item, Integer> itemCounts = new HashMap<>();
    private double itemsPerMinute = 0;

    // Price estimation for exp bottles
    private final List<Integer> recentExpBottlePrices = new ArrayList<>();
    private int currentMaxExpPrice = 64; // Default max price (coins)

    // Anti-detection
    private int currentActionDelay = ACTION_DELAY_BASE_MS;
    private long lastRandomizationTime = 0;

    private enum State {
        IDLE,
        FARMING,
        REPAIRING,
        SELLING,
        BUYING_EXP
    }

    @Override
    public void onEnable() {
        super.onEnable();
        sessionTimer.reset();
        statsTimer.reset();
        resetState();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        resetState();
    }

    private void resetState() {
        autoRepair = false;
        currentState = State.IDLE;
        containerCloseTimer.reset();
        actionTimer.reset();
    }

    private void randomizeDelays() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastRandomizationTime > 30000) { // Randomize every 30 seconds
            currentActionDelay = ACTION_DELAY_BASE_MS + ThreadLocalRandom.current().nextInt(-100, 200);
            lastRandomizationTime = currentTime;
        }
    }

    @EventHandler
    public void onStopUseItem(StopUseItemEvent e) {
        if (mc.player != null) {
            e.setCancelled(mc.player.getFoodStats().needFood());
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (mc.player == null || mc.world == null || MoveUtil.isMoving() || !containerCloseTimer.finished(CLOSE_DELAY_MS))
            return;

        // Update stats
        updateStats();

        // Randomize delays for anti-detection
        randomizeDelays();

        // Get required slots
        Slot hoeSlot = InvUtil.getInventorySlot(HOE_ITEMS);
        if (hoeSlot == null) return;

        Slot plantSlot = InvUtil.getInventorySlot(PLANT_ITEMS);
        Slot expSlot = InvUtil.getInventorySlot(Items.EXPERIENCE_BOTTLE);
        Slot foodSlot = InvUtil.getFoodMaxSaturationSlot();

        Item mainHandItem = mc.player.getHeldItemMainhand().getItem();
        Item offHandItem = mc.player.getHeldItemOffhand().getItem();

        // Update hoe repair status
        float hoeStrength = getItemStrength(hoeSlot);
        autoRepair = hoeStrength < HOE_REPAIR_THRESHOLD || (hoeStrength != 1 && autoRepair);

        // Set camera angle for farming
        RotationComponent.update(new Rotation(Rotation.cameraYaw(), 90), 360, 360, 0, 5);

        // Handle states
        if (mc.player.getFoodStats().needFood() && foodSlot != null) {
            currentState = State.IDLE;
            handleEating(foodSlot, offHandItem);
        } else if (mc.player.inventory.getFirstEmptyStack() == -1) {
            currentState = State.SELLING;
            handleInventoryFull(plantSlot, offHandItem);
        } else if (autoRepair) {
            currentState = State.REPAIRING;
            handleHoeRepair(hoeSlot, expSlot, mainHandItem, offHandItem);
        } else if (actionTimer.finished(currentActionDelay)) {
            currentState = State.FARMING;
            handleFarming(hoeSlot, plantSlot, mainHandItem, offHandItem);
        }
    }

    private void updateStats() {
        // Update items per minute calculation every 10 seconds
        if (statsTimer.finished(10000)) {
            int itemsDelta = harvestedItems - previousHarvestedItems;
            itemsPerMinute = itemsDelta * 6.0; // Items per 10s * 6 = items per minute
            previousHarvestedItems = harvestedItems;
            statsTimer.reset();

            // Debug info
            if (harvestedItems > 0 && mc.ingameGUI != null) {
                mc.ingameGUI.setOverlayMessage(
                        new StringTextComponent(String.format("§a★ AutoFarm: %.1f items/min | Profit: %d coins | Session: %d min",
                                itemsPerMinute,
                                totalProfit,
                                sessionTimer.elapsedTime() / 60000)),
                        false
                );
            }
        }
    }

    private float getItemStrength(Slot slot) {
        if (slot == null || slot.getStack() == null || slot.getStack().getMaxDamage() == 0)
            return 1.0f;

        return 1 - MathHelper.clamp(
                (float) slot.getStack().getDamage() / (float) slot.getStack().getMaxDamage(),
                0,
                1);
    }

    private void handleEating(Slot foodSlot, Item offHandItem) {
        if (foodSlot == null) return;

        if (!offHandItem.equals(foodSlot.getStack().getItem()) && !isContainerScreenOpen()) {
            InvUtil.swapHand(foodSlot, Hand.OFF_HAND, false);
        } else {
            mc.playerController.processRightClick(mc.player, mc.world, Hand.OFF_HAND);
        }
    }

    private void handleInventoryFull(Slot plantSlot, Item offHandItem) {
        if (plantSlot == null) return;

        if (!PLANT_ITEMS.contains(offHandItem) && !isContainerScreenOpen()) {
            InvUtil.swapHand(plantSlot, Hand.OFF_HAND, false);
        }

        if (mc.currentScreen instanceof ContainerScreen<?> screen) {
            String title = screen.getTitle().getString();
            if (title.equals("● Выберите секцию")) {
                InvUtil.clickSlotId(21, 0, ClickType.PICKUP, true);
                return;
            }
            if (title.equals("Скупщик еды")) {
                int slotId = offHandItem.equals(Items.CARROT) ? 10 : 11;

                // Count items being sold for statistics
                int countBefore = InvUtil.getInventoryCount(offHandItem);
                InvUtil.clickSlotId(slotId, 0, ClickType.PICKUP, true);

                // Try to determine how many items were sold and at what price
                // This is simplified and would need to be enhanced with actual price information
                int soldCount = countBefore - InvUtil.getInventoryCount(offHandItem);
                if (soldCount > 0) {
                    soldItems += soldCount;
                    // Assuming 1 coin per item as a placeholder - actual logic would depend on server economy
                    int profit = soldCount;
                    totalProfit += profit;

                    // Track item-specific counts
                    itemCounts.merge(offHandItem, soldCount, Integer::sum);
                }
                return;
            }
        }

        if (actionTimer.elapsedTime() >= BUYER_COMMAND_DELAY_MS + ThreadLocalRandom.current().nextInt(0, 200)) {
            mc.player.sendChatMessage("/buyer");
            actionTimer.reset();
        }
    }

    private void handleHoeRepair(Slot hoeSlot, Slot expSlot, Item mainHandItem, Item offHandItem) {
        if (hoeSlot == null || expSlot == null) return;

        int expBottleCount = InvUtil.getInventoryCount(Items.EXPERIENCE_BOTTLE);
        int requiredBottles = (int) (hoeSlot.getStack().getDamage() / EXP_BOTTLE_TO_DAMAGE_RATIO);

        if (expBottleCount > requiredBottles) {
            if (isContainerScreenOpen()) return;

            if (!offHandItem.equals(Items.EXPERIENCE_BOTTLE)) {
                InvUtil.swapHand(expSlot, Hand.OFF_HAND, false);
            }
            if (!HOE_ITEMS.contains(mainHandItem)) {
                InvUtil.swapHand(hoeSlot, Hand.MAIN_HAND, false);
            }

            mc.player.connection.sendPacket(new CPlayerTryUseItemPacket(Hand.OFF_HAND));
            actionTimer.reset();
        } else if (actionTimer.finished(BUYER_COMMAND_DELAY_MS)) {
            handleBuyingExperience();
        }
    }

    private void handleBuyingExperience() {
        if (mc.player == null) return;

        if (mc.currentScreen instanceof ContainerScreen<?> screen) {
            String title = screen.getTitle().getString();

            if (title.contains("Пузырек опыта")) {
                findBestExpBottle().ifPresent(slot -> {
                    // Get price before purchasing
                    int price = AutoBuyUtils.getPrice(slot.getStack());
                    int count = slot.getStack().getCount();

                    // Add price to price history for future estimation
                    if (price > 0) {
                        addExpBottlePrice(price / count);
                    }

                    // Purchase
                    InvUtil.clickSlot(slot, 0, ClickType.QUICK_MOVE, true);

                    // Record stats
                    expBottlesBought += count;
                    totalExpCost += price;
                });
                actionTimer.reset();
                return;
            } else if (title.contains("Подозрительная цена")) {
                InvUtil.clickSlotId(0, 0, ClickType.QUICK_MOVE, true);
                actionTimer.reset();
                return;
            }
        }

        if (mc.player != null) {
            mc.player.sendChatMessage("/ah search Пузырёк Опыта");
            actionTimer.reset();
        }
    }

    private Optional<Slot> findBestExpBottle() {
        if (mc.player == null || mc.player.openContainer == null) return Optional.empty();

        return mc.player.openContainer.inventorySlots.stream()
                .filter(s -> s != null && s.getStack() != null && s.getStack().getTag() != null && s.slotNumber < 45)
                .filter(s -> {
                    int price = AutoBuyUtils.getPrice(s.getStack()) / Math.max(1, s.getStack().getCount());
                    return price <= getMaxExpBottlePrice();
                })
                .min(Comparator.comparingInt(s -> AutoBuyUtils.getPrice(s.getStack()) / Math.max(1, s.getStack().getCount())));
    }

    private void addExpBottlePrice(int price) {
        // Add to recent prices list
        recentExpBottlePrices.add(price);

        // Keep only last 10 prices
        if (recentExpBottlePrices.size() > 10) {
            recentExpBottlePrices.remove(0);
        }

        // Update max acceptable price (20% above average)
        if (!recentExpBottlePrices.isEmpty()) {
            double avgPrice = recentExpBottlePrices.stream()
                    .mapToInt(Integer::intValue)
                    .average()
                    .orElse(currentMaxExpPrice);

            currentMaxExpPrice = (int) (avgPrice * 1.2);
        }
    }

    private int getMaxExpBottlePrice() {
        return currentMaxExpPrice;
    }

    private void handleFarming(Slot hoeSlot, Slot plantSlot, Item mainHandItem, Item offHandItem) {
        if (mc.player == null || mc.world == null || hoeSlot == null || plantSlot == null) return;

        BlockPos playerPos = mc.player.getPosition();

        if (mc.world.getBlockState(playerPos).getBlock().equals(Blocks.FARMLAND)) {
            if (HOE_ITEMS.contains(mainHandItem) && PLANT_ITEMS.contains(offHandItem)) {
                performFarmActions(playerPos);
            } else if (!isContainerScreenOpen()) {
                if (!PLANT_ITEMS.contains(offHandItem)) {
                    InvUtil.swapHand(plantSlot, Hand.OFF_HAND, false);
                }
                if (!HOE_ITEMS.contains(mainHandItem)) {
                    InvUtil.swapHand(hoeSlot, Hand.MAIN_HAND, false);
                }
            }
        }
    }

    private void performFarmActions(BlockPos pos) {
        if (mc.player == null || mc.player.connection == null) return;

        // Plant
        mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(
                Hand.OFF_HAND,
                new BlockRayTraceResult(pos.getVec(), Direction.UP, pos, false)
        ));

        // Till multiple times with slight randomized delays to ensure it works
        IntStream.range(0, ThreadLocalRandom.current().nextInt(2, 4)).forEach(i ->
                mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(
                        Hand.MAIN_HAND,
                        new BlockRayTraceResult(pos.getVec(), Direction.UP, pos.up(), false)
                ))
        );

        // Harvest
        mc.player.connection.sendPacket(new CPlayerDiggingPacket(
                CPlayerDiggingPacket.Action.START_DESTROY_BLOCK,
                pos.up(),
                Direction.UP
        ));

        // Update stats (assuming successful harvest on average)
        harvestedItems++;
        actionTimer.reset();
    }

    public boolean isContainerScreenOpen() {
        if (mc.currentScreen instanceof ContainerScreen<?> && mc.player != null) {
            mc.player.closeScreen();
            containerCloseTimer.reset();
            return true;
        }
        return false;
    }

    public String getStatsString() {
        long sessionTimeMinutes = sessionTimer.elapsedTime() / 60000;
        if (sessionTimeMinutes < 1) return "Collecting data...";

        return String.format(
                "Session: %dm | Items: %d (%.1f/min) | Sold: %d | Profit: %d coins | Exp bottles: %d (cost: %d)",
                sessionTimeMinutes,
                harvestedItems,
                itemsPerMinute,
                soldItems,
                totalProfit,
                expBottlesBought,
                totalExpCost
        );
    }

    @Override
    public boolean isStarred() {
        return true;
    
    }
}