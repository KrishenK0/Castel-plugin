package fr.krishenk.castel.events.members;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GuildJoinEvent extends CastelEvent implements Cancellable, PlayerOperator, GuildOperator {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final CastelPlayer player;
    private boolean cancelled;

    public GuildJoinEvent(Guild guild, CastelPlayer player) {
        this.guild = Objects.requireNonNull(guild);
        this.player = Objects.requireNonNull(player);
    }

    @Override
    public Guild getGuild() {
        return this.guild;
    }

    @Override
    public CastelPlayer getPlayer() {
        return this.player;
    }

    @Override
    public boolean isCancelled() {
        return this.cancelled;
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
