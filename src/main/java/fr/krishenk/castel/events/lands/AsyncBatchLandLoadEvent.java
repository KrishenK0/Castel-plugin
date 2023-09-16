package fr.krishenk.castel.events.lands;

import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class AsyncBatchLandLoadEvent extends CastelEvent {
    private static final HandlerList handlers = new HandlerList();
    private final Collection<Land> lands;

    public AsyncBatchLandLoadEvent(Collection<Land> lands) {
        super(true);
        this.lands = lands;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public Collection<Land> getLands() {
        return lands;
    }
}
