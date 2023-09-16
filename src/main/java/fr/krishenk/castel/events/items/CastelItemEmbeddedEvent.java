package fr.krishenk.castel.events.items;

import fr.krishenk.castel.abstraction.CastelItemOperator;
import fr.krishenk.castel.abstraction.PlayerOperator;
import fr.krishenk.castel.constants.land.abstraction.CastelItem;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.CastelEvent;
import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;

public abstract class CastelItemEmbeddedEvent<I extends CastelItem<?>> extends CastelEvent implements Cancellable, PlayerOperator, CastelItemOperator {
    private final CastelPlayer player;
    private final I castelItem;
    private final Event event;
    private boolean cancelled;

    public CastelItemEmbeddedEvent(CastelPlayer player, I castelItem, Event event) {
        this.player = player;
        this.castelItem = castelItem;
        this.event = event;
    }

    @Override
    public CastelPlayer getPlayer() {
        return player;
    }

    public I getCastelItem() {
        return castelItem;
    }

    public Event getBukkitEvent() {
        return event;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
        if (this.event instanceof Cancellable)  {
            ((Cancellable) this.event).setCancelled(cancelled);
        }
    }
}
