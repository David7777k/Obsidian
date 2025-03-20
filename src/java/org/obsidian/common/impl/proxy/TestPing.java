package org.obsidian.common.impl.proxy;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.connection.UserConnectionImpl;
import com.viaversion.viaversion.protocol.ProtocolPipelineImpl;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.proxy.Socks4ProxyHandler;
import io.netty.handler.proxy.Socks5ProxyHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.status.IClientStatusNetHandler;
import net.minecraft.network.*;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.status.client.CPingPacket;
import net.minecraft.network.status.client.CServerQueryPacket;
import net.minecraft.network.status.server.SPongPacket;
import net.minecraft.network.status.server.SServerInfoPacket;
import net.minecraft.util.LazyValue;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import org.obsidian.common.impl.viaversion.ViaLoadingBase;
import org.obsidian.common.impl.viaversion.ViaMCP;
import org.obsidian.common.impl.viaversion.netty.handler.ViaDecoder;
import org.obsidian.common.impl.viaversion.netty.handler.ViaEncoder;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class TestPing {
    public String state = "";

    private long pingSentAt;
    private NetworkManager pingDestination = null;
    private Proxy proxy;
    private static final ThreadPoolExecutor EXECUTOR = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());

    public void run(String ip, int port, Proxy proxy) {
        this.proxy = proxy;
        TestPing.EXECUTOR.submit(() -> ping(ip, port));
    }

    private void ping(String ip, int port) {
        state = "Pinging " + ip + "...";
        NetworkManager networkManager;
        try {
            networkManager = createTestNetworkManager(InetAddress.getByName(ip), port);
        } catch (UnknownHostException e) {
            state = TextFormatting.RED + "Can't connect to proxy";
            return;
        } catch (Exception e) {
            state = TextFormatting.RED + "Can't ping " + ip;
            return;
        }
        pingDestination = networkManager;
        networkManager.setNetHandler(new IClientStatusNetHandler() {
            private boolean successful;

            @Override
            public void onDisconnect(ITextComponent reason) {
                pingDestination = null;
                if (!this.successful) {
                    state = TextFormatting.RED + "Can't ping " + ip + ": " + reason.getString();
                }
            }

            @Override
            public NetworkManager getNetworkManager() {
                return networkManager;
            }

            @Override
            public void handleServerInfo(SServerInfoPacket packetIn) {
                pingSentAt = Util.milliTime();
                networkManager.sendPacket(new CPingPacket(pingSentAt));
            }

            @Override
            public void handlePong(SPongPacket packetIn) {
                successful = true;
                pingDestination = null;
                long pingToServer = Util.milliTime() - pingSentAt;
                state = "Ping: " + pingToServer;
                networkManager.closeChannel(new TranslationTextComponent("multiplayer.status.finished"));
            }

        });

        try {
            networkManager.sendPacket(new CHandshakePacket(ip, port, ProtocolType.STATUS));
            networkManager.sendPacket(new CServerQueryPacket());
        } catch (Throwable throwable) {
            state = TextFormatting.RED + "Can't ping " + ip;
        }
    }

    private NetworkManager createTestNetworkManager(InetAddress address, int port) {
        final NetworkManager networkManager = new NetworkManager(PacketDirection.CLIENTBOUND);

        Class<? extends SocketChannel> oclass;
        LazyValue<? extends EventLoopGroup> lazyvalue;

        if (Epoll.isAvailable() && Minecraft.getInstance().gameSettings.isUsingNativeTransport()) {
            oclass = EpollSocketChannel.class;
            lazyvalue = NetworkManager.CLIENT_EPOLL_EVENTLOOP;
        } else {
            oclass = NioSocketChannel.class;
            lazyvalue = NetworkManager.CLIENT_NIO_EVENTLOOP;
        }

        (new Bootstrap()).group(lazyvalue.getValue()).handler(new ChannelInitializer<>() {
            protected void initChannel(Channel channel) {
                try {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                } catch (ChannelException ignored) {
                }

                channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30))
                        .addLast("splitter", new NettyVarint21FrameDecoder())
                        .addLast("decoder", new NettyPacketDecoder(PacketDirection.CLIENTBOUND))
                        .addLast("prepender", new NettyVarint21FrameEncoder())
                        .addLast("encoder", new NettyPacketEncoder(PacketDirection.SERVERBOUND))
                        .addLast("packet_handler", networkManager);

                if (channel instanceof SocketChannel && ViaLoadingBase.getInstance().getTargetVersion().getVersion() != ViaMCP.NATIVE_VERSION) {
                    final UserConnection user = new UserConnectionImpl(channel, true);
                    new ProtocolPipelineImpl(user);
                    channel.pipeline().addBefore("encoder", "via-encoder", new ViaEncoder(user));
                    channel.pipeline().addBefore("decoder", "via-decoder", new ViaDecoder(user));
                }

                if (proxy.type == Proxy.ProxyType.SOCKS5) {
                    channel.pipeline().addFirst(new Socks5ProxyHandler(new InetSocketAddress(proxy.getIp(), proxy.getPort()), proxy.username.isEmpty() ? null : proxy.username, proxy.password.isEmpty() ? null : proxy.password));
                } else {
                    channel.pipeline().addFirst(new Socks4ProxyHandler(new InetSocketAddress(proxy.getIp(), proxy.getPort()), proxy.username.isEmpty() ? null : proxy.username));
                }
            }
        }).channel(oclass).connect(address, port).syncUninterruptibly();
        return networkManager;
    }

    public void pingPendingNetworks() {
        if (pingDestination != null) {
            if (pingDestination.isChannelOpen()) {
                pingDestination.tick();
            } else {
                pingDestination.handleDisconnection();
            }
        }
    }
}
