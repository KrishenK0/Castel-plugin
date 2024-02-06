package fr.krishenk.castel.events.general.ranks;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RankNameChangeEvent extends RankEvent implements Cancellable {
    public static final HandlerList HANDLERS = new HandlerList();
    private String newName;

    public RankNameChangeEvent(Rank rank, Group group, CastelPlayer player, String newName) {
        super(rank, group, player);
        this.newName = newName;
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
