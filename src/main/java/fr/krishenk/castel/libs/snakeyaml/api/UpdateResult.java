
package fr.krishenk.castel.libs.snakeyaml.api;

import fr.krishenk.castel.libs.snakeyaml.nodes.Node;
import fr.krishenk.castel.libs.snakeyaml.nodes.ScalarNode;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class UpdateResult {
    private final List<LinkedList<Node>> added = new ArrayList<LinkedList<Node>>();

    public boolean isChanged() {
        return !this.added.isEmpty();
    }

    public void addPath(LinkedList<Node> path) {
        this.added.add(path);
    }

    public List<Change> getChanges() {
        ArrayList<Change> changes = new ArrayList<Change>(this.added.size());
        for (LinkedList<Node> nodes : this.added) {
            ArrayList<String> path = new ArrayList<String>(nodes.size());
            Iterator iter = nodes.iterator();
            while (iter.hasNext()) {
                Node node = (Node)iter.next();
                if (!iter.hasNext()) {
                    Change change = new Change(path, node);
                    changes.add(change);
                    continue;
                }
                ScalarNode key = (ScalarNode)node;
                path.add(key.getValue());
            }
        }
        return changes;
    }

    public static final class Change {
        final List<String> path;
        final Node value;

        public Change(List<String> path, Node value) {
            this.path = path;
            this.value = value;
        }

        public List<String> getPath() {
            return this.path;
        }

        public Node getValue() {
            return this.value;
        }
    }
}

