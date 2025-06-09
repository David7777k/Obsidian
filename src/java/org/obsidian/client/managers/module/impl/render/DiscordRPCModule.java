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
import org.lwjgl.opengl.GL11;
import org.obsidian.client.managers.module.Category;
import org.obsidian.client.managers.module.Module;
import org.obsidian.client.managers.module.ModuleInfo;

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
    Thread rpcThread;
    volatile boolean running = false;
    volatile boolean initialized = false;

    @Override
    public void onEnable() {
        super.onEnable();
        // Инициализация в отдельном потоке для предотвращения блокировки основного потока
        new Thread(this::startRPC, "DiscordRPC-Init").start();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        stopRPC();
    }

    private void startRPC() {
        try {
            System.out.println("DiscordRPC: инициализация...");

            // Проверка на наличие ошибок OpenGL перед инициализацией
            int glError = GL11.glGetError();
            if (glError != GL11.GL_NO_ERROR) {
                System.err.println("OpenGL ошибка перед инициализацией Discord RPC: " + glError);
            }

            DiscordEventHandlers handlers = new DiscordEventHandlers();
            handlers.ready = user -> {
                System.out.println("DiscordRPC готов: " + user.username);
                initialized = true;
            };

            // Инициализация Discord RPC с обработкой возможных ошибок
            try {
                discordRPC.Discord_Initialize(DISCORD_ID, handlers, true, null);
            } catch (UnsatisfiedLinkError e) {
                System.err.println("DiscordRPC: не удалось загрузить нативную библиотеку: " + e.getMessage());
                if (mc.player != null) {
                    mc.player.sendStatusMessage(
                            new StringTextComponent(TextFormatting.RED + "[DiscordRPC] Ошибка загрузки библиотеки, модуль отключён."),
                            false
                    );
                }
                setEnabled(false);
                return;
            }

            // Проверка на наличие ошибок OpenGL после инициализации
            glError = GL11.glGetError();
            if (glError != GL11.GL_NO_ERROR) {
                System.err.println("OpenGL ошибка после инициализации Discord RPC: " + glError);
            }

        } catch (Exception ex) {
            System.err.println("DiscordRPC: не удалось инициализировать RPC: " + ex.getMessage());
            ex.printStackTrace();
            if (mc.player != null) {
                mc.player.sendStatusMessage(
                        new StringTextComponent(TextFormatting.RED + "[DiscordRPC] Ошибка инициализации, модуль отключён."),
                        false
                );
            }
            setEnabled(false);
            return;
        }

        running = true;
        rpcThread = new Thread(() -> {
            // Ждем небольшую паузу перед началом работы потока
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            Minecraft mc = Minecraft.getInstance();
            DiscordRichPresence presence = new DiscordRichPresence();
            presence.startTimestamp = System.currentTimeMillis() / 1000L;
            presence.largeImageKey = LARGE_IMAGE_KEY;
            presence.largeImageText = "Обсидian Client";
            presence.smallImageKey = SMALL_IMAGE_KEY;
            presence.smallImageText = "Онлайн";

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    // Проверка инициализации перед обновлением
                    if (!initialized) {
                        Thread.sleep(1000);
                        continue;
                    }

                    // Обновление информации только если Discord RPC успешно инициализирован
                    if (mc.world != null && mc.player != null) {
                        String worldName = mc.world.getDimensionKey().getLocation().toString();
                        presence.details = "Мир: " + worldName;
                        presence.state = "Игрок: " + mc.player.getName().getString();
                    } else {
                        presence.details = "В меню";
                        presence.state = "Ждём игрока...";
                    }

                    try {
                        discordRPC.Discord_RunCallbacks();
                        discordRPC.Discord_UpdatePresence(presence);
                    } catch (Exception e) {
                        System.err.println("Ошибка при обновлении Discord RPC: " + e.getMessage());
                        // Продолжаем работу, не прерывая поток
                    }

                    // Увеличенный интервал для уменьшения нагрузки
                    Thread.sleep(15_000);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception ex) {
                    System.err.println("DiscordRPC: ошибка во время обновления presence — " + ex.getMessage());
                    // Не делаем break, чтобы поток продолжал попытки работать
                    try {
                        Thread.sleep(30_000); // Долгая пауза при ошибке
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        }, "DiscordRPC-Thread");

        rpcThread.setDaemon(true);
        rpcThread.start();

        if (mc.player != null) {
            mc.player.sendStatusMessage(
                    new StringTextComponent(TextFormatting.GREEN + "[DiscordRPC] Запущен."),
                    false
            );
        }
    }

    private void stopRPC() {
        running = false;
        initialized = false;

        if (rpcThread != null) {
            rpcThread.interrupt();
            try {
                rpcThread.join(2000);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
            rpcThread = null;
        }

        System.out.println("DiscordRPC: остановка...");
        try {
            discordRPC.Discord_ClearPresence();
            discordRPC.Discord_Shutdown();
        } catch (Exception ex) {
            // Просто логируем ошибку, но не делаем ничего особенного
            System.err.println("DiscordRPC: ошибка при завершении — " + ex.getMessage());
        }
    }

    @Override
    public boolean isStarred() {
        return true;
    }
}