package fr.krishenk.castel.config.managers;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.config.CastelConfig;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;
import fr.krishenk.castel.libs.snakeyaml.validation.ValidationFailure;
import fr.krishenk.castel.libs.snakeyaml.validation.Validator;
import fr.krishenk.castel.utils.config.ConfigSection;
import fr.krishenk.castel.utils.config.adapters.YamlContainer;
import fr.krishenk.castel.utils.config.adapters.YamlFile;
import fr.krishenk.castel.utils.config.adapters.YamlResource;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConfigManager {
    private final CastelPlugin plugin;
    private static final YamlFile GLOBALS;
    private static final List<YamlResource> CONFIGS;

    public static void beforeWrite(YamlContainer adapter) {
        ConfigWatcher.beforeWrite(adapter);
    }

    public static List<YamlResource> getConfigs() {
        return CONFIGS;
    }

    public static void registerAsMainConfig(YamlResource yml) {
        CONFIGS.add(yml);
    }

    public void setupWatchService() {
        Bukkit.getScheduler().runTaskLaterAsynchronously((Plugin)CastelPlugin.getInstance(), ConfigWatcher::setupWatchService, 20L);
    }

    public static void addAllConfigs() {
        CONFIGS.addAll(Arrays.asList(CastelConfig.MAIN, CastelConfig.RANKS, CastelConfig.CLAIMS, CastelConfig.RELATIONS, CastelConfig.POWERS, CastelConfig.RESOURCE_POINTS, CastelConfig.PROTECTION_SIGNS, CastelConfig.MAP, CastelConfig.MISC_UPGRADE, CastelConfig.CHAT));
    }

    public static void addConfig(YamlResource config) {
        CONFIGS.add(config);
    }

    public static void registerNormalWatcher(String normalRelativePath, FileWatcher watcher) {
        ConfigWatcher.NORMAL_WATCHERS.put(normalRelativePath, watcher);
    }

    public void generateSchema() {
        Path pluginFolder = this.plugin.getDataFolder().toPath();
        Path schemaFolder = pluginFolder.resolve("schema");
        try {
            Files.createDirectory(schemaFolder, new FileAttribute[0]);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        for (YamlResource config : CONFIGS) {
            Path relative = pluginFolder.relativize(config.getFile().toPath());
            Path schema = schemaFolder.resolve(relative);
            try {
                Files.createDirectories(schema.getParent(), new FileAttribute[0]);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            Validator.implicitSchemaGenerator(config.getDefaults().getNode(), schema);
        }
    }

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

    static void validate(YamlResource config) {
        ConfigManager.warnAboutValidation(config.getFile().getName(), config.validate());
    }

    public static void warnAboutValidation(String name, List<ValidationFailure> exceptions) {
        if (exceptions.isEmpty()) {
            return;
        }
        StringBuilder warnings = new StringBuilder(exceptions.size() * 100);
        //warnings.append(KingdomsLang.PREFIX.parse(new Object[0])).append((Object)ChatColor.RED).append("Error while validating ").append((Object)ChatColor.GOLD).append(name).append((Object)ChatColor.RED).append(" config:").append('\n');
        int i = 0;
        for (ValidationFailure exception : exceptions) {
            Mark marker = exception.getMarker();
            boolean isError = exception.getSeverity() == ValidationFailure.Severity.ERROR;
            warnings.append((Object)ChatColor.GRAY).append('[').append((Object)(isError ? ChatColor.RED : ChatColor.YELLOW)).append(isError ? "Error" : "Warning").append((Object)ChatColor.GRAY).append("] ").append((Object)(isError ? ChatColor.RED : ChatColor.YELLOW));
            warnings.append(exception.getMessage()).append(" at line ").append((Object)ChatColor.GOLD).append(marker.getLine()).append((Object)ChatColor.DARK_GRAY).append(':').append('\n').append((Object)ChatColor.YELLOW).append(marker.createSnippet(ChatColor.DARK_RED.toString())).append('\n');
            if (++i == exceptions.size()) continue;
            warnings.append((Object) ChatColor.DARK_GRAY).append("-------------------------------------------------------").append('\n');
        }
        warnings.append((Object)ChatColor.GRAY).append("============================================================");
        Bukkit.getConsoleSender().sendMessage(warnings.toString());
    }

    public void validateConfigs() {
        for (YamlResource config : CONFIGS) {
            ConfigManager.validate(config);
        }
    }

    public static ConfigSection getGlobals() {
        return GLOBALS.getConfig();
    }

    public static YamlFile getGlobalsAdapter() {
        return GLOBALS;
    }

    public ConfigManager(CastelPlugin plugin) {
        this.plugin = plugin;
    }

    public static void updateConfigs() {
        for (YamlResource config : CONFIGS) {
            config.update();
        }
    }

    public void createDataFolderIfMissing() {
        try {
            Files.createDirectories(this.plugin.getDataFolder().toPath(), new FileAttribute[0]);
        }
        catch (IOException e) {
            CLogger.error("Failed to create the plugin's folder:");
            e.printStackTrace();
        }
    }

    static {
        CONFIGS = new ArrayList<>(20);
        GLOBALS = new YamlFile(new File(CastelPlugin.getInstance().getDataFolder(), "globals.yml")).load();
        String[] entry = new String[]{"config-migration", "last-fresh-version"};
        if (GLOBALS.getConfig().getString(entry) == null) {
            GLOBALS.getConfig().set(entry, (Object)CastelPlugin.getInstance().getDescription().getVersion());
            GLOBALS.saveConfig();
        }
    }
}


