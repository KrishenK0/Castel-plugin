package fr.krishenk.castel.utils.string.tree;

import org.jetbrains.annotations.NotNull;

public interface EntryPrinter {
    void print(@NotNull PrinterContext var1);

    @NotNull
    String toString();

    default boolean maxEntriesExceeded(@NotNull PrinterContext context) {
        return context.getTree().getTotalEntries$core() >= context.getTree().getStyle().getMaxEntries();
    }
}
