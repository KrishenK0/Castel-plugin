package fr.krishenk.castel.locale.compiler;

import java.util.Objects;
import java.util.function.Supplier;

public class PlaceholderTranslationContext {
    private final Object raw;
    private final MessageCompilerSettings settings;
    public static final MessageCompilerSettings PLACEHOLDER_SETTINGS = new MessageCompilerSettings(false, true, true, true, true, null);

    public PlaceholderTranslationContext(Object raw, MessageCompilerSettings settings) {
        this.raw = Objects.requireNonNull(raw, "Raw value cannot be null");
        this.settings = settings;
    }

    public static PlaceholderTranslationContext withDefaultContext(Object raw) {
        return new PlaceholderTranslationContext(raw, PLACEHOLDER_SETTINGS);
    }

    public MessageCompilerSettings getSettings() {
        return this.settings;
    }

    public Object getValue() {
        if (this.raw instanceof Supplier) {
            return ((Supplier)this.raw).get();
        }
        return this.raw;
    }
}
