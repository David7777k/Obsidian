package org.obsidian.client.managers.module.impl.misc;

import com.mojang.datafixers.util.Pair;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.inventory.container.ChestContainer;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.INBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.server.*;
import net.minecraft.scoreboard.Score;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.annotations.Funtime;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.events.input.KeyboardPressEvent;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.other.ScoreBoardEvent;
import org.obsidian.client.managers.events.player.MoveInputEvent;
import org.obsidian.client.managers.events.render.Render2DEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.managers.module.settings.impl.SliderSetting;
import org.obsidian.client.utils.autobuy.AutoBuyUtils;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.MoveUtil;
import org.obsidian.client.utils.player.PlayerUtil;
import org.obsidian.common.impl.taskript.Script;
import org.obsidian.common.impl.taskript.ScriptManager;
import org.obsidian.lib.util.time.StopWatch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;

@Funtime
@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PUBLIC)
@ModuleInfo(name = "AutoBuy", category = Category.PLAYER)
public class AutoBuy extends Module {
    public static AutoBuy getInstance() {
        return Instance.get(AutoBuy.class);
    }

    private final SliderSetting updateDelay = new SliderSetting(this, "Задержка обновления", 500, 0, 1000, 10);
    private final StopWatch lastUpdateTime = new StopWatch();
    private final ScriptManager scripts = new ScriptManager();
    private int balance = -1;

    public void save() {
        Obsidian.inst().autoBuyManager().set();
    }

    @Override
    protected void onEnable() {
        super.onEnable();
        cleanupScripts();
        ChatUtil.sendText("/ah");
    }

    @Override
    protected void onDisable() {
        super.onDisable();
        cleanupScripts();
    }

    private void cleanupScripts() {
        scripts.cleanupScript("update-task");
        scripts.cleanupScript("buy-task");
    }

    @Override
    public void toggle() {
        super.toggle();
        Obsidian.inst().autoBuyManager().set();
        lastUpdateTime.reset();
    }

    @EventHandler
    public void onEvent(MoveInputEvent event) {
        if (MoveUtil.isMoving()) return;
        int ticks = mc.player.ticksExisted;

        boolean tick1 = ticks % 1000 == 0;
        boolean tick2 = ticks % 1000 == 1;

        event.setSneaking(tick1);
        event.setJump(tick1);
        if (tick2) {
            event.setSneaking(false);
            event.setJump(false);
        }
    }

    @EventHandler
    public void onEvent(KeyboardPressEvent event) {
        if (event.getScreen() != null) {
            if (event.isKey(this.getKey())) {
                mc.player.closeScreen();
                toggle();
            }
        }
    }

    @EventHandler
    public void onEvent(Render2DEvent event) {
        if (PlayerUtil.isFuntime() && mc.currentScreen instanceof ContainerScreen<?> screen && screen.getContainer() instanceof ChestContainer chest) {
            String title = screen.getTitle().getString();
            boolean isAuction = AutoBuyUtils.isAuction(title);
            boolean isContainer = AutoBuyUtils.isContainerScreen(title);

            scripts.updateScript("update-task");
            scripts.updateScript("buy-task", () -> isAuction);

            if (AutoBuyUtils.isSuspectPriceScreen(title)) {
                mc.player.connection.sendPacket(new CClickWindowPacket(chest.windowId, 10, 0, ClickType.PICKUP, ItemStack.EMPTY, chest.getNextTransactionID(mc.player.inventory)));
                return;
            }

            if (isAuction) {
                scripts.getScript("buy-task").ifPresent(script -> {
                    if (script.isFinished()) {
                        for (Slot slot : screen.getContainer().inventorySlots) {
                            if (AutoBuyUtils.isValid(slot)) {
                                script.cleanup().addStep(0, () -> {
                                    onBuyItem(slot);
                                    mc.player.connection.sendPacket(new CClickWindowPacket(chest.windowId, slot.slotNumber, 0, ClickType.QUICK_MOVE, ItemStack.EMPTY, chest.getNextTransactionID(mc.player.inventory)));
                                }, () -> AutoBuyUtils.isAuction(title));
                                break;
                            }
                        }
                    }
                });
            }
            scripts.getScript("update-task").ifPresent(script -> {
                if (script.isFinished() && scripts.finished("buy-task")) {
                    script.cleanup().addStep(updateDelay.getValue().intValue(), () -> mc.player.connection.sendPacket(new CClickWindowPacket(mc.player.openContainer.windowId, 49, 2, ClickType.CLONE, ItemStack.EMPTY, chest.getNextTransactionID(mc.player.inventory))), () -> isAuction || isContainer);
                }
            });
        }
    }

    private void onBuyItem(Slot slot) {
        ItemStack itemStack = slot.getStack();
        Item item = itemStack.getItem();

        String itemName = item.getTranslationKey();
        String itemDisplayName = itemStack.getDisplayName().getString();
        String seller = AutoBuyUtils.getSeller(itemStack);
        String tag = Optional.ofNullable(itemStack.getTag()).map(INBT::getString).orElse("null_tag");
        int slotNumber = slot.slotNumber;
        int price = AutoBuyUtils.getPrice(itemStack);

        String dataToSave = String.join(",", itemName, itemDisplayName, seller, String.valueOf(slotNumber), String.valueOf(price));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("checked.txt", true))) {
            writer.write(dataToSave);
            writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        IPacket<?> packet = event.getPacket();
        boolean isFuntime = PlayerUtil.isFuntime();

        if (event.isSend() && isFuntime && packet instanceof CChatMessagePacket wrapper && wrapper.getMessage().contains("/ah")) {
            cleanupScripts();
            lastUpdateTime.reset();
        }

        if (event.isReceive() && isFuntime) {
            if (packet instanceof SWindowItemsPacket || packet instanceof SOpenWindowPacket || packet instanceof SCloseWindowPacket) {
                lastUpdateTime.reset();
            }
            if (packet instanceof SChatPacket wrapper) {
                String chatMessage = wrapper.getChatComponent().getString();
                if (chatMessage.contains("[☃] Этот товар уже купили")) {
                    scripts.getScript("update-task").ifPresent(Script::resetTime);
                }
                if (chatMessage.contains("[☃] Вы успешно купили")) {
                    scripts.getScript("buy-task").ifPresent(Script::resetTime);
                }
            }
            if (packet instanceof SPlaySoundEffectPacket wrapper) {
                String path = wrapper.getSound().getName().getPath();
                if (path.equals("entity.ender_eye.launch") || path.equals("ui.button.click") ||
                        path.equals("block.note_block.basedrum") || path.equals("entity.enderman.teleport")) {

                    if (mc.currentScreen instanceof ContainerScreen<?> container &&
                            (AutoBuyUtils.isAuction(container.getTitle().getString()) ||
                                    AutoBuyUtils.isContainerScreen(container.getTitle().getString()))) {

                        event.cancel();
                    }
                }
            }
        }
    }

    @EventHandler
    public void onEvent(ScoreBoardEvent event) {
        if (event.getList().isEmpty()) {
            resetBalance();
            return;
        }
        for (Pair<Score, ITextComponent> pair : event.getList()) {
            String component = TextFormatting.removeFormatting(pair.getSecond().getString());
            if (component.contains("Монет:")) {
                String[] splitted = component.split(":");
                if (splitted.length > 1) {
                    try {
                        this.balance = Integer.parseInt(splitted[1].trim());
                        break;
                    } catch (NumberFormatException ignored) {
                        resetBalance();
                    }
                } else {
                    resetBalance();
                }
            }
        }
    }

    private void resetBalance() {
        balance = -1;
    }
}