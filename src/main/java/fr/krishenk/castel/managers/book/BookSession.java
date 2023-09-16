package fr.krishenk.castel.managers.book;

import org.bukkit.event.player.PlayerEditBookEvent;

import java.util.function.Consumer;

public class BookSession {
    private final int slot;
    private final Consumer<PlayerEditBookEvent> onSign;
    private final Object data;

    public BookSession(int slot, Consumer<PlayerEditBookEvent> onSign, Object data) {
        this.slot = slot;
        this.onSign = onSign;
        this.data = data;
    }

    public int getSlot() {
        return slot;
    }

    public Consumer<PlayerEditBookEvent> getOnSign() {
        return onSign;
    }

    public Object getData() {
        return data;
    }
}
