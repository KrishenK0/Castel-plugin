package fr.krishenk.castel.locale.messenger;

import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.provider.MessageProvider;
import org.jetbrains.annotations.NotNull;

public class DefaultedMessenger implements Messenger {
    @NotNull
    private final Messenger first;
    @NotNull
    private final Messenger second;

    public DefaultedMessenger(@NotNull Messenger first, @NotNull Messenger second) {
        this.first = first;
        this.second = second;
    }

    @NotNull
    public final Messenger getFirst() {
        return this.first;
    }

    @NotNull
    public final Messenger getSecond() {
        return this.second;
    }

    @Override
    @NotNull
    public MessageProvider getProvider(@NotNull SupportedLanguage locale) {
        MessageProvider messageProvider = this.first.getProvider(locale);
        if (messageProvider == null) {
            messageProvider = this.second.getProvider(locale);
        }
        return messageProvider;
    }
}


