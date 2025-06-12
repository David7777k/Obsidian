//qq
package org.obsidian.client;

import by.radioegor146.nativeobfuscator.Native;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.extern.log4j.Log4j2;
import net.minecraft.client.Minecraft;
import org.obsidian.client.api.client.Constants;
import org.obsidian.client.api.events.orbit.EventBus;
import org.obsidian.client.managers.command.AdviceCommandFactoryImpl;
import org.obsidian.client.managers.command.ParametersFactoryImpl;
import org.obsidian.client.managers.command.PrefixImpl;
import org.obsidian.client.managers.command.StandaloneCommandDispatcher;
import org.obsidian.client.managers.command.api.*;
import org.obsidian.client.managers.command.api.logger.ConsoleLogger;
import org.obsidian.client.managers.command.api.logger.MinecraftLogger;
import org.obsidian.client.managers.command.api.logger.MultiLogger;
import org.obsidian.client.managers.command.impl.*;
import org.obsidian.client.managers.component.ComponentManager;
import org.obsidian.client.managers.module.ModuleManager;
import org.obsidian.client.managers.other.autobuy.AutoBuyFile;
import org.obsidian.client.managers.other.autobuy.AutoBuyManager;
import org.obsidian.client.managers.other.config.ConfigFile;
import org.obsidian.client.managers.other.config.ConfigManager;
import org.obsidian.client.managers.other.friend.FriendFile;
import org.obsidian.client.managers.other.friend.FriendManager;
import org.obsidian.client.managers.other.macros.MacrosFile;
import org.obsidian.client.managers.other.macros.MacrosManager;
import org.obsidian.client.managers.other.notification.NotificationManager;
import org.obsidian.client.managers.other.staff.StaffFile;
import org.obsidian.client.managers.other.staff.StaffManager;
import org.obsidian.client.screen.account.AccountFile;
import org.obsidian.client.screen.account.AccountGuiScreen;
import org.obsidian.client.screen.account.AccountManager;
import org.obsidian.client.screen.clickgui.ClickGuiScreen;
import org.obsidian.client.screen.flatgui.FlatGuiScreen;
import org.obsidian.client.utils.file.FileManager;
import org.obsidian.client.utils.other.Console;
import org.obsidian.client.utils.render.font.Fonts;
import org.obsidian.client.utils.render.shader.ShaderManager;
import org.obsidian.client.utils.server.ServerTPS;
import org.obsidian.client.utils.time.Profiler;
import org.obsidian.common.impl.proxy.ProxyConfig;
import org.obsidian.common.impl.viaversion.ViaMCP;
import org.obsidian.common.impl.waveycapes.WaveyCapesBase;

import java.lang.invoke.MethodHandles;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("IfStatementWithIdenticalBranches")
@Native
@Log4j2
@Getter
@Accessors(fluent = true)
public class Obsidian {
    @Getter
    private static final Obsidian inst = new Obsidian();

    @Getter
    private static boolean initialized = false;
    @Getter
    private static final boolean devMode = Files.exists(Path.of("sexfromide"));
    @Getter
    private static final EventBus eventHandler = EventBus.threadSafe();

//    @Setter
//    private UserData userData;

    private final long loadTime = System.currentTimeMillis();
    private FileManager fileManager;
    private ModuleManager moduleManager;
    private CommandDispatcher commandDispatcher;
    private ComponentManager componentManager;
    private ConfigManager configManager;
    private FriendManager friendManager;
    private AccountManager accountManager;
    private StaffManager staffManager;
    private MacrosManager macrosManager;

    private AutoBuyManager autoBuyManager;

    private WaveyCapesBase waveyCapes;
    private NotificationManager notificationManager;
    private ClickGuiScreen clickGui;
    private FlatGuiScreen flatGui;
    private AccountGuiScreen accountGui;
    private final Profiler profiler = new Profiler();
    private ServerTPS serverTps;

    public final List<String> sponsors = List.of("mc.funtime.su");

    public void start() {
        eventHandler.registerLambdaFactory(devMode ? "org.obsidian" : "exc", (lookupInMethod, klass) -> (MethodHandles.Lookup) lookupInMethod.invoke(null, klass, MethodHandles.lookup()));
    }

    public void load() {
        Fonts.loadFonts();
        ShaderManager.loadShaders();
        ProxyConfig.loadConfig();

        this.initVia();

        log("Инициализация..");

        this.profiler.start();

        initManagers();

        initCommands();

        initWaveyCapes();

        initScreens();

        this.profiler.stop();

        log("Инициализировано в " + profiler.getTotalTimeMS() + "ms.");

        loadAllConfigs();

        Minecraft.getInstance().getMainWindow().setWindowTitle(Constants.TITLE);

        initialized = true;

        Runtime.getRuntime().addShutdownHook(new Thread(this::unload));
    }

    private void initWaveyCapes() {
        this.waveyCapes = new WaveyCapesBase();
        this.waveyCapes.init();
    }

    private void initScreens() {
        this.clickGui = new ClickGuiScreen();
        this.flatGui = new FlatGuiScreen();
        this.accountGui = new AccountGuiScreen();
    }

    private void initManagers() {
        this.fileManager = new FileManager();

        this.serverTps = new ServerTPS();

        this.notificationManager = new NotificationManager();
        this.notificationManager.init();

        this.moduleManager = new ModuleManager();
        this.moduleManager.init();

        this.componentManager = new ComponentManager();
        this.componentManager.init();

        this.configManager = new ConfigManager();
        this.friendManager = new FriendManager();
        this.accountManager = new AccountManager();
        this.staffManager = new StaffManager();
        this.macrosManager = new MacrosManager();
        this.autoBuyManager = new AutoBuyManager();
    }

    private void initVia() {
        ViaMCP.create();
    }

    private void loadAllConfigs() {
        loadConfigs();
        loadFriends();
        loadAccounts();
        loadStaffs();
        loadMacros();
        loadAutoBuy();
    }

    public void unload() {
        if (configManager != null) {
            configManager.set();
        }
        if (friendManager != null) {
            friendManager.set();
        }
        if (accountManager != null) {
            accountManager.set();
        }
        if (staffManager != null) {
            staffManager.set();
        }
        if (macrosManager != null) {
            macrosManager.set();
        }
        if (autoBuyManager != null) {
            autoBuyManager.set();
        }
    }

    public static void log(String str, Object... args) {
        log.info("{}{}{} -> {}{}",
                Console.getConsoleBackground(),
                Console.getConsoleText(),
                Constants.NAME,
                str.formatted(args),
                Console.getConsoleReset());
    }

    public void loadConfigs() {
        configManager.update();
        final ConfigFile config = configManager.get("default");
        if (config == null || !config.read()) {
            configManager.set();
        } else {
            configManager.set();
        }
    }

    public void loadFriends() {
        final FriendFile friend = friendManager.get();
        if (friend == null || !friend.read()) {
            friendManager.set();
        } else {
            friendManager.set();
        }
    }

    public void loadAccounts() {
        final AccountFile account = accountManager.get();
        if (account == null || !account.read()) {
            accountManager.set();
        } else {
            accountManager.set();
        }
    }

    public void loadStaffs() {
        final StaffFile staff = staffManager.get();
        if (staff == null || !staff.read()) {
            staffManager.set();
        } else {
            staffManager.set();
        }
    }

    public void loadMacros() {
        final MacrosFile macros = macrosManager.get();
        if (macros == null || !macros.read()) {
            macrosManager.set();
        } else {
            macrosManager.set();
        }
    }

    public void loadAutoBuy() {
        final AutoBuyFile autoBuy = autoBuyManager.get();
        if (autoBuy == null || !autoBuy.read()) {
            autoBuyManager.set();
        } else {
            autoBuyManager.set();
        }
    }

    private void initCommands() {
        Logger logger = new MultiLogger(List.of(new ConsoleLogger(), new MinecraftLogger()));
        List<Command> commands = new ArrayList<>();
        Prefix prefix = new PrefixImpl();
        commands.add(new ListCommand(commands, logger));
        commands.add(new FriendCommand(friendManager, prefix, logger));
        commands.add(new StaffCommand(staffManager, prefix, logger));
        commands.add(new BindCommand(prefix, logger));
        commands.add(new LoginCommand(logger));
        commands.add(new NameCommand(logger));
        commands.add(new PanicCommand(logger));
        commands.add(new ServerInfoCommand(logger));
        commands.add(new GPSCommand(prefix));
        commands.add(new WayCommand(prefix, logger));
        commands.add(new ConfigCommand(configManager, prefix, logger));
        commands.add(new MacroCommand(macrosManager, prefix, logger));
        commands.add(new VClipCommand(prefix, logger));
        commands.add(new HClipCommand(prefix, logger));
        commands.add(new MemoryCommand(logger));
        commands.add(new RCTCommand(logger));

        AdviceCommandFactory adviceCommandFactory = new AdviceCommandFactoryImpl(logger);
        ParametersFactory parametersFactory = new ParametersFactoryImpl();

        commandDispatcher = new StandaloneCommandDispatcher(commands, adviceCommandFactory, prefix, parametersFactory, logger);
    }
}