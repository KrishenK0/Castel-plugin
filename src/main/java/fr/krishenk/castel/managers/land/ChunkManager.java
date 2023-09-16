package fr.krishenk.castel.managers.land;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.data.managers.LandManager;
import fr.krishenk.castel.events.lands.AsyncBatchLandLoadEvent;
import fr.krishenk.castel.events.lands.LandChangeEvent;
import fr.krishenk.castel.events.lands.LandUnloadEvent;
import fr.krishenk.castel.lang.Config;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

import java.time.Duration;
import java.util.*;
import java.util.function.Consumer;

public class ChunkManager implements Listener {

    private static void handleLandChange(PlayerMoveEvent event, PlayerTeleportEvent.TeleportCause cause) {
        SimpleChunkLocation fromChunk = SimpleChunkLocation.of(event.getFrom());
        SimpleChunkLocation toChunk = SimpleChunkLocation.of(event.getTo());
        if (fromChunk.getX() != toChunk.getX() || fromChunk.getZ() != fromChunk.getZ()) {
            LandChangeEvent chunkChangeEvent = new LandChangeEvent(event, fromChunk, toChunk, cause);
            Bukkit.getPluginManager().callEvent(chunkChangeEvent);
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        Chunk chunk = event.getChunk();
        LandManager.queryLand(SimpleChunkLocation.of(chunk));
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        Chunk chunk = event.getChunk();
        SimpleChunkLocation loc = SimpleChunkLocation.of(chunk);
        if (CastelPlugin.getInstance().getDataCenter().getLandManager().isLoaded(loc)) {
            Land land = loc.getLand();
            if (land != null && land.isClaimed()) {
                LandUnloadEvent unloadEvent = new LandUnloadEvent(land);
                Bukkit.getPluginManager().callEvent(unloadEvent);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onChunkChange(PlayerMoveEvent event) {
        handleLandChange(event, null);
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        handleLandChange(event, event.getCause());
    }

    static {
        Duration delay = Duration.ofSeconds(30L);
        CastelPlugin.taskScheduler().asyncRepeating(delay, delay, () -> {
            LandManager landManager = CastelPlugin.getInstance().getDataCenter().getLandManager();
            Map<SimpleChunkLocation, List<Consumer<Land>>> cloned = new HashMap<>(LandManager.QUERIED_LANDS);
            LandManager.QUERIED_LANDS.clear();
            Collection<Land> loaded = landManager.load(cloned.keySet());

            for (Land land : loaded) {
                List<Consumer<Land>> consumers = cloned.get(land.getLocation());
                for (Consumer<Land> consumer : consumers) {
                    consumer.accept(land);
                }
            }

            AsyncBatchLandLoadEvent loadEvent = new AsyncBatchLandLoadEvent(loaded);
            Bukkit.getPluginManager().callEvent(loadEvent);
        });

        int queried = 0;
        for (World world : Bukkit.getWorlds()) {
            if (Config.DISABLED_WORLDS.isInDisabledWorld(world)) continue;
            for (Chunk chunk : world.getLoadedChunks()) {
                ++queried;
                LandManager.queryLand(SimpleChunkLocation.of(chunk));
            }
        }
        if (queried != 0) CLogger.info("A total of " + queried + " loaded chunks has been queried.");
    }
}
