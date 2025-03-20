package org.obsidian.client.managers.events.other;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.minecraft.network.IPacket;
import org.obsidian.client.api.events.CancellableEvent;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PacketEvent extends CancellableEvent {
    private final Action action;
    private final IPacket<?> packet;

    public boolean isSend() {
        return this.getAction().equals(Action.SEND);
    }

    public boolean isReceive() {
        return this.getAction().equals(Action.RECEIVE);
    }

    public enum Action {
        SEND, RECEIVE
    }
}