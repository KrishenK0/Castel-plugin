package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class GuildSetHomeEvent extends LocationChangeEvent implements Cancellable, GuildOperator {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final CastelPlayer player;
    private boolean cancelled;

    public GuildSetHomeEvent(Location newLocation, Guild guild, CastelPlayer player) {
        super(newLocation);
        this.guild = guild;
        this.player = player;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
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
    public Guild getGuild() {
        return guild;
    }

    @Override
    public CastelPlayer getPlayer() {
        return player;
    }

    @Override
    public @Nullable Location getOldLocation() {
        return this.guild.getHome();
    }

    public static HandlerList getHandlerList() { return handlers; }
}
