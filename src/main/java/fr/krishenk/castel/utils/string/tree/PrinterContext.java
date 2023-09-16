package fr.krishenk.castel.utils.string.tree;

import org.jetbrains.annotations.NotNull;

public class PrinterContext {
    private final int nestLevel;
    @NotNull
    private final StringTree tree;
    private final int column;
    private int currentColumnLongestEntry;

    public PrinterContext(int nestLevel, @NotNull StringTree tree, int column, int currentColumnLongestEntry) {
        this.nestLevel = nestLevel;
        this.tree = tree;
        this.column = column;
        this.currentColumnLongestEntry = currentColumnLongestEntry;
    }

    public final int getNestLevel() {
        return this.nestLevel;
    }

    @NotNull
    public final StringTree getTree() {
        return this.tree;
    }

    public final int getColumn() {
        return this.column;
    }

    public final int getCurrentColumnLongestEntry() {
        return this.currentColumnLongestEntry;
    }

    public final void setCurrentColumnLongestEntry(int var1) {
        this.currentColumnLongestEntry = var1;
    }
}
