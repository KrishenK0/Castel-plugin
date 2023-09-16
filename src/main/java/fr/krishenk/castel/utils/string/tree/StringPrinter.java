package fr.krishenk.castel.utils.string.tree;


import org.jetbrains.annotations.NotNull;

public class StringPrinter implements EntryPrinter {
    @NotNull
    private final String name;

    public StringPrinter(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public String toString() {
        return this.name;
    }

    public void print(@NotNull PrinterContext context) {
        context.getTree().writeEntry(context.getNestLevel(), this.name);
    }
}
