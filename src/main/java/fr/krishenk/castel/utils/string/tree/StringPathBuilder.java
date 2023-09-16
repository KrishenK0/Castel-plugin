package fr.krishenk.castel.utils.string.tree;

import java.util.*;

public class StringPathBuilder {
    private final LinkedHashMap<String, Object> tree;

    public StringPathBuilder(List<String> strings) {
        this.tree = new LinkedHashMap<>(5);
        if (strings.isEmpty()) throw new IllegalArgumentException("Path string list is empty");
        for (String str : strings) {
            LinkedList<String> sections = new LinkedList<>(Arrays.asList(str.split("/")));
            this.put(this.tree, sections);
        }
    }

    protected void put(LinkedHashMap<String, Object> tree, LinkedList<String> list) {
        String first = list.removeFirst();
        boolean isFile = list.isEmpty();
        Object previous = tree.get(first);
        LinkedHashMap<String, Object> nextTree = null;
        if (previous == null) {
            if (isFile) {
                tree.put(first, true);
                nextTree = null;
            } else {
                nextTree = new LinkedHashMap<>();
                tree.put(first, nextTree);
            }
        } else {
            if (isFile) {
                throw new IllegalStateException("Folder and file name are the same: " + first);
            }

            if (previous instanceof Boolean) {
                nextTree = new LinkedHashMap<>();
                tree.put(first, nextTree);
            } else {
                nextTree = (LinkedHashMap<String, Object>) previous;
            }
        }

        if (nextTree != null) {
            this.put(nextTree, list);
        }
    }

    private ContainerPrinter getEntryOf(String name, LinkedHashMap<String, Object> folder) {
        ContainerPrinter entry = new ContainerPrinter(name);
        for (Map.Entry<String, Object> fileName : folder.entrySet()) {
            String k = fileName.getKey();
            Object v = fileName.getValue();
            List<EntryPrinter> list;
            if (v instanceof Boolean) {
                list = entry.getChildren();
                list.add(new StringPrinter(k));
            } else {
                LinkedHashMap<String, Object> nest = (LinkedHashMap<String, Object>) v;
                list = entry.getChildren();
                list.add(this.getEntryOf(k, nest));
            }
        }
        return entry;
    }

    public ContainerPrinter build() {
        return this.getEntryOf("", this.tree);
    }

    public StringTree toStringTree(TreeStyle style) {
        return new StringTree(this.build(), style);
    }
}
