package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GuildLeaderChangeEvent extends CastelEvent implements Cancellable, PlayerOperator, GuildOperator {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private boolean cancelled;
    private CastelPlayer newLeader;
    private final Reason reason;

    public GuildLeaderChangeEvent(Guild guild, CastelPlayer newLeader, Reason reason) {
        this.guild = guild;
        this.newLeader = newLeader;
        this.reason = reason;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Reason getReason() {
        return reason;
    }

    public CastelPlayer getOldLeader() { return this.guild.getLeader(); }

    public CastelPlayer getNewLeader() {
        return newLeader;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public CastelPlayer getPlayer() {
        return this.newLeader;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() { return handlers; }

    public enum Reason {
        ADMIN,
        LEADER_DECISION,
        ELECTIONS,
        INACTIVITY,
        OTHER;
    }
}
