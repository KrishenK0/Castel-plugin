package fr.krishenk.castel.events.items;

import fr.krishenk.castel.constants.player.CastelPlayer;
import org.bukkit.event.Event;

import java.util.function.Consumer;

public class CastelItemRemoveContext {
    private Event cause;
    private CastelPlayer player;
    private boolean dropsItem = true;
    private Consumer<CastelItemBreakEvent<?>> modifier;
    public static final CastelItemRemoveContext DEFAULT = new CastelItemRemoveContext();

    public Event getCause() {
        return cause;
    }

    public void setCause(Event cause) {
        this.cause = cause;
    }

    public CastelPlayer getPlayer() {
        return player;
    }

    public void setPlayer(CastelPlayer player) {
        this.player = player;
    }

    public boolean getDropsItem() {
        return dropsItem;
    }

    public void setDropsItem(boolean dropsItem) {
        this.dropsItem = dropsItem;
    }

    public Consumer<CastelItemBreakEvent<?>> getModifier() {
        return modifier;
    }

    public void setModifier(Consumer<CastelItemBreakEvent<?>> modifier) {
        this.modifier = modifier;
    }
}
