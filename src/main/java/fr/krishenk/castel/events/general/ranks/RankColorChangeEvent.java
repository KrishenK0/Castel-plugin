package fr.krishenk.castel.events.general.ranks;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RankColorChangeEvent extends RankEvent implements Cancellable {
    public static final HandlerList HANDLERS = new HandlerList();
    private String newColor;

    public RankColorChangeEvent(Rank rank, Group group, CastelPlayer player, String newColor) {
        super(rank, group, player);
        this.newColor = newColor;
    }

    public String getNewColor() {
        return newColor;
    }

    public void setNewColor(String newColor) {
        this.newColor = newColor;
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
