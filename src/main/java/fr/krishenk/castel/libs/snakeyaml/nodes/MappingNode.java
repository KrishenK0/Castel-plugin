
package fr.krishenk.castel.libs.snakeyaml.nodes;

import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.*;
import java.util.function.BiPredicate;

public class MappingNode
extends CollectionNode<NodePair> {
    private LinkedHashMap<String, NodePair> pairs;
    private boolean merged = false;

    public MappingNode(Tag tag, LinkedHashMap<String, NodePair> pairs, FlowStyle flowStyle, Mark startMark, Mark endMark) {
        super(tag, flowStyle, startMark, endMark);
        this.pairs = Objects.requireNonNull(pairs);
    }

    public MappingNode(Tag tag, LinkedHashMap<String, NodePair> pairs, FlowStyle flowStyle) {
        this(tag, pairs, flowStyle, null, null);
    }

    public MappingNode() {
        this(Tag.MAP, new LinkedHashMap<String, NodePair>(), FlowStyle.BLOCK);
    }

    public NodePair put(String key, Node valueNode) {
        ScalarNode keyNode = new ScalarNode(Tag.STR, key, ScalarStyle.PLAIN);
        NodePair pair = new NodePair(keyNode, valueNode);
        this.pairs.put(key, pair);
        return pair;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.MAPPING;
    }

    @Override
    public void cacheConstructed(Object obj) {
    }

    @Override
    public Object getParsed() {
        return null;
    }

    public Node getNode(String key) {
        NodePair pair = this.pairs.get(key);
        return pair == null ? null : pair.getValue();
    }

    public void copyIfDoesntExist(MappingNode other) {
        for (Map.Entry<String, NodePair> pair : other.pairs.entrySet()) {
            Node copyNode = pair.getValue().getValue();
            NodePair currentPair = this.pairs.get(pair.getKey());
            if (currentPair == null) {
                this.pairs.put(pair.getKey(), new NodePair(pair.getValue().getKey().clone(), pair.getValue().getValue().clone()));
                continue;
            }
            Node currentNode = currentPair.getValue();
            if (!(currentNode instanceof MappingNode) || !(copyNode instanceof MappingNode)) continue;
            ((MappingNode)currentNode).copyIfDoesntExist((MappingNode)copyNode);
        }
    }

    public void insertAfter(String key, NodePair pair, BiPredicate<String, NodePair> afterNode) {
        ArrayList<Map.Entry<String, NodePair>> after = null;
        if (afterNode != null) {
            Iterator<Map.Entry<String, NodePair>> iter = this.pairs.entrySet().iterator();
            int beforeSize = 0;
            while (iter.hasNext()) {
                Map.Entry<String, NodePair> next = iter.next();
                ++beforeSize;
                if (!afterNode.test(next.getKey(), next.getValue())) continue;
                after = new ArrayList(this.pairs.size() - beforeSize);
                break;
            }
            while (iter.hasNext()) {
                after.add(iter.next());
                iter.remove();
            }
        } else {
            after = new ArrayList<Map.Entry<String, NodePair>>(this.pairs.entrySet());
            this.pairs.clear();
        }
        this.pairs.put(key, pair);
        after.forEach(x -> this.pairs.put((String)x.getKey(), (NodePair)x.getValue()));
    }

    public LinkedHashMap<String, NodePair> getPairs() {
        return this.pairs;
    }

    @Override
    public Collection<NodePair> getValue() {
        return this.pairs.values();
    }

    public void setValue(LinkedHashMap<String, NodePair> pairs) {
        this.pairs = pairs;
    }

    @Override
    public MappingNode clone() {
        LinkedHashMap<String, NodePair> nodes = new LinkedHashMap<String, NodePair>(this.pairs.size());
        for (Map.Entry<String, NodePair> pair : this.pairs.entrySet()) {
            nodes.put(pair.getKey(), new NodePair(pair.getValue().getKey().clone(), pair.getValue().getValue().clone()));
        }
        MappingNode node = new MappingNode(this.getTag(), nodes, this.getFlowStyle(), this.getStartMark(), this.getEndMark());
        node.copyPropertiesOf(this);
        node.merged = this.merged;
        return node;
    }

    static StringBuilder repeatSpaces(int count) {
        StringBuilder buffer = new StringBuilder(count);
        for (int i = 0; i < count; ++i) {
            buffer.append(' ');
        }
        return buffer;
    }

    static void toString(Collection<NodePair> vals, StringBuilder buf, int level) {
        for (NodePair pair : vals) {
            buf.append((CharSequence)MappingNode.repeatSpaces(level * 2));
            buf.append('\"').append(pair.getKey().getValue()).append('\"');
            buf.append(" => ");
            if (pair.getValue() instanceof MappingNode) {
                buf.append('\n');
                MappingNode.toString(((MappingNode)pair.getValue()).getValue(), buf, level + 1);
            } else {
                buf.append(pair.getValue());
            }
            buf.append('\n');
        }
    }

    public void setMerged(boolean merged) {
        this.merged = merged;
    }

    public boolean isMerged() {
        return this.merged;
    }

    public String toString() {
        StringBuilder buf = new StringBuilder();
        MappingNode.toString(this.getValue(), buf, 1);
        String values = buf.toString();
        return '<' + this.getClass().getName() + " (tag=" + this.getTag() + ", values={\n" + values + "])>";
    }
}

