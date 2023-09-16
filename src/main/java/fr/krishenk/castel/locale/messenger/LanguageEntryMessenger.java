package fr.krishenk.castel.locale.messenger;

import fr.krishenk.castel.locale.LanguageEntry;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.provider.MessageProvider;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class LanguageEntryMessenger implements Messenger {
    @NotNull
    private final LanguageEntry entry;

    public LanguageEntryMessenger(@NotNull LanguageEntry entry) {
        
        this.entry = entry;
    }

    @NotNull
    public final LanguageEntry getEntry() {
        return this.entry;
    }

    public LanguageEntryMessenger(String ... path) {
        this(new LanguageEntry(path));
    }

    @Override
    @Nullable
    public MessageProvider getProvider(@NotNull SupportedLanguage locale) {
        return locale.getMessage(this.entry, false);
    }

    @NotNull
    public String toString() {
        return "LanguageEntryMessenger{" + Arrays.toString(this.entry.getPath()) + '}';
    }
}

