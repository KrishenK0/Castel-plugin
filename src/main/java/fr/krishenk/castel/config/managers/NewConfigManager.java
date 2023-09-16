package fr.krishenk.castel.config.managers;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;
import fr.krishenk.castel.libs.snakeyaml.validation.ValidationFailure;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NewConfigManager {
    private final CastelPlugin plugin;
    private static final FileConfiguration GLOBALS;
    private static final List<FileConfiguration> CONFIGS;

//    public static void beforeWrite(YamlContainer adapter) {
//        ConfigWatcher.beforeWrite(adapter);
//    }

    public static List<FileConfiguration> getConfigs() {
        return CONFIGS;
    }

    public static void registerAsMainConfig(FileConfiguration yml) {
        CONFIGS.add(yml);
    }

    public void setupWatchService() {
        Bukkit.getScheduler().runTaskLaterAsynchronously(CastelPlugin.getInstance(), ConfigWatcher::setupWatchService, 20L);
    }

    public static void addAllConfigs() {
        CONFIGS.addAll(Arrays.asList(Config.MAIN, Config.CLAIMS));
    }

    public static void addConfig(FileConfiguration config) {
        CONFIGS.add(config);
    }

    public static void registerNormalWatcher(String normalRelativePath, FileWatcher watcher) {
        ConfigWatcher.NORMAL_WATCHERS.put(normalRelativePath, watcher);
    }

//    public void generateSchema() {
//        Path pluginFolder = this.plugin.getDataFolder().toPath();
//        Path schemaFolder = pluginFolder.resolve("schema");
//        try {
//            Files.createDirectory(schemaFolder);
//        }
//        catch (IOException e) {
//            e.printStackTrace();
//        }
//        for (FileConfiguration config : CONFIGS) {
//            Path relative = pluginFolder.relativize(config.getFile().toPath());
//            Path schema = schemaFolder.resolve(relative);
//            try {
//                Files.createDirectories(schema.getParent());
//            }
//            catch (IOException e) {
//                e.printStackTrace();
//            }
//            Validator.implicitSchemaGenerator(config.getDefaults().getNode(), schema);
//        }
//    }

    public static void onDisable() {
        if (ConfigWatcher.WATCH_SERVICE != null) {
            try {
                ConfigWatcher.WATCH_SERVICE.close();
            }
            catch (IOException e) {
                CLogger.error("Failed to close config watchers:");
                e.printStackTrace();
            }
        }
    }

//    static void validate(FileConfiguration config) {
//        NewConfigManager.warnAboutValidation(config.getName(), config.validate());
//    }

    public static void warnAboutValidation(String name, List<ValidationFailure> exceptions) {
        if (exceptions.isEmpty()) {
            return;
        }
        StringBuilder warnings = new StringBuilder(exceptions.size() * 100);
        warnings.append(Lang.PREFIX.parse()).append(ChatColor.RED).append("Error while validating ").append(ChatColor.GOLD).append(name).append(ChatColor.RED).append(" config:").append('\n');
        int i = 0;
        for (ValidationFailure exception : exceptions) {
            Mark marker = exception.getMarker();
            boolean isError = exception.getSeverity() == ValidationFailure.Severity.ERROR;
            warnings.append(ChatColor.GRAY).append('[').append(isError ? ChatColor.RED : ChatColor.YELLOW).append(isError ? "Error" : "Warning").append(ChatColor.GRAY).append("] ").append(isError ? ChatColor.RED : ChatColor.YELLOW);
            warnings.append(exception.getMessage()).append(" at line ").append(ChatColor.GOLD).append(marker.getLine()).append(ChatColor.DARK_GRAY).append(':').append('\n').append(ChatColor.YELLOW).append(marker.createSnippet(ChatColor.DARK_RED.toString())).append('\n');
            if (++i == exceptions.size()) continue;
            warnings.append(ChatColor.DARK_GRAY).append("-------------------------------------------------------").append('\n');
        }
        warnings.append(ChatColor.GRAY).append("============================================================");
        Bukkit.getConsoleSender().sendMessage(warnings.toString());
    }

//    public void validateConfigs() {
//        for (FileConfiguration config : CONFIGS) {
//            NewConfigManager.validate(config);
//        }
//    }

    public static ConfigurationSection getGlobals() {
        return GLOBALS.getRoot();
    }

    public static FileConfiguration getGlobalsAdapter() {
        return GLOBALS;
    }

    public NewConfigManager(CastelPlugin plugin) {
        this.plugin = plugin;
    }

//    public static void updateConfigs() {
//        for (FileConfiguration config : CONFIGS) {
//            config.update();
//        }
//    }

    public void createDataFolderIfMissing() {
        try {
            Files.createDirectories(this.plugin.getDataFolder().toPath());
        }
        catch (IOException e) {
            CLogger.error("Failed to create the plugin's folder:");
            e.printStackTrace();
        }
    }

    static {
        CONFIGS = new ArrayList<>(20);
        InputStream in = CastelPlugin.getInstance().getResource("global.yml");
        GLOBALS = YamlConfiguration.loadConfiguration(new InputStreamReader(Objects.requireNonNull(in)));
//        GLOBALS = new YamlFile(new File(CastelPlugin.getInstance().getDataFolder(), "globals.yml")).load();
        String entry = "config-migration.last-fresh-version";
        if (GLOBALS.getString(entry) == null) {
            GLOBALS.set(entry, CastelPlugin.getInstance().getDescription().getVersion());
            CastelPlugin.getInstance().saveResource("global.yml", true);
        }
    }
}


