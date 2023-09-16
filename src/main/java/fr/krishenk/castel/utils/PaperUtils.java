package fr.krishenk.castel.utils;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.utils.platform.Platform;
import io.papermc.lib.PaperLib;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class PaperUtils {
    public static void prepareChunksIgnored(int timer, Location loc) {
        if (Platform.PAPER.isAvailable()) {
            boolean isUrgent = timer < 5;
            World world = loc.getWorld();
            int viewDist = Bukkit.getViewDistance() / 2;
            for (SimpleChunkLocation chunk : SimpleChunkLocation.of(loc).getChunksAround(viewDist)) {
                PaperLib.getChunkAtAsync(world, chunk.getX(), chunk.getZ(), true, isUrgent);
            }
        }
    }

    public static CompletableFuture<Void> prepareChunks(SimpleChunkLocation centerChunk) {
        return !Platform.PAPER.isAvailable() ? CompletableFuture.completedFuture(null) : prepareChunks(Arrays.asList(centerChunk.getChunksAround(Bukkit.getViewDistance() / 2)));
    }

    public static CompletableFuture<Void> prepareChunks(Collection<SimpleChunkLocation> chunks) {
        if (!chunks.isEmpty() && Platform.PAPER.isAvailable()) {
            List<CompletableFuture<Chunk>> tasks = new ArrayList<>();
            World world = chunks.iterator().next().getBukkitWorld();
            for (SimpleChunkLocation chunk : chunks) {
                tasks.add(PaperLib.getChunkAtAsync(world, chunk.getX(), chunk.getZ(), true, false));
            }

            return CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0]));
        }
        return CompletableFuture.completedFuture(null);
    }
}
