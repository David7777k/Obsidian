package org.obsidian.client.managers.component.impl.other;

import com.mojang.authlib.GameProfile;
import lombok.SneakyThrows;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import org.obsidian.client.Obsidian;
import org.obsidian.client.managers.component.Component;
import org.obsidian.common.impl.fastping.InetAddressPatcher;

import java.net.InetAddress;

public final class ConnectionComponent extends Component {
    public static String ip = "localhost";
    public static int port = 0;

    @SneakyThrows
    public static void connectToServer(String ip, int port, GameProfile profile) {
        ConnectionComponent.ip = ip;
        ConnectionComponent.port = port;

        InetAddress inetaddress = InetAddress.getByName(ip);
        inetaddress = InetAddressPatcher.patch(ip, inetaddress);
        NetworkManager networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, mc.gameSettings.isUsingNativeTransport());
        networkManager.setNetHandler(new ClientLoginNetHandler(networkManager, mc, mc.currentScreen, (x) -> Obsidian.log(x.getString())));
        networkManager.sendPacket(new CHandshakePacket(ip, port, ProtocolType.LOGIN));
        networkManager.sendPacket(new CLoginStartPacket(profile));
    }
}