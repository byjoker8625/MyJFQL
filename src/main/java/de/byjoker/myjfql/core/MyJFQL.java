package de.byjoker.myjfql.core;

import de.byjoker.myjfql.command.CommandService;
import de.byjoker.myjfql.command.CommandServiceImpl;
import de.byjoker.myjfql.command.ConsoleCommandSender;
import de.byjoker.myjfql.config.Config;
import de.byjoker.myjfql.config.ConfigDefaults;
import de.byjoker.myjfql.config.ConfigService;
import de.byjoker.myjfql.config.ConfigServiceImpl;
import de.byjoker.myjfql.console.Console;
import de.byjoker.myjfql.console.JLineConsole;
import de.byjoker.myjfql.console.ScannerConsole;
import de.byjoker.myjfql.console.SystemConsole;
import de.byjoker.myjfql.core.lang.Formatter;
import de.byjoker.myjfql.core.lang.JFQLFormatter;
import de.byjoker.myjfql.database.*;
import de.byjoker.myjfql.exception.FileException;
import de.byjoker.myjfql.exception.NetworkException;
import de.byjoker.myjfql.server.Server;
import de.byjoker.myjfql.user.UserService;
import de.byjoker.myjfql.user.UserServiceImpl;
import de.byjoker.myjfql.util.*;

import java.util.Timer;
import java.util.TimerTask;

public final class MyJFQL {

    private static MyJFQL instance;

    private final String version = "1.5.2";
    private final Formatter formatter;
    private final CommandService commandService;
    private final DatabaseService databaseService;
    private final ConfigService configService;
    private final BackupService databaseBackupService;
    private final UserService userService;
    private final ConsoleCommandSender consoleCommandSender;
    private final DBSession dbSession;
    private final Server server;
    private final Updater updater;
    private final Downloader downloader;
    private Config config;
    private Console console;
    private Encryptor encryptor;

    public MyJFQL() {
        instance = this;
        this.configService = new ConfigServiceImpl();
        this.console = new SystemConsole();
        this.config = new ConfigDefaults();
        this.encryptor = new NoneEncryptor();
        this.formatter = new JFQLFormatter();
        this.consoleCommandSender = new ConsoleCommandSender(console);
        this.updater = new Updater(version);
        this.commandService = new CommandServiceImpl(formatter);
        this.userService = new UserServiceImpl();
        this.downloader = updater.getDownloader();
        this.databaseService = new DatabaseServiceImpl();
        this.dbSession = new DBSession(userService, databaseService);
        this.databaseBackupService = new BackupServiceImpl(databaseService);
        this.server = new Server();
    }

    public static MyJFQL getInstance() {
        return instance;
    }

    public void start() {
        console.print("\n      _                  ______ _ _       ____                        _                                              \n" +
                "     | |                |  ____(_) |     / __ \\                      | |                                             \n" +
                "     | | __ ___   ____ _| |__   _| | ___| |  | |_   _  ___ _ __ _   _| |     __ _ _ __   __ _ _   _  __ _  __ _  ___ \n" +
                " _   | |/ _` \\ \\ / / _` |  __| | | |/ _ \\ |  | | | | |/ _ \\ '__| | | | |    / _` | '_ \\ / _` | | | |/ _` |/ _` |/ _ \\\n" +
                "| |__| | (_| |\\ V / (_| | |    | | |  __/ |__| | |_| |  __/ |  | |_| | |___| (_| | | | | (_| | |_| | (_| | (_| |  __/\n" +
                " \\____/ \\__,_| \\_/ \\__,_|_|    |_|_|\\___|\\___\\_\\\\__,_|\\___|_|   \\__, |______\\__,_|_| |_|\\__, |\\__,_|\\__,_|\\__, |\\___|\n" +
                "                                                                 __/ |                   __/ |             __/ |     \n" +
                "                                                                |___/                   |___/             |___/      \n" +
                "");
        console.logInfo("Developer > ByJoker");
        console.logInfo("Version > v" + version);
        console.clean();

        try {
            console.logInfo("Loading system configurations...");

            {
                configService.load();
                configService.searchConfigBuilders("de.byjoker.myjfql.config");
                config = configService.getConfig();
            }

            {
                if (config.jline())
                    console = new JLineConsole();
                else
                    console = new ScannerConsole();
            }

            {
                if (config.encryption().equals("BASE64"))
                    encryptor = new Base64Encryptor();
                else
                    encryptor = new NoneEncryptor();
            }

            console.logInfo("Successfully initialized config.");
            console.clean();
        } catch (Exception ex) {
            throw new FileException(ex);
        }

        if (config.updates()) {
            console.logInfo("Connecting to " + config.updateHost() + "...");

            try {
                updater.fetch(config.updateHost());
            } catch (Exception ex) {
                throw new NetworkException("Server connection failed!");
            }

            console.logInfo("Successfully connected.");
            console.clean();

            switch (updater.getCompatibilityStatus()) {
                case SAME:
                    console.logInfo("Your are up to date with you MyJFQL version. You can enjoy all features of this system :D");
                    break;
                case JUST_FINE:
                    if (config.autoUpdate())
                        downloader.downloadLatestVersion();
                    else
                        console.logWarning("You aren't up to date. Please download the latest version.");
                    break;
                case SOME_CHANGES:
                    console.logWarning("You aren't up to date. Please download the latest version. But please make sure that the new version working you have to make some changes!");
                    break;
                case PENSIONER:
                    console.logWarning("You are using a pretty old version of MyJFQL! With this version you wouldn't be able to update to the latest version without many heavy changes.");
                    break;
            }

            console.clean();
        }

        commandService.searchCommands("de.byjoker.myjfql.command");

        if (config.server()) {
            try {
                server.start(config.port());
            } catch (Exception ex) {
                throw new NetworkException(ex);
            }

            console.clean();
        }

        {
            console.logInfo("Loading databases and users (This can take a while)...");
            databaseService.loadAll();
            userService.loadAll();
            console.logInfo("Loading finished!");
        }

        /*
        if (_configService.isNonCompatibleConfiguration(_configService.getRawConfiguration())) {
            console.clean();
            console.logWarning("You are using a pretty old config version! Please refresh your config for a higher configurability.");
        }*/

        {
            if (databaseService.getDatabases().size() == 0
                    && userService.getUsers().size() != 0) {
                console.clean();
                console.logWarning("No databases exists!");
            }

            if (databaseService.getDatabases().size() != 0
                    && userService.getUsers().size() == 0) {
                console.clean();
                console.logWarning("No users exists!");
            }

            if (databaseService.getDatabases().size() == 0 && userService.getUsers().size() == 0) {
                console.clean();
                console.logWarning("No databases exists!");
                console.logWarning("No users exists!");
            }
        }

        new Timer().scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                databaseService.updateAll();
                userService.updateAll();
            }
        }, 1000 * 60, 1000 * 60);

        console.complete();

        while (true)
            commandService.execute(consoleCommandSender, console.readPrompt());
    }

    public void shutdown() {
        try {
            console.logInfo("Shutdown (This can take a while)...");
            databaseService.updateAll();
            userService.updateAll();
        } catch (Exception ignore) {
        }

        System.exit(0);
    }

    public Console getConsole() {
        return console;
    }

    public ConsoleCommandSender getConsoleCommandSender() {
        return consoleCommandSender;
    }

    public Updater getUpdater() {
        return updater;
    }

    public Downloader getDownloader() {
        return downloader;
    }

    public String getVersion() {
        return version;
    }

    public CommandService getCommandService() {
        return commandService;
    }

    public ConfigService getConfigService() {
        return configService;
    }

    public DBSession getDBSession() {
        return dbSession;
    }

    public Server getServer() {
        return server;
    }

    public Encryptor getEncryptor() {
        return encryptor;
    }

    public Formatter getFormatter() {
        return formatter;
    }

    public DatabaseService getDatabaseService() {
        return databaseService;
    }

    public UserService getUserService() {
        return userService;
    }

    public BackupService getDatabaseBackupService() {
        return databaseBackupService;
    }

    public Config getConfig() {
        return config;
    }
}