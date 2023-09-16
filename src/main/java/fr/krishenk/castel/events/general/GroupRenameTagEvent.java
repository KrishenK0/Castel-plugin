package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GroupOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GroupRenameTagEvent extends CastelEvent implements GroupOperator, PlayerOperator, Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private String newTag;
    private final CastelPlayer player;
    private final Group group;

    public GroupRenameTagEvent(String newTag, CastelPlayer player, Group group) {
        this.newTag = newTag;
        this.player = player;
        this.group = group;
    }

    public String getNewTag() {
        return newTag;
    }

    public String getOldTag() {
        return this.group.getTag();
    }

    public void setNewTag(String newTag) {
        this.newTag = newTag;
    }

    @Override
    public Group getGroup() {
        return group;
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
