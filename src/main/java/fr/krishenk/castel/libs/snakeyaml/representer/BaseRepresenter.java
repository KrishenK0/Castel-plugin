
package fr.krishenk.castel.libs.snakeyaml.representer;

import fr.krishenk.castel.libs.snakeyaml.api.RepresentToNode;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.YamlEngineException;
import fr.krishenk.castel.libs.snakeyaml.nodes.*;

import java.util.*;

public abstract class BaseRepresenter {
    protected final Map<Class<?>, RepresentToNode> representers = new HashMap();
    protected RepresentToNode nullRepresenter;
    protected final Map<Class<?>, RepresentToNode> parentClassRepresenters = new LinkedHashMap();
    protected final Map<Object, Node> representedObjects = new IdentityHashMap<Object, Node>(){

        @Override
        public Node put(Object key, Node value) {
            return super.put(key, new AnchorNode(value));
        }
    };
    protected Object objectToRepresent;

    public Node represent(Object data) {
        Node node = this.representData(data);
        this.representedObjects.clear();
        this.objectToRepresent = null;
        return node;
    }

    protected Optional<RepresentToNode> findRepresenterFor(Object data) {
        Class<?> clazz = data.getClass();
        if (this.representers.containsKey(clazz)) {
            return Optional.of(this.representers.get(clazz));
        }
        for (Map.Entry<Class<?>, RepresentToNode> parentRepresenterEntry : this.parentClassRepresenters.entrySet()) {
            if (!parentRepresenterEntry.getKey().isInstance(data)) continue;
            return Optional.of(parentRepresenterEntry.getValue());
        }
        return Optional.empty();
    }

    protected final Node representData(Object data) {
        this.objectToRepresent = data;
        if (this.representedObjects.containsKey(this.objectToRepresent)) {
            return this.representedObjects.get(this.objectToRepresent);
        }
        if (data == null) {
            return this.nullRepresenter.representData(null);
        }
        RepresentToNode representer = this.findRepresenterFor(data).orElseThrow(() -> new YamlEngineException("Representer is not defined for " + data.getClass()));
        return representer.representData(data);
    }

    protected Node representScalar(Tag tag, String value, ScalarStyle style) {
        return new ScalarNode(tag, value, style);
    }

    protected Node representScalar(Tag tag, String value) {
        return this.representScalar(tag, value, ScalarStyle.PLAIN);
    }

    protected Node representSequence(Tag tag, Iterable<?> sequence, FlowStyle flowStyle) {
        int size = sequence instanceof Collection ? ((Collection)sequence).size() : 10;
        ArrayList<Node> value = new ArrayList<Node>(size);
        SequenceNode node = new SequenceNode(tag, value, flowStyle);
        this.representedObjects.put(this.objectToRepresent, node);
        if (flowStyle == FlowStyle.AUTO) {
            flowStyle = FlowStyle.BLOCK;
            boolean finalizedBestStyle = false;
            if (size <= 1) {
                flowStyle = FlowStyle.FLOW;
                finalizedBestStyle = true;
            }
            int totalSize = 0;
            boolean firstItem = true;
            for (Object item : sequence) {
                Node nodeItem = this.representData(item);
                if (!finalizedBestStyle) {
                    if (nodeItem instanceof ScalarNode) {
                        Tag itemTag;
                        ScalarNode scalarNode = (ScalarNode)nodeItem;
                        if (firstItem && ((itemTag = scalarNode.getTag()) == Tag.INT || itemTag == Tag.FLOAT || itemTag == Tag.BOOL)) {
                            flowStyle = FlowStyle.FLOW;
                            finalizedBestStyle = true;
                        }
                        if ((totalSize += scalarNode.getValue().length()) > 80) {
                            finalizedBestStyle = true;
                        }
                    } else {
                        finalizedBestStyle = true;
                    }
                }
                value.add(nodeItem);
                firstItem = false;
            }
            node.setFlowStyle(flowStyle);
        }
        return node;
    }

    protected NodePair representMappingEntry(Map.Entry<?, ?> entry) {
        return new NodePair((ScalarNode)this.representData(entry.getKey()), this.representData(entry.getValue()));
    }

    protected Node representMapping(Tag tag, Map<?, ?> mapping, FlowStyle flowStyle) {
        LinkedHashMap<String, NodePair> value = new LinkedHashMap<String, NodePair>(mapping.size());
        MappingNode node = new MappingNode(tag, value, flowStyle);
        this.representedObjects.put(this.objectToRepresent, node);
        for (Map.Entry<?, ?> entry : mapping.entrySet()) {
            NodePair tuple = this.representMappingEntry(entry);
            value.put(tuple.getKey().getValue(), tuple);
        }
        if (flowStyle == FlowStyle.AUTO) {
            node.setFlowStyle(FlowStyle.BLOCK);
        }
        return node;
    }
}

