package fr.krishenk.castel.utils.string.tree;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

public class TreeStyle {
    @NotNull
    private final Map<TreeTokenType, String> characters;
    @NotNull
    private final Map<TreeColorScheme, String> colors;
    private int maxColumns;
    private int maxRows;
    private int columizeFromLevel;
    @NotNull
    private String columnIdentation;
    private int indentation;
    private int lines;
    private boolean indentFirstLevel;
    private boolean flatten;
    private int maxNestLevel;
    private int maxEntries;
    @NotNull
    private String ellipsis;
    @Nullable
    private BiFunction<Integer, String, String> entryModifier;
    @Nullable
    private Function<String, Integer> columnSpaceModifier;


    public TreeStyle() {
        this(null, null);
    }

    public TreeStyle(@NotNull Map<TreeTokenType, String> characters, @NotNull Map<TreeColorScheme, String> colors) {
        this.characters = characters;
        this.colors = colors;
        this.maxColumns = 1;
        this.maxRows = Integer.MAX_VALUE;
        this.columizeFromLevel = Integer.MAX_VALUE;
        this.columnIdentation = " ";
        this.indentation = 2;
        this.lines = 1;
        this.indentFirstLevel = true;
        this.flatten = true;
        this.maxNestLevel = Integer.MAX_VALUE;
        this.maxEntries = Integer.MAX_VALUE;
        this.ellipsis = "...";
    }

    @NotNull
    public final Map<TreeTokenType, String> getCharacters() {
        return this.characters;
    }

    @NotNull
    public final Map<TreeColorScheme, String> getColors() {
        return this.colors;
    }

    public final int getMaxColumns() {
        return this.maxColumns;
    }

    public final void setMaxColumns(int var1) {
        this.maxColumns = var1;
    }

    public final int getMaxRows() {
        return this.maxRows;
    }

    public final void setMaxRows(int var1) {
        this.maxRows = var1;
    }

    public final int getColumizeFromLevel() {
        return this.columizeFromLevel;
    }

    public final void setColumizeFromLevel(int var1) {
        this.columizeFromLevel = var1;
    }

    @NotNull
    public final String getColumnIdentation() {
        return this.columnIdentation;
    }

    public final void setColumnIdentation(@NotNull String indent) {
        Objects.requireNonNull(indent);
        this.columnIdentation = indent;
    }

    public final int calculatedMaxRows() {
        return this.maxColumns == 1 ? Integer.MAX_VALUE : this.maxRows;
    }

    public final int getIndentation() {
        return this.indentation;
    }

    public final void setIndentation(int var1) {
        this.indentation = var1;
    }

    public final int getLines() {
        return this.lines;
    }

    public final void setLines(int var1) {
        this.lines = var1;
    }

    public final boolean getIndentFirstLevel() {
        return this.indentFirstLevel;
    }

    public final void setIndentFirstLevel(boolean var1) {
        this.indentFirstLevel = var1;
    }

    public final boolean getFlatten() {
        return this.flatten;
    }

    public final void setFlatten(boolean var1) {
        this.flatten = var1;
    }

    public final int getMaxNestLevel() {
        return this.maxNestLevel;
    }

    public final void setMaxNestLevel(int var1) {
        this.maxNestLevel = var1;
    }

    public final int getMaxEntries() {
        return this.maxEntries;
    }

    public final void setMaxEntries(int var1) {
        this.maxEntries = var1;
    }

    @NotNull
    public final String getEllipsis() {
        return this.ellipsis;
    }

    public final void setEllipsis(@NotNull String ellipsis) {
        Objects.requireNonNull(ellipsis);
        this.ellipsis = ellipsis;
    }

    @Nullable
    public final BiFunction<Integer, String, String> getEntryModifier() {
        return this.entryModifier;
    }

    public final void setEntryModifier(@Nullable BiFunction<Integer, String, String> var1) {
        this.entryModifier = var1;
    }

    @Nullable
    public final Function<String, Integer> getColumnSpaceModifier() {
        return this.columnSpaceModifier;
    }

    public final void setColumnSpaceModifier(@Nullable Function<String, Integer> var1) {
        this.columnSpaceModifier = var1;
    }

    public final void validate() {
        boolean var1;
        String var2;
        if (this.maxColumns < 0) {
            var1 = false;
            var2 = "Max columns must be positive: " + this.maxColumns;
            throw new IllegalArgumentException(var2);
        } else if (this.maxRows < 0) {
            var1 = false;
            var2 = "Max rows must be positive: " + this.maxRows;
            throw new IllegalArgumentException(var2);
        } else if (this.indentation < 0) {
            var1 = false;
            var2 = "Indentation must be positive: " + this.indentation;
            throw new IllegalArgumentException(var2);
        } else if (this.lines < 0) {
            var1 = false;
            var2 = "Lines must be positive: " + this.lines;
            throw new IllegalArgumentException(var2);
        } else if (this.maxNestLevel < 0) {
            var1 = false;
            var2 = "Max nest level must be positive: " + this.maxNestLevel;
            throw new IllegalArgumentException(var2);
        } else if (this.maxEntries < 0) {
            var1 = false;
            var2 = "Max entries must be positive: " + this.maxEntries;
            throw new IllegalArgumentException(var2);
        }
    }

}
