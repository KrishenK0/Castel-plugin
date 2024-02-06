package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GuildPacifismStateChangeEvent extends CastelEvent implements Cancellable, GuildOperator, PlayerOperator {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final boolean pacifist;
    private final CastelPlayer castelPlayer;
    private final Guild guild;

    public GuildPacifismStateChangeEvent(boolean pacifist, CastelPlayer castelPlayer, Guild guild) {
        this.pacifist = pacifist;
        this.castelPlayer = castelPlayer;
        this.guild = guild;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public boolean isPacifist() {
        return pacifist;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public CastelPlayer getPlayer() {
        return this.castelPlayer;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }
}
