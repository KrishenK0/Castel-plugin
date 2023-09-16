package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GroupOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GroupHiddenStateChangeEvent extends CastelEvent implements Cancellable, GroupOperator, PlayerOperator {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private boolean hidden;
    private final CastelPlayer player;
    private final Group group;

    public GroupHiddenStateChangeEvent(Group group, boolean hidden, CastelPlayer player) {
        this.group = group;
        this.hidden = hidden;
        this.player = player;
    }

    public boolean getNewHiddenState() {
        return this.hidden;
    }

    public void setHiddenState(boolean hidden) {
        this.hidden = hidden;
    }

    public boolean wasHidden() {
        return this.group.isHidden();
    }

    @Override
    public Group getGroup() {
        return this.group;
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
