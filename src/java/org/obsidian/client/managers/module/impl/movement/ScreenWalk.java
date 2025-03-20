package org.obsidian.client.managers.module.impl.movement;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.screen.EditSignScreen;
import net.minecraft.client.gui.screen.inventory.InventoryScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.client.util.InputMappings;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CClickWindowPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.component.impl.inventory.InvComponent;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.other.ScreenCloseEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import org.obsidian.client.screen.clickgui.ClickGuiScreen;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.client.utils.player.MoveUtil;
import org.obsidian.client.utils.player.PlayerUtil;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "ScreenWalk", category = Category.MOVEMENT)
public class ScreenWalk extends Module {
    public static ScreenWalk getInstance() {
        return Instance.get(ScreenWalk.class);
    }

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final List<IPacket<?>> packets = new CopyOnWriteArrayList<>();

    final KeyBinding[] moveKeys = {
            mc.gameSettings.keyBindForward, mc.gameSettings.keyBindBack,
            mc.gameSettings.keyBindLeft, mc.gameSettings.keyBindRight,
            mc.gameSettings.keyBindJump
    };

    @EventHandler
    public void onPacket(PacketEvent e) {
        final IPacket<?> packet = e.getPacket();
        if (!(mc.currentScreen instanceof InventoryScreen)) return;
        if (packet instanceof CClickWindowPacket wrapper && MoveUtil.isMoving() && PlayerUtil.isFuntime()) {
            packets.add(wrapper);
            e.cancel();
        }

        if (packet instanceof SCloseWindowPacket) {
            e.cancel();
        }
    }

    @EventHandler
    public void onScreenClose(ScreenCloseEvent e) {
        if (e.getScreen() instanceof InventoryScreen && PlayerUtil.isFuntime() && !packets.isEmpty()) {
            InvComponent.addTask(() -> {
                packets.forEach(packet -> mc.player.connection.sendPacket(packet));
                packets.clear();
            }, 50);
        }
    }

    @EventHandler
    public void onUpdate(UpdateEvent e) {
        if (mc.currentScreen == null || mc.currentScreen instanceof ChatScreen || mc.currentScreen instanceof EditSignScreen || (mc.currentScreen instanceof ClickGuiScreen clickGui && clickGui.searchField().isSelected())) {
            return;
        }

        Arrays.stream(moveKeys).forEach(keyBinding -> keyBinding.setPressed(InputMappings.isKeyDown(mw.getHandle(), keyBinding.getDefault().getKeyCode())));
    }
}
