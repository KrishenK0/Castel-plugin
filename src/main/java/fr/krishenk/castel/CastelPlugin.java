package fr.krishenk.castel;

import fr.krishenk.castel.commands.CastelCommandHandler;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.group.model.relationships.RelationAttributeRegistry;
import fr.krishenk.castel.constants.group.model.relationships.StandardRelationAttribute;
import fr.krishenk.castel.constants.metadata.CastelMetadataRegistry;
import fr.krishenk.castel.constants.player.GuildPermissionRegistry;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.DataManager;
import fr.krishenk.castel.data.StartupCache;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.compiler.placeholders.StandardCastelPlaceholder;
import fr.krishenk.castel.managers.*;
import fr.krishenk.castel.managers.abstraction.ProlongedTask;
import fr.krishenk.castel.managers.book.BookManager;
import fr.krishenk.castel.managers.chat.ChatInputManager;
import fr.krishenk.castel.managers.inviterequests.JoinRequests;
import fr.krishenk.castel.managers.land.ChunkManager;
import fr.krishenk.castel.managers.land.LandEffectsManager;
import fr.krishenk.castel.managers.land.LandProtectionManager;
import fr.krishenk.castel.managers.land.claiming.AutoClaimManager;
import fr.krishenk.castel.managers.land.protection.*;
import fr.krishenk.castel.managers.logger.CastelLogger;
import fr.krishenk.castel.managers.mails.MailUserAgent;
import fr.krishenk.castel.managers.protectionsign.ProtectionSignManager;
import fr.krishenk.castel.managers.teleportation.TpManager;
import fr.krishenk.castel.scheduler.AsyncScheduledTasks;
import fr.krishenk.castel.scheduler.BukkitSchedulerAdapter;
import fr.krishenk.castel.scheduler.TaskScheduler;
import fr.krishenk.castel.services.ServiceVault;
import fr.krishenk.castel.services.SoftService;
import fr.krishenk.castel.services.worldguard.ServiceWorldGuardSeven;
import fr.krishenk.castel.utils.TCListener;
import fr.krishenk.castel.utils.debugging.CastelDebug;
import fr.krishenk.castel.utils.platform.Platform;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public final class CastelPlugin extends JavaPlugin {

    @Getter
    private static CastelPlugin instance;

    private final JavaPlugin loader = this;
    private boolean enabled = false;
    private boolean loaded = false;
    private boolean isDisabling = false;
    private long lastFullSave;

    private String compileCommitSHA = "199a56173f51c34a5e353a201184df518f106b20";
    public TCListener listener;
    static TaskScheduler taskScheduler;
    public static final String channel = "castel:main";
    private static String connectionURL;
    private CastelDataCenter dataCenter;
    private CastelBootstrapProvider bootstrapProvider;
    private final GuildPermissionRegistry permissionRegistry = new GuildPermissionRegistry();
    private final RelationAttributeRegistry relationAttributeRegistry = new RelationAttributeRegistry();
    private final CastelMetadataRegistry metadataRegistry = new CastelMetadataRegistry();


    public CastelPlugin() {
        if (instance != null || this.enabled)
            throw new IllegalStateException("Plugin loaded twice");
        instance = this;
        if (!Platform.BUKKIT.isAvailable()) {
            throw new IllegalArgumentException("Your server doesn't seem to be running on a bukkit platform. Please contact the plugin developers.");
        }
        if (!ReflectionUtils.supports(9)) {
            throw new IllegalStateException("The plugin doesn't support 1.8 and below versions. Visit here for more info: https://github.com/CryptoMorin/KingdomsX/wiki/FAQ#support-for-18-and-lower-versions");
        }
        ReloadProtection.ensureLoadOnce();
        //ConfigSection pluginSettings = new ConfigSection(YamlContainer.getRootOf("plugin.yml", this.getResource("plugin.yml")));
        this.bootstrapProvider = new CastelBootstrapProvider(this);
        //this.compileCommitSHA = Objects.requireNonNull(pluginSettings.getString("compile-master-sha"), () -> pluginSettings.getNode().toString());
        taskScheduler = new BukkitSchedulerAdapter(this, this.bootstrapProvider);
    }

    private static void registerServices() {
        for (SoftService service : SoftService.values()) {
            if (!service.isAvailable()) continue;
            try {
                service.getService().enable();
            } catch (Throwable e) {
                CLogger.info("&4Failed to register &e" + service.getName() + " &4services&8:");
                e.printStackTrace();
            }
        }
        if (SoftService.VAULT.isAvailable()) {
            Bukkit.getScheduler().runTaskLater(CastelPlugin.getInstance(), () -> {
                if (!ServiceVault.isAvailable(ServiceVault.Component.ECO))
                    CLogger.info("Unable to start Vault economy services. Install an economy plugin before using Guild economics.");
                if (!ServiceVault.isAvailable(ServiceVault.Component.CHAT))
                    CLogger.info("Unable to start Vault economy services. Install a prefix/suffix plugin (mostly managed by permission plugins).");
                if (!ServiceVault.isAvailable(ServiceVault.Component.ECO))
                    CLogger.info("Unable to start Vault economy services. Install a permission plugin before using Guild permission checks.");
            }, 0L);
        }
    }

    public void onLoad() {
        if (Bukkit.getPluginManager().getPlugin("WorldGuard") != null && ReflectionUtils.supports(13)) {
            try {
                ServiceWorldGuardSeven.init();
            } catch (Throwable e) {
                getInstance().getLogger().info("Successfully registered 'claimable' WorldGuard flag");
                e.printStackTrace();
            }
        }
        StandardGuildPermission.init();
        StandardRelationAttribute.init();
        this.loaded = true;
    }

    @Override
    public void onEnable() {
        if (!this.loaded) {
            this.getLogger().severe("-------------------------------------------------------------------");
            this.getLogger().severe("The plugin did not load correctly. Please check your startup logs.");
            this.getLogger().severe("-------------------------------------------------------------------");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        ReloadProtection.ensureLoadOnce();
//        ConfigManager manager = new ConfigManager(this);
//        manager.createDataFolderIfMissing();
        createDataFolderIfMissing();
        StandardCastelPlaceholder.init();
        LanguageManager.loadAll();

        CastelPluginPermission.init();
        new CastelCommandHandler(this);

        Rank.init();
        GuildRelation.init();
        SoftService.reportAvailability();
        JoinRequests.registerMetaHandlers();
        ProlongedTask.init();

        this.dataCenter = new CastelDataCenter(this);
        StartupCache.init(this);

        registerServices();
        this.registerAllEvents();

        this.listener = new TCListener(this);
        this.getServer().getMessenger().registerOutgoingPluginChannel(this, channel);

        //Database.initDB();
        this.enabled = true;
    }

    @Override
    public void onDisable() {
        this.isDisabling = true;
        if (!this.loaded || !this.enabled) {
            this.getLogger().severe("Plugin did not load properly to disable normally. Force disabling...");
            return;
        }
        CLogger.info("Disabling thread workers...");
        taskScheduler.shutdownScheduler();
        taskScheduler.shutdownExecutor();
        for (Map.Entry<BukkitTask, Runnable> task : AsyncScheduledTasks.getTasks().entrySet()) {
            task.getValue().run();
            task.getKey().cancel();
        }

        CLogger.info("Closing the logger...");
        CastelLogger.getMain().close();
        this.getLogger().info("Unregistering plugin services...");
        for (SoftService service : SoftService.values()) {
            if (!service.isAvailable()) continue;
            try {
                service.getService().disable();
            }
            catch (Throwable ex) {
                MessageHandler.sendConsolePluginMessage("&4Failed to disable &e" + service.getName() + " &4services&8:");
                ex.printStackTrace();
            }
        }
//        this.getLogger().info("Removing kingdom mobs...");
//        KingdomEntityRegistry.removeAllKingdomMobs();
//        this.getLogger().info("Removing temporary entities...");
//        ChairManager.removeAll();
//        EntityFactory.removeAllTemporaryEntities();
        this.getLogger().info("Removing temporary items...");
        BookManager.removeAll();
        if (LandExplosionManager.REGENERATE) {
            this.getLogger().info("Force regenerating all exploded stuff...");
            LandExplosionManager.forceOngoingRegenerations();
        }
        this.signalFullSave();
        //this.getLogger().info("Done, goodbye cruel world!");
    }

    public void saveAllData() {
        CLogger.debug(CastelDebug.SAVE_ALL, "Saving all data");
        this.lastFullSave = System.currentTimeMillis();
        Objects.requireNonNull(this.dataCenter, "Plugin data handler is null");
        for (DataManager<?, ?> dataManager : this.dataCenter.getAllDataManagers()) {
            this.getLogger().info("Saving " + dataManager.getDisplayName() + "...");
            dataManager.saveAll(true);
        }
    }

    public void signalFullSave() {
        Duration lastSave = Duration.ofMillis(this.lastFullSave);
        Duration shouldSaveAter = lastSave.plus(Duration.ofSeconds(30L));
        Duration current = Duration.ofMillis(System.currentTimeMillis());
        if (!current.minus(shouldSaveAter).isNegative()) {
            this.saveAllData();
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("group") && args[0].equalsIgnoreCase("create")) {
           if (sender instanceof  Player) {
               Player p = (Player) sender;
                /*
               Connection connection = Database.getConnection();
               PreparedStatement statement;
               try {
                   statement = connection.prepareStatement("INSERT INTO Groups(UUID, LeaderUUID, Name) VALUES(?, ?, ?);");
                   statement.setString(1, UUID.nameUUIDFromBytes(p.getName().getBytes()).toString());
                   statement.setString(2, p.getUniqueId().toString());
                   statement.setString(3, args[1]);
                   statement.execute();
                   System.out.println(statement.getGeneratedKeys());
                   connection.close();
               } catch (SQLException ex) {
                   throw new RuntimeException(ex);
               }*/
           }
        }
        return true;
    }

    public static String getConnectionURL() {
        return connectionURL;
    }

    public static TaskScheduler taskScheduler() {
        return taskScheduler;
    }

    public void registerAllEvents() {
        // TODO : A faire
        GeneralizedEventWatcher.init();
//        this.registerEvent(new LogManager());
        this.registerEvent(new ReloadProtection());
//        this.registerEvent(new InteractiveGUIManager());
        this.registerEvent(new JoinAndLeaveManager());
        this.registerEvent(new AutoClaimManager());
        this.registerEvent(new PowerupManager());
//        this.registerEvent(new CastelItemManager());
        if (!PvPManager.isPvPType(PvPManager.PvPType.DISABLED)) {
            this.registerEvent(new PvPManager());
        }
//        this.registerEvent(new InvasionManager());
//        this.registerEvent(new KingdomEntityManager());
        this.registerEvent(new TpManager());
        this.registerEvent(new ChatInputManager());
//        this.registerEvent(new GuildGuardManager());
//        this.registerEvent(new ChairManager());
        this.registerEvent(new BookManager());
        this.registerEvent(new JoinRequests());
//        this.registerEvent(new RegulatorManager());
//        this.registerEvent(new SiegeManager());
//        this.registerEvent(new FuelManager());
        this.registerEvent(new MailUserAgent());
        this.registerEvent(new GuildVaultManager());
        this.registerEvent(new ChunkManager());
//        this.registerEvent(new CastelItemHologramManager());
        this.registerEvent(new MiscUpgradeManager());
        this.registerEvent(new LandExplosionManager());
        this.registerEvent(new LandPistonManager());
        this.registerEvent(new LandChangeWatcher());
        this.registerEvent(new LandProtectionManager());
//        this.registerEvent(new KingdomItemGUIProtection());
        this.registerEvent(new LandEffectsManager());
        if (XMaterial.supports(13)) {
            if (Config.Claims.BEACON_PROTECTED_EFFECTS.getManager().getBoolean()) {
                this.registerEvent(new LandEffectsManager.BeaconManager());
            }
//            this.registerEvent(new InvasionManager.AdvancedInvasionManager());
        }
//        new KingdomChatChannelListener(this);
        if (Config.GUILD_FLY_ENABLED.getBoolean()) {
            this.registerEvent(new FlyManager());
        }
        if (Config.ProtectionSigns.ENABLED.getManager().getBoolean()) {
            this.registerEvent(new ProtectionSignManager(this));
        }
//        if (KingdomsConfig.Turrets.ENABLED.getManager().getBoolean()) {
//            this.registerEvent(new TurretFactory());
//            this.registerEvent(new ManualTurretHandler());
//            this.registerEvent(new SoldierManager());
//            this.registerEvent(new LandTurretCacheOptimizerWatcher());
//        }
    }

    private void registerEvent(Listener listener) {
        Bukkit.getPluginManager().registerEvents(listener, this.loader);
    }

    public boolean isDisabling() {
        return isDisabling;
    }

    public JavaPlugin getLoader() {
        return this;
    }

    public void createDataFolderIfMissing() {
        try {
            Files.createDirectories(getDataFolder().toPath());
        } catch (IOException e) {
            getLogger().info("Failed to create the plugin's folder:");
            e.printStackTrace();
        }
    }

    public static Path getFolder() {
        return CastelPlugin.getInstance().getDataFolder().toPath();
    }

    public static Path getPath(String path) {
        return CastelPlugin.getFolder().resolve(path);
    }

    public CastelDataCenter getDataCenter() { return dataCenter; }

    public GuildPermissionRegistry getPermissionRegistry() {
        return this.permissionRegistry;
    }

    public RelationAttributeRegistry getRelationAttributeRegistry() { return relationAttributeRegistry; }

    public CastelMetadataRegistry getMetadataRegistry() { return metadataRegistry; }

    public String getCompileCommitSHA() {
        return this.compileCommitSHA;
    }
}
