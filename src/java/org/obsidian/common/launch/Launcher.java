package org.obsidian.common.launch;

import by.radioegor146.nativeobfuscator.Native;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.GameConfiguration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ScreenSize;
import net.minecraft.client.util.UndeclaredException;
import net.minecraft.crash.CrashReport;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.integrated.IntegratedServer;
import net.minecraft.util.DefaultUncaughtExceptionHandler;
import net.minecraft.util.Session;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Bootstrap;
import net.mojang.blaze3d.systems.RenderSystem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.obsidian.client.Obsidian;
import org.obsidian.client.api.client.Constants;
import org.obsidian.common.user.LoginManager;

import java.io.File;
import java.net.Proxy;
import java.util.OptionalInt;

@Native
public class Launcher {
    private static final Logger LOGGER = LogManager.getLogger();

    static {
        System.setProperty("file.encoding", "UTF-8");
        System.setProperty("java.net.preferIPv4Stack", "true");
        System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", String.valueOf(Runtime.getRuntime().availableProcessors()));
    }

    public static void start() {
        CrashReport.crash();
        Bootstrap.register();
        Bootstrap.checkTranslations();
        Util.startInfiniteTimer();

        // User Data
        String username = LoginManager.loadUsername();
        LoginManager.saveUsername(username);

        String uuid = PlayerEntity.getOfflineUUID(username).toString();
        String accessToken = "0";
        String type = "legacy";
        Session session = new Session(username, uuid, accessToken, type);

        // Display Data
        int width = 854;
        int height = 480;
        OptionalInt fullscreenWidth = OptionalInt.empty();
        OptionalInt fullscreenHeight = OptionalInt.empty();
        boolean isFullscreen = false;

        // Folder Data
        File gameDir = new File(Constants.MAIN_DIR.toAbsolutePath().toString());
        File assetsDir = Obsidian.devMode() ? new File("assets/") : new File(gameDir, "assets/");
        File resourcepacksDir = new File(gameDir, "resourcepacks/");
        String assetIndex = "1.16";

        // Game Data
        boolean demo = false;
        String launchVersion = "1.16.5";
        String versionType = "release";
        boolean disableMultiplayer = false;
        boolean disableChat = false;

        // Server Data
        int port = 25565;

        GameConfiguration gameConfiguration = new GameConfiguration(
                new GameConfiguration.UserInformation(session, new PropertyMap(), new PropertyMap(), Proxy.NO_PROXY),
                new ScreenSize(width, height, fullscreenWidth, fullscreenHeight, isFullscreen),
                new GameConfiguration.FolderInformation(gameDir, resourcepacksDir, assetsDir, assetIndex),
                new GameConfiguration.GameInformation(demo, launchVersion, versionType, disableMultiplayer, disableChat),
                new GameConfiguration.ServerInformation(null, port));
        start(gameConfiguration);
    }


    private static void start(GameConfiguration gameConfiguration) {
        Thread thread = getShutdownThread();
        Runtime.getRuntime().addShutdownHook(thread);
        final Minecraft minecraft;

        try {
            Thread.currentThread().setName("Main");
            RenderSystem.initRenderThread();
            RenderSystem.beginInitialization();
            //noinspection resource
            minecraft = new Minecraft(gameConfiguration);
            RenderSystem.finishInitialization();
        } catch (UndeclaredException undeclaredexception) {
            LOGGER.warn("Failed to create window: ", undeclaredexception);
            return;
        } catch (Throwable throwable1) {
            CrashReport crashreport = CrashReport.makeCrashReport(throwable1, "Initializing game");
            crashreport.makeCategory("Initialization");
            Minecraft.fillCrashReport(null, gameConfiguration.gameInfo.version, null, crashreport);
            Minecraft.displayCrashReport(crashreport);
            return;
        }

        Thread mainThread;

        if (minecraft.isRenderOnThread()) {
            mainThread = new Thread("Game thread") {
                public void run() {
                    try {
                        RenderSystem.initGameThread(true);
                        minecraft.run();
                    } catch (Throwable throwable2) {
                        LOGGER.error("Exception in client thread", throwable2);
                    }
                }
            };
            mainThread.start();
        } else {
            mainThread = null;

            try {
                RenderSystem.initGameThread(false);
                minecraft.run();
            } catch (Throwable throwable) {
                LOGGER.error("Unhandled game exception", throwable);
            }
        }

        try {
            minecraft.shutdown();

            if (mainThread != null) {
                mainThread.join();
            }
        } catch (InterruptedException interruptedexception) {
            LOGGER.error("Exception during client thread shutdown", interruptedexception);
        } finally {
            minecraft.shutdownMinecraftApplet();
        }
    }

    private static Thread getShutdownThread() {
        Thread thread = new Thread("Client Shutdown Thread") {
            @Override
            public void run() {
                Minecraft minecraft = Minecraft.getInstance();
                IntegratedServer integratedserver = minecraft.getIntegratedServer();

                if (integratedserver != null) {
                    integratedserver.initiateShutdown(true);
                }
            }
        };
        thread.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler(LOGGER));
        return thread;
    }

}