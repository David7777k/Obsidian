package net.minecraft.network.login;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationUnavailableException;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.login.client.CCustomPayloadLoginPacket;
import net.minecraft.network.login.client.CEncryptionResponsePacket;
import net.minecraft.network.login.client.CLoginStartPacket;
import net.minecraft.network.login.server.SDisconnectLoginPacket;
import net.minecraft.network.login.server.SEnableCompressionPacket;
import net.minecraft.network.login.server.SEncryptionRequestPacket;
import net.minecraft.network.login.server.SLoginSuccessPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.CryptException;
import net.minecraft.util.CryptManager;
import net.minecraft.util.DefaultUncaughtExceptionHandler;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obsidian.common.impl.fastrandom.FastRandom;

import javax.annotation.Nullable;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.security.PrivateKey;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerLoginNetHandler implements IServerLoginNetHandler {
    private static final AtomicInteger AUTHENTICATOR_THREAD_ID = new AtomicInteger(0);
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new FastRandom();
    private final byte[] verifyToken = new byte[4];
    private final MinecraftServer server;
    public final NetworkManager networkManager;
    private ServerLoginNetHandler.State currentLoginState = ServerLoginNetHandler.State.HELLO;

    /**
     * How long has player been trying to login into the server.
     */
    private int connectionTimer;
    private GameProfile loginGameProfile;
    private final String serverId = "";
    private SecretKey secretKey;
    private ServerPlayerEntity player;

    public ServerLoginNetHandler(MinecraftServer serverIn, NetworkManager networkManagerIn) {
        this.server = serverIn;
        this.networkManager = networkManagerIn;
        RANDOM.nextBytes(this.verifyToken);
    }

    public void tick() {
        if (this.currentLoginState == ServerLoginNetHandler.State.READY_TO_ACCEPT) {
            this.tryAcceptPlayer();
        } else if (this.currentLoginState == ServerLoginNetHandler.State.DELAY_ACCEPT) {
            ServerPlayerEntity serverplayerentity = this.server.getPlayerList().getPlayerByUUID(this.loginGameProfile.getId());

            if (serverplayerentity == null) {
                this.currentLoginState = ServerLoginNetHandler.State.READY_TO_ACCEPT;
                this.server.getPlayerList().initializeConnectionToPlayer(this.networkManager, this.player);
                this.player = null;
            }
        }

        if (this.connectionTimer++ == 600) {
            this.disconnect(new TranslationTextComponent("multiplayer.disconnect.slow_login"));
        }
    }

    /**
     * Returns this the NetworkManager instance registered with this NetworkHandlerPlayClient
     */
    public NetworkManager getNetworkManager() {
        return this.networkManager;
    }

    public void disconnect(ITextComponent reason) {
        try {
            LOGGER.info("Disconnecting {}: {}", this.getConnectionInfo(), reason.getString());
            this.networkManager.sendPacket(new SDisconnectLoginPacket(reason));
            this.networkManager.closeChannel(reason);
        } catch (Exception exception) {
            LOGGER.error("Error whilst disconnecting player", exception);
        }
    }

    public void tryAcceptPlayer() {
        if (!this.loginGameProfile.isComplete()) {
            this.loginGameProfile = this.getOfflineProfile(this.loginGameProfile);
        }

        ITextComponent itextcomponent = this.server.getPlayerList().canPlayerLogin(this.networkManager.getRemoteAddress(), this.loginGameProfile);

        if (itextcomponent != null) {
            this.disconnect(itextcomponent);
        } else {
            this.currentLoginState = ServerLoginNetHandler.State.ACCEPTED;

            if (this.server.getNetworkCompressionThreshold() >= 0 && !this.networkManager.isLocalChannel()) {
                this.networkManager.sendPacket(new SEnableCompressionPacket(this.server.getNetworkCompressionThreshold()), (p_210149_1_) ->
                {
                    this.networkManager.setCompressionThreshold(this.server.getNetworkCompressionThreshold());
                });
            }

            this.networkManager.sendPacket(new SLoginSuccessPacket(this.loginGameProfile));
            ServerPlayerEntity serverplayerentity = this.server.getPlayerList().getPlayerByUUID(this.loginGameProfile.getId());

            if (serverplayerentity != null) {
                this.currentLoginState = ServerLoginNetHandler.State.DELAY_ACCEPT;
                this.player = this.server.getPlayerList().createPlayerForUser(this.loginGameProfile);
            } else {
                this.server.getPlayerList().initializeConnectionToPlayer(this.networkManager, this.server.getPlayerList().createPlayerForUser(this.loginGameProfile));
            }
        }
    }

    /**
     * Invoked when disconnecting, the parameter is a ChatComponent describing the reason for termination
     */
    public void onDisconnect(ITextComponent reason) {
        LOGGER.info("{} lost connection: {}", this.getConnectionInfo(), reason.getString());
    }

    public String getConnectionInfo() {
        return this.loginGameProfile != null ? this.loginGameProfile + " (" + this.networkManager.getRemoteAddress() + ")" : String.valueOf(this.networkManager.getRemoteAddress());
    }

    public void processLoginStart(CLoginStartPacket packetIn) {
        Validate.validState(this.currentLoginState == ServerLoginNetHandler.State.HELLO, "Unexpected hello packet");
        this.loginGameProfile = packetIn.getProfile();

        if (this.server.isServerInOnlineMode() && !this.networkManager.isLocalChannel()) {
            this.currentLoginState = ServerLoginNetHandler.State.KEY;
            this.networkManager.sendPacket(new SEncryptionRequestPacket("", this.server.getKeyPair().getPublic().getEncoded(), this.verifyToken));
        } else {
            this.currentLoginState = ServerLoginNetHandler.State.READY_TO_ACCEPT;
        }
    }

    public void processEncryptionResponse(CEncryptionResponsePacket packetIn) {
        Validate.validState(this.currentLoginState == ServerLoginNetHandler.State.KEY, "Unexpected key packet");
        PrivateKey privatekey = this.server.getKeyPair().getPrivate();
        final String s;

        try {
            if (!Arrays.equals(this.verifyToken, packetIn.getVerifyToken(privatekey))) {
                throw new IllegalStateException("Protocol error");
            }

            this.secretKey = packetIn.getSecretKey(privatekey);
            Cipher cipher = CryptManager.createNetCipherInstance(2, this.secretKey);
            Cipher cipher1 = CryptManager.createNetCipherInstance(1, this.secretKey);
            s = (new BigInteger(CryptManager.getServerIdHash("", this.server.getKeyPair().getPublic(), this.secretKey))).toString(16);
            this.currentLoginState = ServerLoginNetHandler.State.AUTHENTICATING;
            this.networkManager.func_244777_a(cipher, cipher1);
        } catch (CryptException cryptexception) {
            throw new IllegalStateException("Protocol error", cryptexception);
        }

        Thread thread = new Thread("User Authenticator #" + AUTHENTICATOR_THREAD_ID.incrementAndGet()) {
            public void run() {
                GameProfile gameprofile = ServerLoginNetHandler.this.loginGameProfile;

                try {
                    ServerLoginNetHandler.this.loginGameProfile = ServerLoginNetHandler.this.server.getMinecraftSessionService().hasJoinedServer(new GameProfile(null, gameprofile.getName()), s, this.getAddress());

                    if (ServerLoginNetHandler.this.loginGameProfile != null) {
                        ServerLoginNetHandler.LOGGER.info("UUID of player {} is {}", ServerLoginNetHandler.this.loginGameProfile.getName(), ServerLoginNetHandler.this.loginGameProfile.getId());
                        ServerLoginNetHandler.this.currentLoginState = ServerLoginNetHandler.State.READY_TO_ACCEPT;
                    } else if (ServerLoginNetHandler.this.server.isSinglePlayer()) {
                        ServerLoginNetHandler.LOGGER.warn("Failed to verify username but will let them in anyway!");
                        ServerLoginNetHandler.this.loginGameProfile = ServerLoginNetHandler.this.getOfflineProfile(gameprofile);
                        ServerLoginNetHandler.this.currentLoginState = ServerLoginNetHandler.State.READY_TO_ACCEPT;
                    } else {
                        ServerLoginNetHandler.this.disconnect(new TranslationTextComponent("multiplayer.disconnect.unverified_username"));
                        ServerLoginNetHandler.LOGGER.error("Username '{}' tried to join with an invalid session", gameprofile.getName());
                    }
                } catch (AuthenticationUnavailableException authenticationunavailableexception) {
                    if (ServerLoginNetHandler.this.server.isSinglePlayer()) {
                        ServerLoginNetHandler.LOGGER.warn("Authentication servers are down but will let them in anyway!");
                        ServerLoginNetHandler.this.loginGameProfile = ServerLoginNetHandler.this.getOfflineProfile(gameprofile);
                        ServerLoginNetHandler.this.currentLoginState = ServerLoginNetHandler.State.READY_TO_ACCEPT;
                    } else {
                        ServerLoginNetHandler.this.disconnect(new TranslationTextComponent("multiplayer.disconnect.authservers_down"));
                        ServerLoginNetHandler.LOGGER.error("Couldn't verify username because servers are unavailable");
                    }
                }
            }

            @Nullable
            private InetAddress getAddress() {
                SocketAddress socketaddress = ServerLoginNetHandler.this.networkManager.getRemoteAddress();
                return ServerLoginNetHandler.this.server.getPreventProxyConnections() && socketaddress instanceof InetSocketAddress ? ((InetSocketAddress) socketaddress).getAddress() : null;
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        thread.start();
    }

    public void processCustomPayloadLogin(CCustomPayloadLoginPacket p_209526_1_) {
        this.disconnect(new TranslationTextComponent("multiplayer.disconnect.unexpected_query_response"));
    }

    protected GameProfile getOfflineProfile(GameProfile original) {
        UUID uuid = PlayerEntity.getOfflineUUID(original.getName());
        return new GameProfile(uuid, original.getName());
    }

    enum State {
        HELLO,
        KEY,
        AUTHENTICATING,
        NEGOTIATING,
        READY_TO_ACCEPT,
        DELAY_ACCEPT,
        ACCEPTED
    }
}
