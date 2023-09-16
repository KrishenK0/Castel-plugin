package fr.krishenk.castel.events.general.ranks;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RankDeleteEvent extends RankEvent implements Cancellable {
    public static final HandlerList handlers = new HandlerList();

    public RankDeleteEvent(Rank rank, Group group, CastelPlayer player) {
        super(rank, group, player);
    }

    public static HandlerList getHandlerList() { return handlers; }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
