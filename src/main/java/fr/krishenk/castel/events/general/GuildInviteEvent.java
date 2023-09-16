package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GuildInviteEvent extends CastelEvent implements PlayerOperator, GuildOperator, Cancellable {
    public static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final CastelPlayer inviter;
    private final CastelPlayer invited;
    private long acceptTime;
    private boolean cancelled;

    public GuildInviteEvent(Guild guild, CastelPlayer inviter, CastelPlayer invited, long acceptTime) {
        this.guild = Objects.requireNonNull(guild);
        this.inviter = Objects.requireNonNull(inviter);
        this.invited = Objects.requireNonNull(invited);
        this.acceptTime = acceptTime;
    }

    public CastelPlayer getInviter() {
        return inviter;
    }

    public long getAcceptTime() {
        return acceptTime;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Override
    public CastelPlayer getPlayer() {
        return this.invited;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
