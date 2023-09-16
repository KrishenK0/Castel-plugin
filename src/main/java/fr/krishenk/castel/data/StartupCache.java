package fr.krishenk.castel.data;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class StartupCache {
    private static final List<Consumer<CastelPlugin>> ON_LOAD = new ArrayList<>(10);
    private static boolean loaded = false;

    public static void whenLoaded(Consumer<CastelPlugin> runnable) {
        Objects.requireNonNull(runnable, "Load consumer cannot be null");
        if (loaded) runnable.accept(CastelPlugin.getInstance());
        else ON_LOAD.add(runnable);
    }

    public static void init(CastelPlugin plugin) {
        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
            try {
                plugin.getPermissionRegistry().lock();
                plugin.getRelationAttributeRegistry().lock();
                plugin.getMetadataRegistry().lock();
            } catch (IllegalAccessException e) {}
            StartupCache.loadGuilds(plugin);
            StartupCache.loadLands(plugin);
            loaded = true;
            for (Consumer<CastelPlugin> runnable : ON_LOAD) {
                runnable.accept(plugin);
            }
        }, 20L);
    }

    private static void loadGuilds(CastelPlugin plugin) {
        CLogger.info("&2Setting up guilds data...");
        long start = System.currentTimeMillis();
        int loaded = plugin.getDataCenter().getGuildManager().loadAllData();
        CLogger.info("&2Done, loaded a total of &6" + loaded + " &2guilds. Took &6" + (System.currentTimeMillis() - start) + "ms");
    }

    private static void loadLands(CastelPlugin plugin) {
        CLogger.info("&2Loading lands data...");
        long start = System.currentTimeMillis();
        int loaded = plugin.getDataCenter().getLandManager().loadAllData();
        CLogger.info("&2Done, loaded a total of &6" + loaded + " &2lands. Took &6" + (System.currentTimeMillis() - start) + "ms");
    }
}


