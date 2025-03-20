package org.obsidian.client.managers.component.impl.client;

import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.network.IPacket;
import net.minecraft.network.play.client.CChatMessagePacket;
import net.minecraft.network.play.client.CPlayerTryUseItemPacket;
import net.minecraft.network.play.server.SCloseWindowPacket;
import net.minecraft.network.play.server.SDisconnectPacket;
import net.minecraft.network.play.server.SSpawnParticlePacket;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.events.orbit.EventHandler;
import org.obsidian.client.managers.command.DispatchResult;
import org.obsidian.client.managers.component.Component;
import org.obsidian.client.managers.component.impl.other.ConnectionComponent;
import org.obsidian.client.managers.events.input.ChatInputEvent;
import org.obsidian.client.managers.events.other.DisconnectEvent;
import org.obsidian.client.managers.events.other.PacketEvent;
import org.obsidian.client.utils.chat.ChatUtil;
import org.obsidian.client.utils.other.ViaUtil;
import org.obsidian.common.impl.viaversion.ViaLoadingBase;

public class ClientComponent extends Component {
    @EventHandler
    public void onChat(ChatInputEvent event) {
        if (Obsidian.inst().commandDispatcher().dispatch(event.getMessage()) == DispatchResult.DISPATCHED) {
            event.cancel();
        }
    }

    @EventHandler
    public void onEvent(PacketEvent event) {
        if (mc.player == null) return;
        final IPacket<?> packet = event.getPacket();
        if (packet instanceof SSpawnParticlePacket wrapper) {
            final double distance = mc.player.getDistanceSq(wrapper.getXCoordinate(), wrapper.getYCoordinate(), wrapper.getZCoordinate());
            float maxDistance = 8;
            if (distance > (maxDistance * maxDistance)) {
                event.cancel();
            }
        }
        if (packet instanceof SCloseWindowPacket && mc.currentScreen instanceof ChatScreen) {
            event.cancel();
        }
        if (ViaUtil.allowedBypass() && packet instanceof CPlayerTryUseItemPacket wrapper) {
            ViaUtil.sendPositionPacket();
            mc.player.connection.sendPacketWithoutEvent(wrapper);
            event.cancel();
        }
        if (packet instanceof SDisconnectPacket) {
            new DisconnectEvent().hook();
        }
        if (packet instanceof CChatMessagePacket wrapper) {
            if (wrapper.getMessage().equalsIgnoreCase("/ah me")) {
                ChatUtil.sendText("/ah " + mc.session.getProfile().getName());
                event.cancel();
            }
        }
    }

    @EventHandler
    public void onEvent(DisconnectEvent event) {
        final String ip = ConnectionComponent.ip;
        final ViaLoadingBase via = ViaLoadingBase.getInstance();
        if ((ip.toLowerCase().contains("funtime") || ip.toLowerCase().contains("holyworld")) && via.getTargetVersion().equalTo(ProtocolVersion.v1_17_1)) {
            via.reload(ProtocolVersion.v1_16_4);
        }
    }

}
