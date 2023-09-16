package fr.krishenk.castel.events.lands;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class UnclaimLandEvent extends ClaimingEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Reason reason;

    public UnclaimLandEvent(CastelPlayer player, Guild guild, Set<SimpleChunkLocation> lands, Reason reason) {
        super(player, guild, lands, false);
        this.reason = reason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }

    public Reason getReason() {
        return reason;
    }

    public enum Reason {
        UNCLAIMED,
        AUTO_UNCLAIMED,
        CLIPBOARD,
        INVASION,
        ADMIN,
        OVERCLAIM,
        CUSTOM;
    }
}
