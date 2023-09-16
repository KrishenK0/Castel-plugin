package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class GroupRelationshipChangeEvent extends CastelEvent implements Cancellable, PlayerOperator {
    public static final HandlerList handlers = new HandlerList();
    private final Group first;
    private final Group second;
    private final GuildRelation newRelation;
    private final CastelPlayer player;
    private boolean cancelled;

    public GroupRelationshipChangeEvent(CastelPlayer player, Group first, Group second, GuildRelation relation) {
        this.player = player;
        this.first = Objects.requireNonNull(first);
        this.second = Objects.requireNonNull(second);
        this.newRelation = Objects.requireNonNull(relation);
        if (first.getClass() != second.getClass())
            throw new IllegalArgumentException("Relationship cannot change between two different group type: " + first.getClass().getName() + " - " + second.getClass().getName());
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public GuildRelation getOldRelation() {
        return this.first.getRelations().get(this.second.getId());
    }

    public Group getFirst() {
        return first;
    }

    public Group getSecond() {
        return second;
    }

    public GuildRelation getNewRelation() {
        return newRelation;
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
        return player;
    }
}
