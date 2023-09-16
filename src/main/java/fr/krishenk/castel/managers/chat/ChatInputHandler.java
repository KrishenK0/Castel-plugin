package fr.krishenk.castel.managers.chat;

import fr.krishenk.castel.managers.abstraction.MoveSensitiveAction;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.function.Function;

public class ChatInputHandler<T> extends MoveSensitiveAction {
    protected Function<AsyncPlayerChatEvent, Boolean> onInput;

    protected Runnable onCancel;
    protected boolean sync;
    protected final T session;

    public ChatInputHandler(T session) {
        this.session = session;
    }

    public ChatInputHandler() {
        this(null);
    }

    public T getSession() {
        return session;
    }

    public void sync() {
        this.sync = true;
    }

    public void onInput(Function<AsyncPlayerChatEvent, Boolean> handler) {
        this.onInput = handler;
    }

    public void onCancel(Runnable onCancel) {
        this.onCancel = onCancel;
    }
}
