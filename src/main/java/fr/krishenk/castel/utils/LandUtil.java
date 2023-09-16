package fr.krishenk.castel.utils;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;

import java.util.*;

public class LandUtil {
    public static Collection<Set<SimpleChunkLocation>> getConnectedClusters(int radius, Set<SimpleChunkLocation> chunks) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Wrong radius for land clusters: " + radius);
        }
        ArrayList<Set<SimpleChunkLocation>> clusters = new ArrayList<Set<SimpleChunkLocation>>(radius + 1);
        HashSet<SimpleChunkLocation> checked = new HashSet<SimpleChunkLocation>(chunks.size());
        LinkedList<SimpleChunkLocation> queue = new LinkedList<SimpleChunkLocation>();
        for (SimpleChunkLocation chunk : chunks) {
            if (checked.contains(chunk)) continue;
            HashSet<SimpleChunkLocation> cluster = new HashSet<SimpleChunkLocation>(10);
            queue.add(chunk);
            while (!queue.isEmpty()) {
                for (SimpleChunkLocation around : ((SimpleChunkLocation)queue.remove()).getChunksAround(radius, true)) {
                    boolean isInCurrentCluster;
                    if (!chunks.contains(around)) continue;
                    boolean wasChecked = checked.add(around);
                    if (wasChecked != (isInCurrentCluster = cluster.add(around))) {
                        throw new AssertionError((Object)("Chunk cannot co-exist in two clusters: chunk=" + chunk + " | current=" + around + " (wasChecked=" + wasChecked + ", isInCurrentCluster=" + isInCurrentCluster + ')'));
                    }
                    if (wasChecked || isInCurrentCluster) continue;
                    queue.add(around);
                }
            }
            clusters.add(cluster);
        }
        return clusters;
    }
}
