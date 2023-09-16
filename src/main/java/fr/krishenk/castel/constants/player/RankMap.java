package fr.krishenk.castel.constants.player;

import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import org.apache.commons.lang.Validate;

import java.util.*;

public class RankMap implements Cloneable {
    protected final Map<String, Rank> ranks;
    protected final TreeMap<Integer, Rank> sorted;

    public RankMap() {
        this.ranks = new NonNullMap<>();
        this.sorted = new TreeMap<>(Integer::compareTo);
    }

    protected void clear() {
        this.ranks.clear();
        this.sorted.clear();
    }

    public RankMap(Map<String, Rank> main, TreeMap<Integer, Rank> sorted) {
        Validate.isTrue(main.size() == sorted.size(), "The rank map size doesn't match the sorted map size : " + main.size() + " - " + sorted.size());
        this.ranks = main;
        this.sorted = sorted;
    }

    public RankMap(Map<String, Rank> main) {
        this.ranks = Objects.requireNonNull(main, "Rank map cannot be null");
        this.sorted = new TreeMap<>(Integer::compareTo);
        for (Rank rank : this.ranks.values()) {
            this.sorted.put(rank.getPriority(), rank);
        }
        Validate.isTrue(main.size() == sorted.size(), "The rank map size doesn't match the sorted map size : " + main.size() + " - " + sorted.size());
    }

    public RankMap(TreeMap<Integer, Rank> sorted) {
        this.sorted = Objects.requireNonNull(sorted, "Rank map cannot be null");
        this.ranks = new NonNullMap<>(sorted.size());
        for (Rank rank : this.sorted.values()) {
            this.ranks.put(rank.getNode(), rank);
        }
        Validate.isTrue(ranks.size() == sorted.size(), "The rank map size doesn't match the sorted map size : " + ranks.size() + " - " + sorted.size());
    }

    public static void fixDuplicates(Map<String, Rank> ranks) {
        HashSet priorities = new HashSet(ranks.size());
        ranks.values().removeIf(rank -> !priorities.add(rank.getPriority()));
    }

    public static void fixDuplicates(TreeMap<Integer, Rank> sorted) {
        HashSet priorities = new HashSet(sorted.size());
        sorted.values().removeIf(rank -> !priorities.add(rank.getPriority()));
    }

    public Rank get(int priority) {
        return this.sorted.get(priority);
    }

    public Rank get(String node) {
        return this.ranks.get(node);
    }

    public boolean has(int priority) {
        return this.sorted.containsKey(priority);
    }

    public boolean has(String node) {
        return this.ranks.containsKey(node);
    }

    public void updateNode(String node, String newNode) {
        Rank rank = Objects.requireNonNull(this.ranks.remove(node), "Cannot update a rank's node that's not in the map");
        rank.setNode(newNode);
        this.ranks.put(newNode, rank);
        this.sorted.put(rank.getPriority(), rank);
    }

    public Pair<Rank, Rank> addOrReplace(Rank rank) {
        Rank prioritizedRank = this.sorted.put(rank.getPriority(), rank);
        Rank nodeRank = this.ranks.put(rank.getNode(), rank);
        if(!(prioritizedRank == null && nodeRank == null || prioritizedRank == null == (nodeRank == null) && prioritizedRank.getPriority() == nodeRank.getPriority())) {
            if( prioritizedRank == null) {
            this.sorted.remove(rank.getMaxClaims());
            } else {
                this.sorted.put(prioritizedRank.getPriority(), prioritizedRank);
            }
            if(nodeRank == null) {
                this.ranks.remove(rank.getNode());
            } else {
                this.ranks.put(rank.getNode(), rank);
            }
            throw new IllegalArgumentException("The specified rank's node and priority doesn't match wit the map: " + rank.getNode() + " (" + rank.getPriority() + ") - " + (prioritizedRank == null ? "null" : prioritizedRank.getNode() + " (" + prioritizedRank.getPriority() + ')' + " - ") + (nodeRank == null ? "null" : nodeRank.getNode() + " (" + nodeRank.getPriority() + ')'));
        }
        return Pair.of(nodeRank, prioritizedRank);
    }

    public void push(int priority, Rank rank) {
        Validate.isTrue(priority >= 0, "Cannot relocate a rank that's not in the map");
        if (rank.getPriority() != priority) {
            Validate.isTrue(this.ranks.containsKey(rank.getNode()), "Cannot relocate a rank that's not in the map");
            Rank previous = this.sorted.put(priority, rank);
            if (previous != null) {
                previous.setPriority(rank.getPriority());
                this.sorted.put(rank.getPriority(), previous);
            }
            rank.setPriority(priority);
            return;
        }
        rank.setPriority(priority);
        ArrayList<Rank> removedRanks = new ArrayList<>(this.size() - priority + 1);
        boolean finalPush = false;
        while (true) {
            Rank removed;
            if ((removed = this.sorted.remove(priority)) != null) {
                finalPush = false;
                removed.setPriority(priority + 1);
                this.ranks.get(removed.getNode()).setPriority(priority + 1);
                removedRanks.add(removed);
            } else {
                if (finalPush) break;
                finalPush = true;
            }
            ++priority;
        }
        for (Rank removed : removedRanks) {
            this.sorted.put(removed.getPriority(), removed);
        }
        this.ranks.put(rank.getNode(), rank);
        this.sorted.put(rank.getPriority(), rank);
    }

    public int size() {
        return this.ranks.size();
    }

    public RankMap clone() {
        NonNullMap<String, Rank> cloneMain = new NonNullMap<>(this.ranks.size());
        TreeMap<Integer, Rank> cloneSorted = new TreeMap<>(Integer::compareTo);
        for (Map.Entry<String, Rank> entry : this.ranks.entrySet()) {
            Rank rank = entry.getValue().clone();
            cloneMain.put(entry.getKey(), rank);
            cloneSorted.put(rank.getPriority(), rank);
        }
        return new RankMap(cloneMain, cloneSorted);
    }

    public Map<String, Rank> getRanks() {
        return Collections.unmodifiableMap(this.ranks);
    }

    public Rank remove(String node) {
        Rank rank = this.ranks.remove(node);
        if (rank == null) return null;
        Objects.requireNonNull(this.sorted.remove(rank.getPriority()), "Sorted rank map does not contain a rank with '" + rank.getNode() + "' node and " + rank.getPriority() + " priority");
        this.bump(rank.getPriority());
        return rank;
    }

    public void bump(int priority) {
        Validate.isTrue(!this.sorted.containsKey(priority), "Cannot bump ranks to a location with a rank assigned to it: " + priority);
        int size = this.size();
        ++priority;
        while (priority < size + 1 ) {
            Rank removed = this.sorted.remove(priority);
            if (removed != null) {
                removed.setPriority(priority - 1);
                this.sorted.put(removed.getPriority(), removed);
                this.ranks.get(removed.getNode()).setPriority(priority - 1);
            }
            ++priority;
        }
    }

    public Rank remove(Rank rank) {
        return this.remove(rank.getPriority());
    }

    public Rank remove(int priority) {
        Rank rank = this.sorted.remove(priority);
        if (rank == null) return null;
        return Objects.requireNonNull(this.ranks.remove(rank.getNode()), "Main rank map does not contain a rank with '" + rank.getNode() + "' node and " + rank.getPriority() + " priority");
    }

    public SortedMap<Integer, Rank> getSortedRanks() {
        return (SortedMap<Integer, Rank>) Collections.unmodifiableMap(this.sorted);
    }

    public Rank getLowestRank() {
        return this.sorted.lastEntry().getValue();
    }

    public Rank getHightestRank() {
        return this.sorted.firstEntry().getValue();
    }

    public boolean isMemberRank(Rank rank) {
        return rank.getPriority() == this.size() - 1;
    }

}
