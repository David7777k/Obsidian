package org.obsidian.client.managers.component.impl.inventory;

import net.minecraft.network.IPacket;
import net.minecraft.network.play.server.SHeldItemChangePacket;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.api.events.orbit.EventPriority;
import org.obsidian.client.managers.component.Component;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.managers.events.player.UpdateEvent;
import org.obsidian.client.utils.other.Instance;
import org.obsidian.lib.util.time.StopWatch;

public class HandComponent extends Component {
    public static boolean isEnabled;
    private boolean changingItem;
    private int currentSlot = -1;
    private final StopWatch time = new StopWatch();

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (event.isSend()) {
            return;
        }
        final IPacket<?> packet = event.getPacket();
        if (packet instanceof SHeldItemChangePacket) {
            this.changingItem = true;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEvent(UpdateEvent event) {
        if (this.changingItem && this.currentSlot != -1) {
            isEnabled = true;
            mc.player.inventory.currentItem = this.currentSlot;
            if (time.finished(200)) {
                this.changingItem = false;
                this.currentSlot = -1;
                isEnabled = false;
            }
        }
    }

    public static void reset() {
        Instance.getComponent(HandComponent.class).time.reset();
    }

    public static void setCurrentSlot(int slot) {
        Instance.getComponent(HandComponent.class).currentSlot = slot;
    }
}
