package fr.krishenk.castel.utils.string.tree;

import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class StringTree {
    @NotNull
    private EntryPrinter root;
    @NotNull
    private final TreeStyle style;
    @NotNull
    private final String entryColor;
    @NotNull
    private final String separator;
    @NotNull
    private final String separatorColor;
    @NotNull
    private final ArrayList<StringBuilder> lines;
    @NotNull
    private StringBuilder currentBuilder;
    private int linePosition;
    private int totalEntries;
    @NotNull
    private static final Map<TreeTokenType, String> ASCII_CHARACTER_SET;
    @NotNull
    private static final Map<TreeTokenType, String> UTF_CHARACTER_SET;

    public StringTree(@NotNull EntryPrinter root, @NotNull TreeStyle style) {
        this.root = root;
        this.style = style;
        this.entryColor = StringTree.getOrEmpty(this.style.getColors(), TreeColorScheme.ENTRIES);
        this.separator = this.style.getCharacters().getOrDefault(TreeTokenType.INDICATOR_PATH_SEPARATOR, "/");
        this.separatorColor = StringTree.getOrEmpty(this.style.getColors(), TreeColorScheme.PATH_SEPARATORS);
        this.lines = new ArrayList(10);
        this.currentBuilder = new StringBuilder(100);
        if (this.style.getFlatten()) {
            this.root = this.flatten(this.root);
        }

        this.style.validate();
        this.lines.add(this.currentBuilder);
    }

    @NotNull
    public final TreeStyle getStyle() {
        return this.style;
    }

    public static final <K> String getOrEmpty(@NotNull Map<K, String> map, K key) {
        String value = map.get(key);
        return value == null ? "" : value;
    }

    public final void writeIndicator(boolean writeIndicator, @NotNull TreeTokenType type) {
        Objects.requireNonNull(type);
        String color = StringTree.getOrEmpty(this.style.getColors(), TreeColorScheme.INDICATORS);
        String characters = StringTree.getOrEmpty(this.style.getCharacters(), type);
        this.currentBuilder.append(color).append(characters);
        if (writeIndicator && this.style.getLines() != 0) {
            TreeTokenType[] array = new TreeTokenType[]{TreeTokenType.INDICATOR_MIDDLE, TreeTokenType.INDICATOR_CORNERS_FIRST, TreeTokenType.INDICATOR_CORNERS_LAST};
            if (Arrays.asList(array).contains(type)) {
                String nest = StringTree.getOrEmpty(this.style.getCharacters(), TreeTokenType.INDICATOR_HORIZONTAL_LINE);
                this.currentBuilder.append(Strings.repeat(nest, this.style.getLines()));
            }
        }

    }

    public final void writeEntry(int nestLevel, @NotNull String name) {
        BiFunction<Integer, String, String> entryModifier = this.style.getEntryModifier();
        String modifier = entryModifier != null ? entryModifier.apply(nestLevel, name) : null;
        if (modifier == null) {
            modifier = name;
        }

        String newName = modifier;
        this.currentBuilder.append(this.entryColor).append(newName);
        this.totalEntries++;
    }

    private final EntryPrinter flatten(EntryPrinter entry) {
        if (!(entry instanceof ContainerPrinter)) {
            return entry;
        } else if (((ContainerPrinter)entry).getChildren().size() == 1) {
            EntryPrinter flat = this.flatten(((ContainerPrinter) entry).getChildren().get(0));
            String finalName = entry + this.separatorColor + this.separator + this.entryColor + flat;
            EntryPrinter printer;
            if (flat instanceof ContainerPrinter) {
                ((ContainerPrinter)flat).setName(finalName);
                printer = flat;
            } else {
                if (!(flat instanceof StringPrinter)) {
                    throw new IllegalStateException("Cannot flatten entry printer: " + flat);
                }

                printer = new StringPrinter(finalName);
            }

            return printer;
        } else {
            ContainerPrinter container = new ContainerPrinter(((ContainerPrinter)entry).getName());
            for (EntryPrinter child : ((ContainerPrinter) entry).getChildren()) {
                container.getChildren().add(this.flatten(child));
            }

            return container;
        }
    }

    public final void newLine() {
        int var1 = this.linePosition++;
        if (this.linePosition == this.lines.size()) {
            this.currentBuilder = new StringBuilder(this.currentBuilder.length());
            this.lines.add(this.currentBuilder);
        } else {
            this.currentBuilder = this.lines.get(this.linePosition);
        }

    }

    public final void revertLinePosition(int newLinePos) {
        this.linePosition = newLinePos;
        this.currentBuilder = this.lines.get(this.linePosition);
    }

    @NotNull
    public final ArrayList<StringBuilder> getLines() {
        return this.lines;
    }

    @NotNull
    public final StringBuilder getCurrentBuilder() {
        return this.currentBuilder;
    }

    public final void setCurrentBuilder(@NotNull StringBuilder builder) {
        this.currentBuilder = builder;
    }

    public final int getLinePosition() {
        return this.linePosition;
    }

    public final void setLinePosition(int var1) {
        this.linePosition = var1;
    }

    public final int getTotalEntries$core() {
        return this.totalEntries;
    }

    public final void setTotalEntries$core(int var1) {
        this.totalEntries = var1;
    }

    @NotNull
    public final StringTree print() {
        this.root.print(new PrinterContext(0, this, 1, 0));
        return this;
    }

    @NotNull
    public String toString() {
        return lines.stream().collect(Collectors.joining("\n"));
    }

    @NotNull
    public static final Map<TreeTokenType, String> getASCII_CHARACTER_SET() {
        return StringTree.ASCII_CHARACTER_SET;
    }

    @NotNull
    public static final Map<TreeTokenType, String> getUTF_CHARACTER_SET() {
        return StringTree.UTF_CHARACTER_SET;
    }

    static {
        EnumMap<TreeTokenType, String> map = new EnumMap<>(TreeTokenType.class);
        map.put(TreeTokenType.INDICATOR_VERTICAL_LINE, "|");
        map.put(TreeTokenType.INDICATOR_HORIZONTAL_LINE, "-");
        map.put(TreeTokenType.INDICATOR_CORNERS_FIRST, "+");
        map.put(TreeTokenType.INDICATOR_CORNERS_LAST, "+");
        map.put(TreeTokenType.INDICATOR_MIDDLE, "|");
        ASCII_CHARACTER_SET = Collections.unmodifiableMap(map);
        map = new EnumMap<>(TreeTokenType.class);
        map.put(TreeTokenType.INDICATOR_VERTICAL_LINE, "│");
        map.put(TreeTokenType.INDICATOR_HORIZONTAL_LINE, "─");
        map.put(TreeTokenType.INDICATOR_CORNERS_FIRST, "├");
        map.put(TreeTokenType.INDICATOR_CORNERS_LAST, "├");
        map.put(TreeTokenType.INDICATOR_MIDDLE, "├");
        UTF_CHARACTER_SET = Collections.unmodifiableMap(map);
    }
}
