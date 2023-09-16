
package fr.krishenk.castel.libs.snakeyaml.nodes;

import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class SequenceNode
extends CollectionNode<Node> {
    private List<Node> value;
    private Collection<Object> cached;

    public SequenceNode(Tag tag, List<Node> value, FlowStyle flowStyle, Mark startMark, Mark endMark) {
        super(tag, flowStyle, startMark, endMark);
        this.value = Objects.requireNonNull(value, "value in a Node is required.");
    }

    public SequenceNode(Tag tag, List<Node> value, FlowStyle flowStyle) {
        this(tag, value, flowStyle, null, null);
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.SEQUENCE;
    }

    @Override
    public void cacheConstructed(Object obj) {
    }

    @Override
    public Object getParsed() {
        return null;
    }

    @Override
    public SequenceNode clone() {
        ArrayList<Node> nodes = new ArrayList<Node>(this.value.size());
        for (Node node : this.value) {
            nodes.add(node.clone());
        }
        return new SequenceNode(this.getTag(), nodes, this.getFlowStyle(), this.getStartMark(), this.getEndMark()).copyPropertiesOf(this);
    }

    @Override
    protected SequenceNode copyPropertiesOf(Node other) {
        super.copyPropertiesOf(other);
        if (!(other instanceof SequenceNode)) {
            return this;
        }
        SequenceNode seq = (SequenceNode)other;
        this.cached = seq.cached;
        return this;
    }

    @Override
    public List<Node> getValue() {
        return this.value;
    }

    public void setValue(List<Node> value) {
        this.value = value;
    }

    public String toString() {
        return '<' + this.getClass().getName() + " (tag=" + this.getTag() + ", value=" + this.value + ")>";
    }
}

