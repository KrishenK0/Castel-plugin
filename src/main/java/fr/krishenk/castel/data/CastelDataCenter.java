package fr.krishenk.castel.data;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.data.handlers.DataHandler;
import fr.krishenk.castel.data.managers.CastelPlayerManager;
import fr.krishenk.castel.data.managers.GuildManager;
import fr.krishenk.castel.data.managers.LandManager;
import fr.krishenk.castel.data.managers.MessageTransferAgent;
import org.bukkit.scheduler.BukkitRunnable;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class CastelDataCenter {
    public static final String DATA_FOLDER_NAME = "data";
    public static final Path DATA_FOLDER = CastelPlugin.getPath("data");
    private static CastelDataCenter INSTANCE;
    private final CastelPlayerManager castelPlayerManager;
    private final GuildManager guildManager;
    private final LandManager landManager;
    private final MessageTransferAgent mtg;

    public CastelDataCenter(CastelPlugin plugin) {
        INSTANCE = this;
        this.castelPlayerManager = new CastelPlayerManager(this);
        this.guildManager = new GuildManager(this);
        this.landManager = new LandManager(this);
        this.mtg = new MessageTransferAgent(this);
        long time = DataManager.INTERVAL;
        time = TimeUnit.MILLISECONDS.toSeconds(time);
        new BukkitRunnable(){
            @Override
            public void run() {
                CLogger.info("&2Auto saving... Checking &6" + CastelDataCenter.this.castelPlayerManager.size() + "&2 players.");
            }
        }.runTaskTimerAsynchronously(plugin, time *= 20L, time);
    }

    public <K, T extends CastelObject<K>> CastelDatabase<K, T> constructDatabase(String name, String table, DataHandler<K, T> dataHandler) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(table);
        Objects.requireNonNull(dataHandler);
        Path path = CastelPlugin.getPath(DATA_FOLDER_NAME);
        SQLConnectionProvider.setDefaultProviderIfNull(path);
        return new Database<>(table, dataHandler, SQLConnectionProvider.getDefaultProvider());
    }

    public List<DataManager<?, ?>> getAllDataManagers() {
        return Arrays.asList(this.castelPlayerManager, this.guildManager, this.landManager, this.mtg);
    }

    public static CastelDataCenter get() {
        return INSTANCE;
    }

    public CastelPlayerManager getCastelPlayerManager() {
        return this.castelPlayerManager;
    }

    public GuildManager getGuildManager() { return this.guildManager; }

    public LandManager getLandManager() { return landManager; }

    public MessageTransferAgent getMTG() {
        return this.mtg;
    }
}
