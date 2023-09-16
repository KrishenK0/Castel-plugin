package fr.krishenk.castel.config.managers;

import com.github.benmanes.caffeine.cache.Cache;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.upgradable.Powerup;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.locale.compiler.placeholders.StandardCastelPlaceholder;
import fr.krishenk.castel.managers.ResourcePointManager;
import fr.krishenk.castel.scheduler.ScheduledTask;
import fr.krishenk.castel.utils.cache.CacheHandler;
import fr.krishenk.castel.utils.config.adapters.YamlContainer;
import fr.krishenk.castel.utils.internal.ExpirableSet;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
public class ConfigWatcher {
    protected static final WatchService WATCH_SERVICE;
    private static final ExpirableSet<String> HANDLER_COOLDOWN;
    private static final Cache<String, ScheduledTask> DELAYED_FTP_UPLOADS;
    private static final Map<WatchKey, BiConsumer<Path, WatchEvent.Kind<?>>> WATCHED_KEYS;
    private static final CastelPlugin plugin;
    private static final Path PLUGIN_FOLDER_PATH;
    private static final String WINSCP_FILEPARTS_EXTENSION = ".filepart";
    private static boolean accepting;
    protected static final Map<String, FileWatcher> NORMAL_WATCHERS;

    public static void setAccepting(boolean accepting) {
        ConfigWatcher.accepting = accepting;
    }

//    public static void reload(FileConfiguration config, String file) {
//        MessageHandler.sendConsolePluginMessage("&2Detected changes for&6 " + file + "&2, reloading...");
//        config.reload();
//        if (Config.UPDATES_CONFIGS.getBoolean()) {
//            config.update();
//        }
//        ConfigManager.validate(config);
//    }

    public static WatchKey register(Path dir, BiConsumer<Path, WatchEvent.Kind<?>> handler) {
        try {
            WatchKey key = dir.register(WATCH_SERVICE, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE, StandardWatchEventKinds.ENTRY_MODIFY);
            WATCHED_KEYS.put(key, handler);
            return key;
        }
        catch (IOException e) {
            CLogger.warn("Failed to register config watchers for: " + dir);
            throw new RuntimeException(e);
        }
    }

    public static void unregister(WatchKey key) {
        Objects.requireNonNull(key);
        key.cancel();
        WATCHED_KEYS.remove(key);
    }

//    public static void registerGUIWatchers(SupportedLanguage language) {
//        Path guiPath = language.getGUIFolder();
//        if (!Files.exists(guiPath, new LinkOption[0])) {
//            return;
//        }
//        final BiConsumer<Path, WatchEvent.Kind<?>> handler = ConfigWatcher.generateGUIHandlerForLang(language);
//        try {
//            Files.walkFileTree(guiPath, (FileVisitor<? super Path>)new SimpleFileVisitor<Path>(){
//
//                @Override
//                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
//                    ConfigWatcher.register(dir, handler);
//                    return super.preVisitDirectory(dir, attrs);
//                }
//            });
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    protected static void setupWatchService() {
        if (WATCH_SERVICE == null) {
            return;
        }
        Path mainDir = CastelPlugin.getFolder();
        ConfigWatcher.register(mainDir, ConfigWatcher::handleNormalConfigs);
//        ConfigWatcher.register(mainDir.resolve("Turrets"), ConfigWatcher::handleTurrets);
//        ConfigWatcher.register(mainDir.resolve("Structures"), ConfigWatcher::handleStructures);
//        ConfigWatcher.register(LanguageManager.LANG_FOLDER, ConfigWatcher::handleLanguageFile);
//        for (SupportedLanguage lang : SupportedLanguage.VALUES) {
//            if (!lang.isInstalled()) continue;
//            ConfigWatcher.registerGUIWatchers(lang);
//        }
        CastelPlugin.taskScheduler().executeAsync(ConfigWatcher::processWatchedKeys);
    }

    protected static void beforeWrite(YamlContainer adapter) {
        String name = CastelPlugin.getFolder().relativize(adapter.getFile().toPath()).toString();
        name = ConfigWatcher.toCooldownHandlerName(name);
        HANDLER_COOLDOWN.add(name);
    }

    static String toCooldownHandlerName(String path) {
        return path.substring(0, path.length() - 4);
    }

    static void processWatchedKeys() {
        while (true) {
            WatchKey key;
            try {
                key = WATCH_SERVICE.take();
            }
            catch (InterruptedException | ClosedWatchServiceException e) {
                CLogger.info("Config watcher service has stopped.");
                break;
            }
            List<WatchEvent<?>> events = key.pollEvents();
            if (accepting) {
                for (WatchEvent<?> event : events) {
                    long size;
                    Path resolvedPath;
                    Path relativePath;
                    String shortName;
                    Path eventPath = (Path)event.context();
                    if (eventPath.toString().endsWith(WINSCP_FILEPARTS_EXTENSION) || HANDLER_COOLDOWN.contains(shortName = ConfigWatcher.toCooldownHandlerName((relativePath = PLUGIN_FOLDER_PATH.relativize(resolvedPath = ((Path)key.watchable()).resolve(eventPath))).toString()))) continue;
                    boolean wasDeleted = event.kind() == StandardWatchEventKinds.ENTRY_DELETE || !Files.exists(resolvedPath);
                    try {
                        if (Files.isDirectory(resolvedPath) || Files.isHidden(resolvedPath)) {
                            continue;
                        }
                    }
                    catch (IOException iOException) {
                        // empty catch block
                    }
                    try {
                        size = wasDeleted ? -100L : Files.size(resolvedPath);
                    }
                    catch (Throwable ex) {
                        ex.printStackTrace();
                        size = -10000000L;
                    }
                    ScheduledTask previousFTPHandler = DELAYED_FTP_UPLOADS.getIfPresent(shortName);
                    if (previousFTPHandler != null) {
                        previousFTPHandler.cancel();
                    }
                    if (previousFTPHandler != null || !wasDeleted && size <= 0L) {
                        DELAYED_FTP_UPLOADS.put(shortName, CastelPlugin.taskScheduler().asyncLater(Duration.ofSeconds(5L), () -> {
                            try {
                                DELAYED_FTP_UPLOADS.invalidate(shortName);
                                BiConsumer<Path, WatchEvent.Kind<?>> handler = WATCHED_KEYS.get(key);
                                Objects.requireNonNull(handler, () -> "Handler for file " + resolvedPath + " (" + eventPath + ") is null");
                                handler.accept(relativePath, event.kind());
                            }
                            catch (Throwable ex) {
                                CLogger.error("Failed to handle FTP automatic reload for file: " + relativePath);
                                ex.printStackTrace();
                            }
                        }));
                        continue;
                    }
                    HANDLER_COOLDOWN.add(shortName);
                    try {
                        BiConsumer<Path, WatchEvent.Kind<?>> handler = WATCHED_KEYS.get(key);
                        Objects.requireNonNull(handler, () -> "Handler for file " + resolvedPath + " (" + eventPath + ") is null");
                        handler.accept(relativePath, event.kind());
                    }
                    catch (Throwable ex) {
                        CLogger.error("Failed to handle automatic reload for file: " + relativePath);
                        ex.printStackTrace();
                    }
                }
            }
            key.reset();
        }
    }

//    static void handleStructures(Path path, WatchEvent.Kind<?> kind) {
//        Path mainDir = plugin.getDataFolder().toPath();
//        Path structureDir = mainDir.relativize(mainDir.resolve("Structures"));
//        path = structureDir.relativize(path);
//        String file = path.toString();
//        file = file.substring(0, file.length() - 4);
//        StructureStyle style = StructureRegistry.getStyle(file);
//        MessageHandler.sendConsolePluginMessage("&2Detected changes for structure&8: &9" + file + (style == null ? " &4which is not a registered structure style, ignoring." : ""));
//        if (style != null) {
//            style.getConfig().reload();
//            StructureRegistry.validate(file, style.getConfig());
//            style.loadSettings();
//        }
//    }

//    static void handleTurrets(Path path, WatchEvent.Kind<?> kind) {
//        Path mainDir = plugin.getDataFolder().toPath();
//        Path turretsDir = mainDir.relativize(mainDir.resolve("Turrets"));
//        path = turretsDir.relativize(path);
//        String file = path.toString();
//        file = file.substring(0, file.length() - 4);
//        TurretStyle style = TurretRegistry.getStyle(file);
//        MessageHandler.sendConsolePluginMessage("&2Detected changes for turret&8: &9" + file + (style == null ? " &4which is not a registered turret style, ignoring." : ""));
//        if (style != null) {
//            style.getAdapter().reload();
//            TurretRegistry.validate(file, style.getAdapter());
//            style.loadSettings();
//        }
//    }

//    static void handleLanguageFile(Path path, WatchEvent.Kind<?> kind) {
//        String file = path.getFileName().toString();
//        SupportedLanguage lang = LanguageManager.getLanguageOrWarn(file = file.substring(0, file.length() - 4));
//        if (lang == null) {
//            return;
//        }
//        if (!lang.isInstalled()) {
//            return;
//        }
//        MessageHandler.sendConsolePluginMessage("&2Detected changes for language file&8: &9" + file);
//        LanguageManager.load(lang);
//    }

//    public static BiConsumer<Path, WatchEvent.Kind<?>> generateGUIHandlerForLang(final SupportedLanguage language) {
//        return new BiConsumer<Path, WatchEvent.Kind<?>>(){
//            private final Path folder;
//            {
//                this.folder = Kingdoms.getFolder().relativize(GUIConfig.getFolder().resolve(language.getLowerCaseName()));
//            }
//
//            @Override
//            public void accept(Path paths, WatchEvent.Kind<?> kind) {
//                if (!language.isInstalled()) {
//                    return;
//                }
//                Path relative = this.folder.relativize(paths);
//                if (Files.isDirectory(relative) || !relative.toString().endsWith(".yml")) {
//                    return;
//                }
//                String guiName = relative.toString().replace('\\', '/');
//                if ((guiName = guiName.substring(0, guiName.length() - 4)).startsWith("templates")) {
//                    return;
//                }
//                GUIObject gui = language.getGUI(guiName);
//                boolean isRegistered = SupportedLanguage.EN.getGUI(guiName) != null;
//                MessageHandler.sendConsolePluginMessage("&2Detected changes for GUI&8: &9" + language.getLowerCaseName() + '/' + guiName + (!isRegistered ? " &8(&4which is an unknown GUI&8)" : ""));
//                if (gui == null) {
//                    Path path = language.getGUIFolder().resolve(guiName + ".yml");
//                    File repoFile = language.getRepoPath().resolve("guis").resolve(guiName + ".yml").toFile();
//                    YamlWithDefaults adapter = GUIConfig.createAdapter(path.toFile(), repoFile);
//                    GUIConfig.loadAndRegisterGUI(language, adapter, guiName);
//                } else {
//                    GUIConfig.reload(gui, language);
//                }
//            }
//        };
//    }

    public static void handleNormalConfigs(Path path, WatchEvent.Kind<?> kind) {
        String file = path.toString();
        String simpleConfigName = file.toLowerCase(Locale.ENGLISH).replace('\\', '/').substring(0, file.length() - ".yml".length());
        FileWatcher custom = NORMAL_WATCHERS.get(simpleConfigName);
        if (custom != null) {
            custom.handle(new FileWatchEvent(path, kind));
            return;
        }
        switch (simpleConfigName) {
            case "config": {
//                ConfigWatcher.reload(Config.MAIN, file);
                StandardCastelPlaceholder.init();
                return;
            }
            case "claims": {
//                ConfigWatcher.reload(Config.CLAIMS, file);
                return;
            }
            case "map": {
//                ConfigWatcher.reload(Config.MAP, file);
                return;
            }
            case "ranks": {
//                ConfigWatcher.reload(Config.RANKS, file);
                Rank.init();
                return;
            }
//            case "structures": {
//                ConfigWatcher.reload(CastelConfig.STRUCTURES, file);
//                StructureRegistry.getStyles().values().forEach(style -> {
//                    style.getConfig().reload();
//                    style.loadSettings();
//                });
//                return;
//            }
//            case "turrets": {
//                ConfigWatcher.reload(CastelConfig.TURRETS, file);
//                return;
//            }
            case "protection-signs": {
//                ConfigWatcher.reload(Config.PROTECTION_SIGNS, file);
                return;
            }
            case "relations": {
//                ConfigWatcher.reload(Config.RELATIONS, file);
                GuildRelation.init();
                return;
            }
            case "misc-upgrades": {
//                ConfigWatcher.reload(Config.MISC_UPGRADE, file);
                return;
            }
//            case "chat": {
//                ConfigWatcher.reload(CastelConfig.CHAT, file);
//                KingdomsChatChannel.registerChannels();
//                return;
//            }
            case "powers": {
//                ConfigWatcher.reload(Config.POWERS, file);
                Powerup.init();
                return;
            }
            case "resource-points": {
//                ConfigWatcher.reload(Config.RESOURCE_POINTS, file);
                ResourcePointManager.loadSettings();
            }
        }
    }

    static {
        WatchService watchService;
        HANDLER_COOLDOWN = new ExpirableSet<>(3L, TimeUnit.SECONDS, false);
        DELAYED_FTP_UPLOADS = CacheHandler.newBuilder().expireAfterWrite(Duration.ofSeconds(5L)).build();
        WATCHED_KEYS = new IdentityHashMap<>();
        plugin = CastelPlugin.getInstance();
        PLUGIN_FOLDER_PATH = plugin.getDataFolder().toPath();
        accepting = true;
        Path basePath = CastelPlugin.getInstance().getDataFolder().toPath().toAbsolutePath();
        try {
            watchService = basePath.getFileSystem().newWatchService();
        }
        catch (IOException e) {
            CLogger.error("Failed to register config file watchers:");
            e.printStackTrace();
            watchService = null;
        }
        WATCH_SERVICE = watchService;
        NORMAL_WATCHERS = new HashMap<String, FileWatcher>();
    }
}


