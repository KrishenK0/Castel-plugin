package fr.krishenk.castel.events.general.ranks;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.libs.xseries.XMaterial;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class RankMaxClaimsChangeEvent extends RankEvent implements Cancellable {
    public static final HandlerList HANDLERS = new HandlerList();
    private int newMaxClaims;

    public RankMaxClaimsChangeEvent(Rank rank, Group group, CastelPlayer player, int newMaxClaims) {
        super(rank, group, player);
        this.newMaxClaims = newMaxClaims;
    }

    public int getNewMaxClaims() {
        return newMaxClaims;
    }

    public void setNewMaxClaims(int newMaxClaims) {
        this.newMaxClaims = newMaxClaims;
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
