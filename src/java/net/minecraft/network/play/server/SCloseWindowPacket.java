package net.minecraft.network.play.server;

import lombok.Getter;
import net.minecraft.client.network.play.IClientPlayNetHandler;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;

import java.io.IOException;

@Getter
public class SCloseWindowPacket implements IPacket<IClientPlayNetHandler> {
    private int windowId;

    public SCloseWindowPacket() {
    }

    public SCloseWindowPacket(int windowIdIn) {
        this.windowId = windowIdIn;
    }

    /**
     * Passes this Packet on to the NetHandler for processing.
     */
    public void processPacket(IClientPlayNetHandler handler) {
        handler.handleCloseWindow(this);
    }

    /**
     * Reads the raw packet data from the data stream.
     */
    public void readPacketData(PacketBuffer buf) throws IOException {
        this.windowId = buf.readUnsignedByte();
    }

    /**
     * Writes the raw packet data to the data stream.
     */
    public void writePacketData(PacketBuffer buf) throws IOException {
        buf.writeByte(this.windowId);
    }
}
