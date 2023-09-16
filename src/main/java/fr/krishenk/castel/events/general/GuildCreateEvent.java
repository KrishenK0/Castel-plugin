package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GuildOperator;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GuildCreateEvent extends CastelEvent implements GuildOperator {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;

    public GuildCreateEvent(Guild guild) {
        this.guild = guild;
    }

    @Override
    public Guild getGuild() {
        return guild;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
