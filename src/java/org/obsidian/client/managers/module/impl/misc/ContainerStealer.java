package org.obsidian.client.managers.module.impl.misc;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.Blocks;
import net.minecraft.client.gui.ClientBossInfo;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.component.impl.rotation.Rotation;
import org.obsidian.client.managers.component.impl.rotation.RotationComponent;
import org.obsidian.client.managers.events.other.EntityRayTraceEvent;
import org.obsidian.client.managers.events.other.ScreenCloseEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.impl.combat.NoEntityTrace;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.managers.module.settings.impl.ModeSetting;
import org.obsidian.client.managers.module.settings.impl.MultiBooleanSetting;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.client.utils.rotation.RotationUtil;
import org.obsidian.lib.util.time.StopWatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ContainerStealer", category = Category.MISC)
public class ContainerStealer extends Module {
    public static ContainerStealer getInstance() {
        return Instance.get(ContainerStealer.class);
    }

    private final ModeSetting mode = new ModeSetting(this, "Режим", "FunTime", "WhiteList", "Default");
    private final SliderSetting stealDelay = new SliderSetting(this, "Задержка", 100, 0, 1000, 1).setVisible(() -> !mode.is("FunTime"));
    private final BooleanSetting autoOpen = new BooleanSetting(this, "Авто-открытие", true).setVisible(() -> mode.is("FunTime"));
    private final BooleanSetting disableIfEmpty = new BooleanSetting(this, "Закрыть если пустой", true);
    private final MultiBooleanSetting items = new MultiBooleanSetting(this, "Предметы",
            BooleanSetting.of("Элитры", false),
            BooleanSetting.of("Тотем", false),
            BooleanSetting.of("Отмычка", false),
            BooleanSetting.of("Сфера", false),
            BooleanSetting.of("Незер Меч", true),
            BooleanSetting.of("Незер Кирка", true),
            BooleanSetting.of("Незер Шлем", true),
            BooleanSetting.of("Незер Нагрудник", true),
            BooleanSetting.of("Незер Поножи", true),
            BooleanSetting.of("Незер Ботинки", true),
            BooleanSetting.of("Призмариновая Звезда", true),
            BooleanSetting.of("Незер Звезда", true),
            BooleanSetting.of("Чарка", true),
            BooleanSetting.of("Гэпл", true),
            BooleanSetting.of("Зелья", true),
            BooleanSetting.of("Динамит", true),
            BooleanSetting.of("Золотой Шлем", true),
            BooleanSetting.of("Золотая Кирка", true)
    ).setVisible(() -> mode.is("WhiteList"));
    private final StopWatch timeClick = new StopWatch();
    private final StopWatch timeOpen = new StopWatch();
    int containerEmptyTick = 0;

    @EventHandler
    public void onScreenCloseEvent(ScreenCloseEvent event) {
        containerEmptyTick = 0;
        timeOpen.reset();
    }

    @EventHandler
    public void onUpdate(UpdateEvent event) {
        if (mc.currentScreen == null) {
            return;
        }

        if (mode.is("FunTime") && atTheEvent()) {
            if (mc.player.openContainer instanceof ChestContainer container) {
                IInventory inventory = container.getLowerChestInventory();

                containerEmptyTick = inventory.isEmpty() ? 0 : containerEmptyTick + 1;
                if (disableIfEmpty.getValue() && containerEmptyTick > 25) {
                    mc.player.closeScreen();
                    timeOpen.reset();
                    return;
                }

                if (!mc.player.getCooldownTracker().hasCooldown(Items.GUNPOWDER) && timeClick.finished(200)) {
                    for (int i = 0; i < inventory.getSizeInventory(); i++) {
                        if (!container.getSlot(i).getStack().isEmpty()) {
                            mc.playerController.windowClick(container.windowId, i, 0, ClickType.QUICK_MOVE, mc.player);
                            timeClick.reset();
                            break;
                        }
                    }
                }
            } else if (autoOpen.getValue() && timeOpen.finished(5000)) {
                BlockPos enderChestBP = PlayerUtil.getSphere(mc.player.getPosition(), 4, 4, false, true, 0, true).stream()
                        .filter(blockPos -> mc.world.getBlockState(blockPos).getBlock() == Blocks.ENDER_CHEST)
                        .findFirst()
                        .orElse(null);

                if (enderChestBP != null) {
                    float[] rotation = RotationUtil.calculateAngle(Vector3d.copyCentered(enderChestBP).add(new Vector3d(Direction.UP.toVector3f()).mul(0.5)));
                    RotationComponent.update(new Rotation(rotation[0], rotation[1]), 360, 360, 1, 10);
                    mc.playerController.rightClickBlock(mc.player, mc.world, Hand.MAIN_HAND, new BlockRayTraceResult(enderChestBP.getVec(), Direction.UP, enderChestBP, false));
                    mc.player.swingArm(Hand.MAIN_HAND);
                }
            }
        } else if (mode.is("WhiteList") || mode.is("Default")) {
            if (mc.player.openContainer instanceof ChestContainer container) {
                IInventory inventory = container.getLowerChestInventory();
                List<Slot> validSlots = new ArrayList<>();

                for (int i = 0; i < inventory.getSizeInventory(); i++) {
                    Slot slot = container.getSlot(i);
                    if (!slot.getStack().isEmpty() && (!mode.is("WhiteList") || whiteItem(slot.getStack().getItem()))) {
                        validSlots.add(slot);
                    }
                }

                if (validSlots.isEmpty()) {
                    if (disableIfEmpty.getValue()) mc.player.closeScreen();
                } else {
                    int slotNumber = validSlots.get(0).slotNumber;
                    mc.playerController.windowClick(container.windowId, slotNumber, 0, ClickType.QUICK_MOVE, mc.player);
                }
            }
        }
    }

    @EventHandler
    public void onEntityRayTrace(EntityRayTraceEvent event) {
        if (!NoEntityTrace.getInstance().isEnabled()) {
            event.cancel();
        }
    }

    public boolean atTheEvent() {
        for (Map.Entry<UUID, ClientBossInfo> bossInfo : mc.ingameGUI.getBossOverlay().getMapBossInfos().entrySet()) {
            String bossBarName = bossInfo.getValue().getName().getString().toLowerCase();
            if (bossBarName.contains("активации") || bossBarName.contains("закрыт") || bossBarName.contains("открыт")) {
                return true;
            }
        }
        return false;
    }

    public boolean whiteItem(Item item) {
        return (item.equals(Items.ELYTRA) && items.getValue("Элитры")
                || item.equals(Items.TOTEM_OF_UNDYING) && items.getValue("Тотем")
                || item.equals(Items.TRIPWIRE_HOOK) && items.getValue("Отмычка")
                || item.equals(Items.PLAYER_HEAD) && items.getValue("Сфера")
                || item.equals(Items.NETHERITE_SWORD) && items.getValue("Незер Меч")
                || item.equals(Items.NETHERITE_PICKAXE) && items.getValue("Незер Кирка")
                || item.equals(Items.NETHERITE_HELMET) && items.getValue("Незер Шлем")
                || item.equals(Items.NETHERITE_CHESTPLATE) && items.getValue("Незер Нагрудник")
                || item.equals(Items.NETHERITE_LEGGINGS) && items.getValue("Незер Поножи")
                || item.equals(Items.NETHERITE_BOOTS) && items.getValue("Незер Ботинки")
                || item.equals(Items.PRISMARINE_SHARD) && items.getValue("Призмариновая Звезда")
                || item.equals(Items.NETHER_STAR) && items.getValue("Незер Звезда")
                || item.equals(Items.ENCHANTED_GOLDEN_APPLE) && items.getValue("Чарка")
                || item.equals(Items.GOLDEN_APPLE) && items.getValue("Гэпл")
                || ((item.equals(Items.POTION) || item.equals(Items.SPLASH_POTION)) && items.getValue("Зелья"))
                || item.equals(Items.TNT) && items.getValue("Динамит")
                || item.equals(Items.GOLDEN_HELMET) && items.getValue("Золотой Шлем")
                || item.equals(Items.GOLDEN_PICKAXE) && items.getValue("Золотая Кирка"));
    }
}
