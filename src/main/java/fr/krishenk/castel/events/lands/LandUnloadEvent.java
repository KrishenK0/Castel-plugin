package fr.krishenk.castel.events.lands;

import fr.krishenk.castel.abstraction.LandOperator;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LandUnloadEvent extends CastelEvent implements LandOperator {
    private static final HandlerList handlers = new HandlerList();
    private final Land land;

    public LandUnloadEvent(Land land) {
        this.land = land;
    }

    private static final HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @Nullable
    @Override
    public Land getLand() {
        return land;
    }
}
