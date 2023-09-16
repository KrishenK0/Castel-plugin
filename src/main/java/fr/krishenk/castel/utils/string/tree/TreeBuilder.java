package fr.krishenk.castel.utils.string.tree;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TreeBuilder {
    private final Map<String, List<String>> map;

    public TreeBuilder(Map<String, List<String>> map) {
        this.map = map;
    }

    public Map<String, List<String>> getMap() {
        return map;
    }

    public final StringTree parse(TreeStyle style) {
        return new StringTree(this.parseContainer("", this.map), style);
    }

    private ContainerPrinter parseContainer(String key, Map<String, List<String>> map) {
        ContainerPrinter root = new ContainerPrinter(key);
        for (Map.Entry<String, List<String>> entry : map.entrySet()) {
            Object k = entry.getKey();
            Object v = entry.getValue();
            if (v instanceof Map) {
                List<EntryPrinter> children = root.getChildren();
                children.add(this.parseContainer(k.toString(), (Map<String, List<String>>) v));
            } else {
                if (!(v instanceof Collection)) {
                    throw new IllegalArgumentException("Unknown type for tree builder value: " + v);
                }

                ContainerPrinter childEntry = new ContainerPrinter(k.toString());
                for (List<Object> element : (Iterable<List<Object>>) v) {
                    childEntry.getChildren().add(new StringPrinter(String.valueOf(element)));
                }
                root.getChildren().add(childEntry);
            }
        }
            return root;

    }
}
