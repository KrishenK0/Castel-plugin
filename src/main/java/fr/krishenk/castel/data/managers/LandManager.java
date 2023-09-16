package fr.krishenk.castel.data.managers;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.DataManager;
import fr.krishenk.castel.data.handlers.DataHandlerLand;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class LandManager extends DataManager<SimpleChunkLocation, Land> {
    public static final Map<SimpleChunkLocation, List<Consumer<Land>>> QUERIED_LANDS = new ConcurrentHashMap<>(100);

    public LandManager(CastelDataCenter dataCenter) {
        super("lands", dataCenter.constructDatabase("Lands", "lands", new DataHandlerLand()), true, false);
        this.autoSave(CastelPlugin.getInstance());
    }

    public CompletableFuture<Collection<Land>> getLands(Collection<SimpleChunkLocation> locations) {
        CompletableFuture<Collection<Land>> completableFuture = new CompletableFuture<>();
        CastelPlugin.taskScheduler().async().execute(() -> {
            ArrayList<Land> loaded = new ArrayList<>(locations.size());
            Collection<SimpleChunkLocation> chosenLocs = locations;
            for (SimpleChunkLocation location : locations) {
                Land land = this.getDataIfLoaded(location);
                if (land == null) continue;
                loaded.add(land);
                if (chosenLocs == locations) chosenLocs = new HashSet<>(locations);
                chosenLocs.remove(location);
            }
            if (chosenLocs.isEmpty()) completableFuture.complete(loaded);
            else {
                BulkChunkHandler handler = new BulkChunkHandler((Set<SimpleChunkLocation>) chosenLocs, loaded, completableFuture);
                for (SimpleChunkLocation chosenLoc : chosenLocs) {
                    LandManager.queryLand(chosenLoc, handler);
                }
            }
        });
        return completableFuture;
    }

    public static void queryLand(SimpleChunkLocation chunk) {
        LandManager.queryLand(chunk, null);
    }

    public static void queryLand(SimpleChunkLocation chunk, Consumer<Land> consumer) {
        List<Consumer<Land>> query = QUERIED_LANDS.get(chunk);
        if (query == null) {
            query = consumer == null ? new ArrayList<>() : new ArrayList<>(1);
            QUERIED_LANDS.put(chunk, query);
        }
        if (consumer != null) query.add(consumer);
    }

    private static class BulkChunkHandler implements Consumer<Land> {
        private final Set<SimpleChunkLocation> locations;
        private final List<Land> loaded;
        private final CompletableFuture<Collection<Land>> completableFuture;

        private BulkChunkHandler(Set<SimpleChunkLocation> locations, List<Land> loaded, CompletableFuture<Collection<Land>> completableFuture) {
            this.locations = locations;
            this.loaded = loaded;
            this.completableFuture = completableFuture;
        }

        @Override
        public void accept(Land land) {
            this.loaded.add(land);
            this.locations.remove(land.getLocation());
            if (locations.isEmpty()) this.completableFuture.complete(this.loaded);
        }
    }
}
