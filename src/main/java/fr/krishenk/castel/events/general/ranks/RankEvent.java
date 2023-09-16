package fr.krishenk.castel.events.general.ranks;

import fr.krishenk.castel.abstraction.GroupOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class RankEvent extends Event implements Cancellable, PlayerOperator, GroupOperator {
    private final Group group;
    private final CastelPlayer player;
    private boolean cancelled;
    private Rank rank;

    public RankEvent(Rank rank, Group group, CastelPlayer player, boolean async) {
        super(async);
        this.rank = rank;
        this.group = group;
        this.player = player;
    }

    public RankEvent(Rank rank, Group group, CastelPlayer player) {
        this(rank, group, player, true);
    }

    public final Rank getRank() {
        return this.rank;
    }

    protected void setRank(Rank rank) {
        this.rank = rank;
    }

    @Override
    public CastelPlayer getPlayer() {
        return this.player;
    }

    @Override
    public Group getGroup() {
        return group;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
