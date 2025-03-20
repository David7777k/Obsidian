package org.obsidian.client.managers.module.impl.misc;

import com.google.common.collect.ImmutableList;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.block.EnderChestBlock;
import net.minecraft.client.gui.screen.inventory.ChestScreen;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CCloseWindowPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.input.KeyboardPressEvent;
import org.obsidian.client.managers.events.input.MousePressEvent;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.other.ScreenCloseEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.BindSetting;
import org.obsidian.client.managers.module.settings.impl.BooleanSetting;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.InvUtil;
import org.obsidian.client.utils.player.PlayerUtil;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "EnderChestPlus", category = Category.MISC)
public class EnderChestPlus extends Module {
    public static EnderChestPlus getInstance() {
        return Instance.get(EnderChestPlus.class);
    }

    private final BindSetting key = new BindSetting(this, "Закрыть Эндер-сундук");
    private final BindSetting foldKey = new BindSetting(this, "Сложить ресурсы");
    public final BooleanSetting onlyWhiteList = new BooleanSetting(this, "Только ценные предметы", false);
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    public ContainerScreen<?> containerScreen;

    @Override
    public void toggle() {
        super.toggle();
        closeChest();
    }

    @EventHandler
    public void onMousePress(MousePressEvent e) {
        if (containerScreen == null) return;
        if (e.isKey(key.getValue()) && e.getScreen() == null) closeChest();
        if (e.isKey(foldKey.getValue()) && (e.getScreen() instanceof ChestScreen || e.getScreen() == null)) foldItems();
    }

    @EventHandler
    public void onKeyPress(KeyboardPressEvent e) {
        if (containerScreen == null) return;
        if (e.isKey(key.getValue()) && e.getScreen() == null) closeChest();
        if (e.isKey(foldKey.getValue()) && (e.getScreen() instanceof ChestScreen || e.getScreen() == null)) foldItems();
    }

    @EventHandler
    public void onPacket(PacketEvent e) {
        if (PlayerUtil.nullCheck() || containerScreen == null) return;
        IPacket<?> packet = e.getPacket();

        if (packet instanceof SCloseWindowPacket) {
            containerScreen = null;
        }

        if (packet instanceof CPlayerDiggingPacket digging && digging.getAction().equals(CPlayerDiggingPacket.Action.SWAP_ITEM_WITH_OFFHAND)) {
            int slot = containerScreen.getContainer().getInventory().size() - 9 + mc.player.inventory.currentItem;
            mc.playerController.windowClick(containerScreen.getContainer().windowId, slot, 40, ClickType.SWAP, mc.player);
            e.cancel();
        }

        if (packet instanceof CPlayerTryUseItemOnBlockPacket useItemOnBlock) {
            mc.world.loadedTileEntityList.stream().filter(entity -> entity.getPos().equals(useItemOnBlock.func_218794_c().getPos())).forEach(entity -> {
                scheduler.schedule(() -> mc.player.connection.sendPacketWithoutEvent(useItemOnBlock), 41, TimeUnit.MILLISECONDS);
                closeChest();
                e.cancel();
            });
        }
    }

    @EventHandler
    public void onScreenCloseEvent(ScreenCloseEvent e) {
        if (e.getScreen() instanceof ContainerScreen<?> container && (container.getTitle().getString().contains(EnderChestBlock.CONTAINER_NAME.getString()) || container.getTitle().getString().toLowerCase().contains("эндер-сундук"))) {
            containerScreen = container;
        }
        if (containerScreen != null) {
            mc.displayScreen(null);
            e.cancel();
        }
    }

    public void foldItems() {
        List<Slot> slots = containerScreen.getContainer().inventorySlots;
        slots.stream().filter(s -> s.slotNumber < slots.size() - 36 && s.getStack().isEmpty())
                .findFirst().ifPresent(s -> InvUtil.clickSlot(s, 40, ClickType.SWAP, false));
        slots.stream().filter(s -> s.slotNumber > slots.size() - 36 && !s.getStack().isEmpty() && (whiteItem(s.getStack().getItem()) || !onlyWhiteList.getValue()))
                .forEach(s -> InvUtil.clickSlot(s, 0, ClickType.QUICK_MOVE, false));
    }

    public void closeChest() {
        if (containerScreen != null) {
            mc.player.connection.sendPacket(new CCloseWindowPacket(containerScreen.getContainer().windowId));
            containerScreen = null;
        }
    }

    public boolean whiteItem(Item item) {
        return ImmutableList.of(Items.ELYTRA, Items.TOTEM_OF_UNDYING, Items.TRIPWIRE_HOOK, Items.PLAYER_HEAD, Items.NETHERITE_SWORD, Items.NETHERITE_PICKAXE,
                Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, Items.ENCHANTED_GOLDEN_APPLE, Items.GOLDEN_APPLE,
                Items.PRISMARINE_SHARD, Items.FIREWORK_STAR, Items.NETHER_STAR, Items.GOLDEN_HELMET, Items.GOLDEN_PICKAXE, Items.POTION, Items.TNT).contains(item);
    }
}
