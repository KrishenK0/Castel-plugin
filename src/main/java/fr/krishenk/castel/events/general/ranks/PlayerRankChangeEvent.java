package fr.krishenk.castel.events.general.ranks;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import org.bukkit.event.HandlerList;

import java.util.Objects;

public class PlayerRankChangeEvent extends RankEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Rank oldRank;
    private final CastelPlayer byPlayer;

    public PlayerRankChangeEvent(Rank oldRank, Rank newRank, Group group, CastelPlayer player, CastelPlayer byPlayer) {
        super(newRank, group, player, false);
        this.oldRank = oldRank;
        this.byPlayer = byPlayer;
    }

    public final Rank getOldRank() {
        return this.oldRank;
    }

    public final CastelPlayer getByPlayer() {
        return this.byPlayer;
    }

    public void setRank(String node) {
        Objects.requireNonNull(node);
        Group group = this.getGroup();
        Rank rank = group.getRanks().get(node);
        if (rank == null)
            throw new IllegalArgumentException("The specified rank with node '" + node + "' is not in group '" + group.getName() + "' (" + group.getClass().getName() + ')');
        super.setRank(rank);
    }

    public CastelPlayer getPlayer() {
        CastelPlayer cp = super.getPlayer();
        Objects.nonNull(cp);
        return cp;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
