package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.jetbrains.annotations.Nullable;

public abstract class LocationChangeEvent extends CastelEvent implements Cancellable, PlayerOperator {
    private Location newLocation;

    public LocationChangeEvent(Location newLocation) {
        this.newLocation = newLocation;
    }

    public Location getNewLocation() {
        return newLocation;
    }

    public void setNewLocation(Location newLocation) {
        this.newLocation = newLocation;
    }

    @Nullable
    public abstract Location getOldLocation();
}
