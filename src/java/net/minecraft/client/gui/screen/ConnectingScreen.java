package net.minecraft.client.gui.screen;


import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.DialogTexts;
import net.minecraft.client.gui.chat.NarratorChatListener;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.multiplayer.ServerAddress;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.network.login.ClientLoginNetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.ProtocolType;
import net.minecraft.network.handshake.client.CHandshakePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import net.minecraft.util.DefaultUncaughtExceptionHandler;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.mojang.blaze3d.matrix.MatrixStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obsidian.client.managers.component.impl.other.ConnectionComponent;
import org.obsidian.common.impl.fastping.InetAddressPatcher;
import org.obsidian.common.impl.viaversion.ViaLoadingBase;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class ConnectingScreen extends Screen {
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger();
    private NetworkManager networkManager;
    private boolean cancel;
    private final Screen previousGuiScreen;
    private ITextComponent field_209515_s = new TranslationTextComponent("connect.connecting");
    private long field_213000_g = -1L;

    public ConnectingScreen(Screen parent, Minecraft mcIn, ServerData serverDataIn) {
        super(NarratorChatListener.EMPTY);
        this.minecraft = mcIn;
        this.previousGuiScreen = parent;
        ServerAddress serveraddress = ServerAddress.fromString(serverDataIn.serverIP);
        mcIn.unloadWorld();
        mcIn.setServerData(serverDataIn);
        this.connect(serveraddress.getIP(), serveraddress.getPort());
    }

    public ConnectingScreen(Screen parent, Minecraft mcIn, String hostName, int port) {
        super(NarratorChatListener.EMPTY);
        this.minecraft = mcIn;
        this.previousGuiScreen = parent;
        mcIn.unloadWorld();
        this.connect(hostName, port);
    }


    private void connect(final String ip, final int port) {
        final ViaLoadingBase via = ViaLoadingBase.getInstance();
        if ((ip.toLowerCase().contains("funtime") || ip.toLowerCase().contains("holyworld")) && via.getTargetVersion().equalTo(ProtocolVersion.v1_16_4)) {
            via.reload(ProtocolVersion.v1_17_1);
        }
        connectToServer(ip, port);
    }

    public void connectToServer(String ip, int port) {
        Thread thread = new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {
            public void run() {
                InetAddress inetaddress = null;

                try {
                    if (ConnectingScreen.this.cancel) {
                        return;
                    }

                    inetaddress = InetAddress.getByName(ip);

                    inetaddress = InetAddressPatcher.patch(ip, inetaddress);

                    ConnectingScreen.this.networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, ConnectingScreen.this.minecraft.gameSettings.isUsingNativeTransport());
                    ConnectingScreen.this.networkManager.setNetHandler(new ClientLoginNetHandler(ConnectingScreen.this.networkManager, ConnectingScreen.this.minecraft, ConnectingScreen.this.previousGuiScreen, ConnectingScreen.this::func_209514_a));
                    ConnectingScreen.this.networkManager.sendPacket(new CHandshakePacket(ip, port, ProtocolType.LOGIN));
                    ConnectingScreen.this.networkManager.sendPacket(new CLoginStartPacket(ConnectingScreen.this.minecraft.getSession().getProfile()));
                    ConnectionComponent.ip = ip;
                    ConnectionComponent.port = port;
                } catch (UnknownHostException unknownhostexception) {
                    if (ConnectingScreen.this.cancel) {
                        return;
                    }

                    ConnectingScreen.LOGGER.error("Couldn't connect to server", unknownhostexception);
                    ConnectingScreen.this.minecraft.execute(() ->
                    {
                        ConnectingScreen.this.minecraft.displayScreen(new DisconnectedScreen(ConnectingScreen.this.previousGuiScreen, DialogTexts.CONNECTION_FAILED, new TranslationTextComponent("disconnect.genericReason", "Unknown host")));
                    });
                } catch (Exception exception) {
                    if (ConnectingScreen.this.cancel) {
                        return;
                    }

                    ConnectingScreen.LOGGER.error("Couldn't connect to server", exception);
                    String s = inetaddress == null ? exception.toString() : exception.toString().replaceAll(inetaddress + ":" + port, "");
                    ConnectingScreen.this.minecraft.execute(() ->
                            ConnectingScreen.this.minecraft.displayScreen(new DisconnectedScreen(ConnectingScreen.this.previousGuiScreen, DialogTexts.CONNECTION_FAILED, new TranslationTextComponent("disconnect.genericReason", s))));
                }
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    private void func_209514_a(ITextComponent p_209514_1_) {
        this.field_209515_s = p_209514_1_;
    }

    public void tick() {
        if (this.networkManager != null) {
            if (this.networkManager.isChannelOpen()) {
                this.networkManager.tick();
            } else {
                this.networkManager.handleDisconnection();
            }
        }
    }

    public boolean shouldCloseOnEsc() {
        return false;
    }


    protected void init() {
        this.addButton(new Button(this.width / 2 - 100, this.height / 4 + 120 + 12, 200, 20, DialogTexts.GUI_CANCEL, (p_212999_1_) ->
        {
            this.cancel = true;

            if (this.networkManager != null) {
                this.networkManager.closeChannel(new TranslationTextComponent("connect.aborted"));
            }

            this.minecraft.displayScreen(this.previousGuiScreen);
        }));
    }

    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        this.renderBackground(matrixStack);
        long i = Util.milliTime();

        if (i - this.field_213000_g > 2000L) {
            this.field_213000_g = i;

        }

        drawCenteredStringWithShadow(matrixStack, this.font, this.field_209515_s, this.width / 2, this.height / 2 - 50, 16777215);
        super.render(matrixStack, mouseX, mouseY, partialTicks);
    }
}
