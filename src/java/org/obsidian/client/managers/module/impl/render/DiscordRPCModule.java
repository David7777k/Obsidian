package org.obsidian.client.managers.module.impl.render;

import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;

@ModuleInfo(name = "DiscordRPC", category = Category.RENDER)
public class DiscordRPCModule extends Module {
    private static final String DISCORD_ID = "1350950850737995796"; // Ваш Application ID
    private final DiscordRPC discordRPC = DiscordRPC.INSTANCE; // Экземпляр на уровне объекта
    private Thread rpcThread;
    private volatile boolean running = false;

    @Override
    public void onEnable() {
        super.onEnable();
        startRPC();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stopRPC();
    }

    private void startRPC() {
        System.out.println("Инициализация Discord RPC...");
        DiscordEventHandlers handlers = new DiscordEventHandlers();
        handlers.ready = user -> System.out.println("Discord RPC готов: " + user.username);
        discordRPC.Discord_Initialize(DISCORD_ID, handlers, true, null);

        running = true;
        rpcThread = new Thread(() -> {
            DiscordRichPresence presence = new DiscordRichPresence();
            presence.startTimestamp = System.currentTimeMillis() / 1000L; // Инициализация времени
            presence.largeImageKey = "https://media1.tenor.com/m/bozQQujzT68AAAAd/ranboo-obsidian.gif"; // Ключ изображения из Developer Portal
            presence.largeImageText = "qq";
            if (!running) return; // Проверка перед запуском цикла

            while (running) {
                try {
                    presence.details = "Роль: > Developer";
                    presence.state = "Версия: > 1.3.3.7";
                    discordRPC.Discord_RunCallbacks(); // Обработка событий
                    discordRPC.Discord_UpdatePresence(presence);
                    Thread.sleep(10000); // Обновление каждые 10 секунд
                } catch (InterruptedException e) {
                    running = false;
                    System.out.println("Поток Discord RPC прерван.");
                } catch (Exception e) {
                    System.err.println("Ошибка в Discord RPC: " + e.getMessage());
                    running = false;
                }
            }
        }, "DiscordRPC-Thread");
        rpcThread.start();
    }

    private void stopRPC() {
        running = false;
        if (rpcThread != null) {
            rpcThread.interrupt();
            try {
                rpcThread.join(1000); // Ждём завершения потока до 1 секунды
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        System.out.println("Остановка Discord RPC...");
        discordRPC.Discord_Shutdown();
    }
}