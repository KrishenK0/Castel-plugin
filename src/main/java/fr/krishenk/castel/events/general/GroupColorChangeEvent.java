package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GroupOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class GroupColorChangeEvent extends CastelEvent implements Cancellable, GroupOperator, PlayerOperator {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private Color newColor;
    private final CastelPlayer player;
    private final Group group;

    public GroupColorChangeEvent(Color newColor, CastelPlayer player, Group group) {
        this.newColor = newColor;
        this.player = player;
        this.group = group;
    }

    public Color getNewColor() {
        return newColor;
    }

    public Color getOldColor() {
        return this.group.getColor();
    }

    public void setNewColor(Color newColor) {
        this.newColor = newColor;
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
