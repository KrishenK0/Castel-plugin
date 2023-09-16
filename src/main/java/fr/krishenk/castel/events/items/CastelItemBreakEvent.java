package fr.krishenk.castel.events.items;

import fr.krishenk.castel.constants.land.abstraction.CastelItem;
import fr.krishenk.castel.constants.player.CastelPlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CastelItemBreakEvent<T extends CastelItem<?>> extends CastelItemEmbeddedEvent<T> {
    private static final HandlerList handlers = new HandlerList();
    private boolean dropItem;

    public CastelItemBreakEvent(CastelPlayer player, T castelItem, Event event, boolean dropItem) {
        super(player, castelItem, event);
        this.dropItem = dropItem;
    }

    public boolean dropItem() {
        return dropItem;
    }

    public void setDropItem(boolean dropItem) {
        this.dropItem = dropItem;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
}
