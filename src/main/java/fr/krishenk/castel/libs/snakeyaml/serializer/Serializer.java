
package fr.krishenk.castel.libs.snakeyaml.serializer;

import fr.krishenk.castel.libs.snakeyaml.api.DumpSettings;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentLine;
import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.emitter.Emitable;
import fr.krishenk.castel.libs.snakeyaml.events.*;
import fr.krishenk.castel.libs.snakeyaml.nodes.*;

import java.util.*;

public class Serializer {
    private final DumpSettings settings;
    private final Emitable emitable;
    private final Set<Node> serializedNodes = new HashSet<Node>();
    private final Map<Node, Anchor> anchors = new HashMap<Node, Anchor>();

    public Serializer(DumpSettings settings, Emitable emitable) {
        this.settings = settings;
        this.emitable = emitable;
    }

    public void serializeDocument(Node node) {
        this.anchorNode(node);
        this.serializeNode(node, false);
        this.serializedNodes.clear();
        this.anchors.clear();
    }

    private void anchorNode(Node node) {
        Node realNode = node.getNodeType() == NodeType.ANCHOR ? ((AnchorNode)node).getRealNode() : node;
        if (this.anchors.containsKey(realNode)) {
            this.anchors.computeIfAbsent(realNode, a -> this.settings.getAnchorGenerator().nextAnchor(realNode));
        } else {
            this.anchors.put(realNode, realNode.getAnchor() != null ? realNode.getAnchor() : null);
            switch (realNode.getNodeType()) {
                case SEQUENCE: {
                    SequenceNode seqNode = (SequenceNode)realNode;
                    Collection<Node> list2 = seqNode.getValue();
                    for (Node item : list2) {
                        this.anchorNode(item);
                    }
                    break;
                }
                case MAPPING: {
                    MappingNode mappingNode = (MappingNode)realNode;
                    Collection<NodePair> map = mappingNode.getValue();
                    for (NodePair object : map) {
                        ScalarNode key = object.getKey();
                        Node value = object.getValue();
                        this.anchorNode(key);
                        this.anchorNode(value);
                    }
                    break;
                }
            }
        }
    }

    private void serializeNode(Node node, boolean isKey) {
        if (node.getNodeType() == NodeType.ANCHOR) {
            node = ((AnchorNode)node).getRealNode();
        }
        Optional<Anchor> tAlias = Optional.ofNullable(this.anchors.get(node));
        if (this.serializedNodes.contains(node)) {
            this.emitable.emit(new AliasEvent(tAlias));
        } else {
            this.serializedNodes.add(node);
            switch (node.getNodeType()) {
                case SCALAR: {
                    ScalarNode scalarNode = (ScalarNode)node;
                    this.serializeComments(node.getBlockComments());
                    this.emitable.emit(new ScalarEvent(tAlias, scalarNode.getValue(), scalarNode.getScalarStyle()));
                    this.serializeComments(node.getInLineComments());
                    break;
                }
                case SEQUENCE: {
                    SequenceNode seqNode = (SequenceNode)node;
                    this.serializeComments(node.getBlockComments());
                    this.emitable.emit(new SequenceStartEvent(tAlias, Optional.of(node.getTag().getValue()), Serializer.determineFlowStyle(seqNode)));
                    Collection<Node> list2 = seqNode.getValue();
                    for (Node item : list2) {
                        this.serializeNode(item, false);
                    }
                    this.emitable.emit(new SequenceEndEvent());
                    this.serializeComments(node.getInLineComments());
                    break;
                }
                default: {
                    this.serializeComments(node.getBlockComments());
                    MappingNode mappingNode = (MappingNode)node;
                    Collection<NodePair> map = mappingNode.getValue();
                    if (mappingNode.getTag() == Tag.COMMENT) break;
                    this.emitable.emit(new MappingStartEvent(tAlias, Optional.of(mappingNode.getTag().getValue()), mappingNode.getFlowStyle(), null, null));
                    for (NodePair entry : map) {
                        ScalarNode key = entry.getKey();
                        Node value = entry.getValue();
                        this.serializeNode(key, true);
                        this.serializeNode(value, false);
                    }
                    this.emitable.emit(new MappingEndEvent());
                    this.serializeComments(node.getInLineComments());
                }
            }
        }
    }

    static FlowStyle determineFlowStyle(SequenceNode node) {
        if (node.getFlowStyle() != FlowStyle.AUTO) {
            return node.getFlowStyle();
        }
        if (node.getValue().size() <= 1) {
            return FlowStyle.FLOW;
        }
        Tag itemTag = ((Node)node.getValue().get(0)).getTag();
        if (itemTag == Tag.INT || itemTag == Tag.FLOAT || itemTag == Tag.BOOL) {
            return FlowStyle.FLOW;
        }
        return FlowStyle.BLOCK;
    }

    private void serializeComments(List<CommentLine> comments) {
        if (comments == null) {
            return;
        }
        for (CommentLine line : comments) {
            CommentEvent commentEvent = new CommentEvent(line.getCommentType(), line.getValue(), line.getStartMark(), line.getEndMark());
            this.emitable.emit(commentEvent);
        }
    }
}

