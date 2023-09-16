
package fr.krishenk.castel.libs.snakeyaml.nodes;

import java.util.Objects;

public final class NodePair {
    private final ScalarNode key;
    private Node value;

    public NodePair(ScalarNode keyNode, Node valueNode) {
        this.key = Objects.requireNonNull(keyNode, "Node pair key cannot be null");
        this.value = Objects.requireNonNull(valueNode, "Node pair value cannot be null");
    }

    public void setValue(Node value) {
        this.value = value;
    }

    public ScalarNode getKey() {
        return this.key;
    }

    public Node getValue() {
        return this.value;
    }

    public String toString() {
        return "<NodePair key=" + this.key + ", value=" + this.value + '>';
    }
}

