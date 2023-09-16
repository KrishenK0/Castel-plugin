package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GuildDisbandEvent extends CastelEvent implements Cancellable, GroupDisband, GuildOperator {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final GroupDisband.Reason reason;
    private final Guild guild;

    public GuildDisbandEvent(Guild guild, GroupDisband.Reason reason) {
        this.guild = guild;
        this.reason = Objects.requireNonNull(reason, "Disband reason cannot be null");
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @Override
    public Reason getReason() {
        return reason;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
