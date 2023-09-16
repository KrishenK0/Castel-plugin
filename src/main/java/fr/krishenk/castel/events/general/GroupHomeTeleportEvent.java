package fr.krishenk.castel.events.general;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class GroupHomeTeleportEvent extends CastelEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Group group;
    private final Player player;
    private Location location;
    private final LocationType locationType;
    private boolean cancelled;

    public GroupHomeTeleportEvent(Group group, Player player, Location location, LocationType locationType) {
        this.group = group;
        this.player = player;
        this.location = location;
        this.locationType = locationType;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    public Player getPlayer() {
        return player;
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

    public void setLocation(Location location) {
        this.location = location;
    }

    public Location getLocation() {
        return location;
    }

    public LocationType getLocationType() {
        return locationType;
    }

    public Group getGroup() {
        return group;
    }

    public static enum LocationType {
        HOME,
        NEXUS;
    }
}
