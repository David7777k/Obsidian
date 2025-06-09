package org.obsidian.client.managers.module.impl.render;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import net.minecraft.client.Minecraft;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;
import java.util.concurrent.CompletableFuture;

@Getter
@Accessors(fluent = true)
@FieldDefaults(level = AccessLevel.PRIVATE)
@ModuleInfo(name = "DiscordRPC", category = Category.RENDER, autoEnabled = true)
public class DiscordRPCModule extends Module {
    // ID приложения
    static final String DISCORD_ID = "1350950850737995796";
    // Ключ большого изображения
    static final String LARGE_IMAGE_KEY = "ranboo_logo";
    // Ключ маленького изображения
    static final String SMALL_IMAGE_KEY = "small_icon";

    final DiscordRPC discordRPC = DiscordRPC.INSTANCE;

    final Object lock = new Object();
    Thread initThread;
    Thread rpcThread;
    volatile boolean running = false;
    volatile boolean initialized = false;

    @Override
    public void onEnable() {
        super.onEnable();
        synchronized (lock) {
            if (running || initThread != null) {
                return;
            }
            initThread = new Thread(this::startRPC, "DiscordRPC-Init");
            initThread.setDaemon(true);
            initThread.start();
        }
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stopRPC();
    }

    private void startRPC() {
        try {
            DiscordEventHandlers handlers = new DiscordEventHandlers();
            handlers.ready = user -> {
                System.out.println("DiscordRPC ready: " + user.username);
                initialized = true;
            };

            discordRPC.Discord_Initialize(DISCORD_ID, handlers, true, null);
        } catch (UnsatisfiedLinkError e) {
            System.err.println("DiscordRPC: unable to load native library: " + e.getMessage());
            notifyPlayer(TextFormatting.RED + "[DiscordRPC] Ошибка загрузки библиотеки, модуль отключён.");
            setEnabled(false);
            return;
        } catch (Exception ex) {
            System.err.println("DiscordRPC: не удалось инициализировать RPC: " + ex.getMessage());
            notifyPlayer(TextFormatting.RED + "[DiscordRPC] Ошибка инициализации, модуль отключён.");
            setEnabled(false);
            return;
        }

        synchronized (lock) {
            initThread = null;
            if (!isEnabled()) {
                shutdownRpc();
                return;
            }
            running = true;
        }

        rpcThread = new Thread(this::loop, "DiscordRPC-Thread");
        rpcThread.setDaemon(true);
        rpcThread.start();

        notifyPlayer(TextFormatting.GREEN + "[DiscordRPC] Запущен.");
    }

    private void loop() {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        DiscordRichPresence presence = new DiscordRichPresence();
        presence.startTimestamp = System.currentTimeMillis() / 1000L;
        presence.largeImageKey = LARGE_IMAGE_KEY;
        presence.largeImageText = "Обсидian Client";
        presence.smallImageKey = SMALL_IMAGE_KEY;
        presence.smallImageText = "Онлайн";

        while (running && !Thread.currentThread().isInterrupted()) {
            try {
                if (!initialized) {
                    Thread.sleep(1000);
                    continue;
                }

                updatePresence(presence);

                discordRPC.Discord_RunCallbacks();
                discordRPC.Discord_UpdatePresence(presence);

                Thread.sleep(15_000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (Exception ex) {
                System.err.println("DiscordRPC update error: " + ex.getMessage());
                try {
                    Thread.sleep(30_000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        shutdownRpc();
    }

    private void updatePresence(DiscordRichPresence presence) throws Exception {
        Minecraft mc = Minecraft.getInstance();
        CompletableFuture<String[]> future = mc.supplyAsync(() -> {
            if (mc.world != null && mc.player != null) {
                String worldName = mc.world.getDimensionKey().getLocation().toString();
                String playerName = mc.player.getName().getString();
                return new String[]{"Мир: " + worldName, "Игрок: " + playerName};
            } else {
                return new String[]{"В меню", "Ждём игрока..."};
            }
        });
        String[] info = future.get();
        presence.details = info[0];
        presence.state = info[1];
    }

    private void shutdownRpc() {
        if (!initialized) {
            return;
        }
        initialized = false;
        try {
            discordRPC.Discord_ClearPresence();
            discordRPC.Discord_Shutdown();
        } catch (Exception ex) {
            System.err.println("DiscordRPC: ошибка при завершении — " + ex.getMessage());
        }
    }

    private void notifyPlayer(String message) {
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            if (mc.player != null) {
                mc.player.sendStatusMessage(new StringTextComponent(message), false);
            }
        });
    }

    private void stopRPC() {
        synchronized (lock) {
            running = false;
            initialized = false;

            if (initThread != null) {
                initThread.interrupt();
                try {
                    initThread.join(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                initThread = null;
            }

            if (rpcThread != null) {
                rpcThread.interrupt();
                try {
                    rpcThread.join(2000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
                rpcThread = null;
            }
        }

        shutdownRpc();
    }

    @Override
    public boolean isStarred() {
        return true;
    }
}