package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GroupRelationshipRequestEvent extends CastelEvent implements PlayerOperator, Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Group from;
    private final Group to;
    private final GuildRelation relationship;
    private final CastelPlayer player;

    public GroupRelationshipRequestEvent(Group from, Group to, GuildRelation relationship, CastelPlayer player) {
        this.from = from;
        this.to = to;
        this.relationship = relationship;
        this.player = player;
    }

    public Group getFrom() {
        return from;
    }

    public Group getTo() {
        return to;
    }

    public GuildRelation getRelationship() {
        return relationship;
    }

    @Override
    public CastelPlayer getPlayer() {
        return player;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean b) {
        this.cancelled = b;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() { return handlers; }
}
