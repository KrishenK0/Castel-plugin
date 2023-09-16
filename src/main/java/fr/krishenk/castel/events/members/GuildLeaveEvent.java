package fr.krishenk.castel.events.members;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

public class GuildLeaveEvent extends CastelEvent implements Cancellable, PlayerOperator, GuildOperator {
    private static final HandlerList handlers = new HandlerList();
    private final CastelPlayer player;
    private final LeaveReason reason;
    private boolean cancelled;

    public GuildLeaveEvent(CastelPlayer player, LeaveReason reason) {
        this.player = player;
        this.reason = reason;
    }

    public HandlerList getHandlers() {
        return handlers;
    }

    public LeaveReason getReason() {
        return this.reason;
    }

    public boolean isCancelled() {
        return this.cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public CastelPlayer getPlayer() {
        return this.player;
    }

    @Override
    public Guild getGuild() {
        return this.player.getGuild();
    }

    public static HandlerList getHandlerList() { return handlers; }
}
