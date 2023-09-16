package fr.krishenk.castel.utils.string.tree;

import com.google.common.base.Strings;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ContainerPrinter implements EntryPrinter {
    @NotNull
    private String name;
    @NotNull
    private final List<EntryPrinter> children;
    @NotNull
    public static final String ROOT = "";

    public ContainerPrinter(@NotNull String name) {
        this.name = name;
        this.children = new ArrayList<>();
    }

    @NotNull
    public final String getName() {
        return this.name;
    }

    public final void setName(@NotNull String name) {
        this.name = name;
    }

    @NotNull
    public final List<EntryPrinter> getChildren() {
        return this.children;
    }

    public void print(@NotNull PrinterContext context) {
        Objects.requireNonNull(context);
        StringTree tree = context.getTree();
        int nestLevel = context.getNestLevel();
        if (!Objects.equals(this.name, "")) {
            tree.writeEntry(nestLevel, this.name);
            if (nestLevel > tree.getStyle().getMaxNestLevel()) {
                tree.getCurrentBuilder().append(tree.getStyle().getEllipsis());
                return;
            }

            tree.newLine();
        }

        boolean columize = tree.getStyle().getColumizeFromLevel() <= nestLevel;
        if (!columize) {
            this.printChildren(this.children, context);
        } else {
            List<EntryPrinter> leftChildren = this.children;
            int maxRows = tree.getStyle().calculatedMaxRows();
            int maxColumns = tree.getStyle().getMaxColumns();
            int rows = maxRows == Integer.MAX_VALUE ? (int)Math.ceil((double)this.children.size() / (double)maxColumns) : maxRows;
            int currentLinePos = tree.getLinePosition();
            int i = 0;
            if (i <= maxColumns) {
                while(true) {
                    List<EntryPrinter> children = new ArrayList<>(leftChildren.stream().limit(rows).collect(Collectors.toList()));
                    leftChildren = leftChildren.stream().skip(rows).collect(Collectors.toList());
                    this.printChildren(children, context);
                    if (this.maxEntriesExceeded(context)) {
                        return;
                    }

                    if (leftChildren.isEmpty()) {
                        break;
                    }

                    tree.revertLinePosition(currentLinePos);
                    Iterator<StringBuilder> lines = tree.getLines().stream().skip(tree.getLinePosition()).collect(Collectors.toList()).iterator();
                    if (!lines.hasNext()) {
                        throw new NoSuchElementException();
                    }

                    Integer modifier;
                    label52: {
                        StringBuilder x = lines.next();
                        Function<String, Integer> columnSpaceModifier = tree.getStyle().getColumnSpaceModifier();
                        if (columnSpaceModifier != null) {
                            modifier = columnSpaceModifier.apply(x.toString());
                            if (modifier != null) {
                                break label52;
                            }
                        }

                        modifier = x.length();
                    }

                    int var19 = ((Number)modifier).intValue();

                    while(lines.hasNext()) {
                        label60: {
                            StringBuilder x = lines.next();
                            Function<String, Integer> columnSpaceModifier = tree.getStyle().getColumnSpaceModifier();
                            if (columnSpaceModifier != null) {
                                modifier = columnSpaceModifier.apply(x.toString());
                                if (modifier != null) {
                                    break label60;
                                }
                            }

                            modifier = x.length();
                        }

                        int var21 = ((Number)modifier).intValue();
                        if (var19 < var21) {
                            var19 = var21;
                        }
                    }

                    context.setCurrentColumnLongestEntry(var19);
                    if (i == maxColumns) {
                        break;
                    }

                    ++i;
                }
            }

            tree.setLinePosition(tree.getLines().size() - 1);
            List<StringBuilder> lines = tree.getLines();
            StringBuilder lastBuilder = new StringBuilder(lines.get(lines.size() - 1));
            tree.setCurrentBuilder(lastBuilder);

        }
    }

    private final void printChildren(List<? extends EntryPrinter> children, PrinterContext context) {
        StringTree tree = context.getTree();
        int nestLevel = context.getNestLevel();
        String nest = StringTree.getOrEmpty(tree.getStyle().getCharacters(), TreeTokenType.INDICATOR_VERTICAL_LINE) + Strings.repeat(" ", tree.getStyle().getIndentation());
        String nestColor = StringTree.getOrEmpty(tree.getStyle().getColors(), TreeColorScheme.INDICATORS);
        Iterator<? extends EntryPrinter> it = children.iterator();
        int i = 0;

        while(it.hasNext()) {
            int index = i++;
            EntryPrinter child = it.next();
            TreeTokenType token = index == 0 ? TreeTokenType.INDICATOR_CORNERS_FIRST : (index + 1 < children.size() ? TreeTokenType.INDICATOR_MIDDLE : TreeTokenType.INDICATOR_CORNERS_LAST);
            if (tree.getStyle().getIndentFirstLevel() || nestLevel > 0) {
                int spaces = tree.getStyle().getIndentFirstLevel() ? nestLevel : nestLevel - 1;
                boolean writeIndicator;
                if (context.getCurrentColumnLongestEntry() <= 0) {
                    tree.getCurrentBuilder().append(nestColor).append(Strings.repeat(nest, spaces));
                    writeIndicator = true;
                } else {
                    String currBuilder;
                    Integer modifier;
                    label41: {
                        writeIndicator = false;
                        currBuilder = tree.getCurrentBuilder().toString();
                        Function<String, Integer> spaceModifier = tree.getStyle().getColumnSpaceModifier();
                        if (spaceModifier != null) {
                            modifier = spaceModifier.apply(currBuilder);
                            if (modifier != null) {
                                break label41;
                            }
                        }

                        modifier = currBuilder.length();
                    }

                    int rawBuilderLength = ((Number)modifier).intValue();
                    int netPadding = Math.max(0, context.getCurrentColumnLongestEntry() - rawBuilderLength);
                    StringBuilder var17 = tree.getCurrentBuilder();
                    var17.setLength(0);
                    var17.append(currBuilder);
                    var17.append(Strings.repeat(" ", netPadding));
                    var17.append(context.getTree().getStyle().getColumnIdentation());
                }

                tree.writeIndicator(writeIndicator, token);
            }

            child.print(new PrinterContext(nestLevel + 1, tree, context.getColumn(), context.getCurrentColumnLongestEntry()));
            if (this.maxEntriesExceeded(context)) {
                return;
            }

            if (index + 1 < children.size()) {
                tree.newLine();
            }
        }

    }

    @NotNull
    public String toString() {
        return this.name;
    }
}
