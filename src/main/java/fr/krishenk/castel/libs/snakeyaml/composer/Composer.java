
package fr.krishenk.castel.libs.snakeyaml.composer;

import fr.krishenk.castel.libs.snakeyaml.api.LoadSettings;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentEventsCollector;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentLine;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentType;
import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.constructor.StandardConstructor;
import fr.krishenk.castel.libs.snakeyaml.events.*;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ComposerException;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;
import fr.krishenk.castel.libs.snakeyaml.exceptions.YamlEngineException;
import fr.krishenk.castel.libs.snakeyaml.nodes.*;
import fr.krishenk.castel.libs.snakeyaml.parser.Parser;
import fr.krishenk.castel.libs.snakeyaml.resolver.ResolverContext;
import fr.krishenk.castel.libs.snakeyaml.resolver.ScalarResolver;

import java.util.*;

public class Composer {
    protected final Parser parser;
    private final ScalarResolver scalarResolver;
    private final Map<Anchor, Node> anchors;
    private int nonScalarAliasesCount = 0;
    private final LoadSettings settings;
    private final CommentEventsCollector blockCommentsCollector;
    private final CommentEventsCollector inlineCommentsCollector;

    public Composer(LoadSettings settings, Parser parser) {
        this.parser = parser;
        this.scalarResolver = settings.getScalarResolver();
        this.settings = settings;
        this.anchors = new HashMap<>();
        this.blockCommentsCollector = new CommentEventsCollector(parser, CommentType.BLANK_LINE, CommentType.BLOCK);
        this.inlineCommentsCollector = new CommentEventsCollector(parser, CommentType.IN_LINE);
    }

    public MappingNode getRoot() {
        Optional<Node> document = Optional.empty();
        if (!this.parser.checkEvent(Event.ID.DocumentEnd)) {
            document = Optional.of(this.next());

            Event event = this.parser.next();
            Mark previousDocMark = document.map(Node::getStartMark).orElse(null);
            throw new ComposerException("expected a single document in the stream", previousDocMark, "but found another document", event.getStartMark());
        }
        this.parser.next();
        MappingNode map = (MappingNode)document.orElseGet(MappingNode::new);
        return map;
    }

    public Node next() {
        this.blockCommentsCollector.collectEvents();
        if (this.parser.checkEvent(Event.ID.DocumentEnd)) {
            List<CommentLine> commentLines = this.blockCommentsCollector.consume();
            Mark startMark = commentLines.get(0).getStartMark();
            LinkedHashMap<String, NodePair> children = new LinkedHashMap<String, NodePair>();
            MappingNode node = new MappingNode(Tag.MAP, children, FlowStyle.BLOCK, startMark, null);
            node.setBlockComments(commentLines);
            return node;
        }
        Node node = this.composeNode(Optional.empty());
        this.blockCommentsCollector.collectEvents();
        this.parser.next();
        this.anchors.clear();
        this.nonScalarAliasesCount = 0;
        return node;
    }

//    private Node composeNode(Optional<Node> parent) {
//        Node node;
//        this.blockCommentsCollector.collectEvents();
//        if (this.parser.checkEvent(Event.ID.Alias)) {
//            AliasEvent event = (AliasEvent)this.parser.next();
//            Anchor anchor = event.getAlias();
//            node = this.anchors.get(anchor);
//            if (node == null) {
//                throw new ComposerException("found undefined alias " + anchor, event.getStartMark());
//            }
//            SequenceNode parameters = null;
//            if (this.parser.checkEvent(Event.ID.SequenceStart)) {
//                parameters = this.composeSequenceNode(Optional.empty());
//            }
//            if (node.getNodeType() != NodeType.SCALAR) {
//                ++this.nonScalarAliasesCount;
//                if (this.nonScalarAliasesCount > this.settings.getMaxAliasesForCollections()) {
//                    throw new YamlEngineException("Number of aliases for non-scalar nodes exceeds the specified max=" + this.settings.getMaxAliasesForCollections());
//                }
//            }
//            if (anchor.getIdentifier().startsWith("fn-") && node instanceof MappingNode) {
//                MappingNode map = (MappingNode)node;
//                SequenceNode params = (SequenceNode)map.getNode("args");
//                HashMap<String, Object> parsedParams = new HashMap<String, Object>();
//                List<Node> signature = params.getValue();
//                int index = 0;
//                StandardConstructor ctor = new StandardConstructor(this.settings);
//                for (int i = 0; i < signature.size(); ++i) {
//                    String name = ((ScalarNode)signature.get(index)).getValue();
//                    Node real = parameters.getValue().get(index);
//                    Object parsed = ctor.constructObject(real);
//                    parsedParams.put(name, parsed);
//                    ++index;
//                }
//                node = Objects.requireNonNull(map.getNode("return"), "Cannot find return value for function").clone();
//                Composer.replaceAll(node, parsedParams);
//            }
//            this.blockCommentsCollector.consume();
//            this.inlineCommentsCollector.collectEvents().consume();
//        } else {
//            NodeEvent event = (NodeEvent)this.parser.peekEvent();
//            Optional<Anchor> anchor = event.getAnchor();
//            node = this.parser.checkEvent(Event.ID.Scalar) ? this.composeScalarNode(anchor, this.blockCommentsCollector.consume()) : (this.parser.checkEvent(Event.ID.SequenceStart) ? this.composeSequenceNode(anchor) : this.composeMappingNode(anchor));
//        }
//        return node;
//    }

    private Node composeNode(Optional<Node> parent) {
        Node node;
        this.blockCommentsCollector.collectEvents();
        if (this.parser.checkEvent(Event.ID.Alias)) {
            AliasEvent event = (AliasEvent) this.parser.next();
            Anchor anchor = event.getAlias();
            node = this.anchors.get(anchor);
            if (node == null) {
                throw new ComposerException("found undefined alias " + anchor, event.getStartMark());
            }
            SequenceNode parameters = null;
            if (this.parser.checkEvent(Event.ID.SequenceStart)) {
                parameters = this.composeSequenceNode(Optional.empty());
            }
            if (node.getNodeType() != NodeType.SCALAR) {
                this.nonScalarAliasesCount++;
                if (this.nonScalarAliasesCount > this.settings.getMaxAliasesForCollections()) {
                    throw new YamlEngineException("Number of aliases for non-scalar nodes exceeds the specified max=" + this.settings.getMaxAliasesForCollections());
                }
            }
            if (anchor.getIdentifier().startsWith("fn-") && node instanceof MappingNode) {
                MappingNode map = (MappingNode) node;
                SequenceNode params = (SequenceNode) map.getNode("args");
                HashMap<String, Object> parsedParams = new HashMap<>();
                List<Node> signature = params.getValue();
                int index = 0;
                StandardConstructor ctor = new StandardConstructor(this.settings);
                for (int i = 0; i < signature.size(); ++i) {
                    String name = ((ScalarNode) signature.get(index)).getValue();
                    Node real = parameters.getValue().get(index);
                    Object parsed = ctor.constructObject(real);
                    parsedParams.put(name, parsed);
                    ++index;
                }
                node = Objects.requireNonNull(map.getNode("return"), "Cannot find return value for function").clone();
                Composer.replaceAll(node, parsedParams);
            }
            this.blockCommentsCollector.consume();
            this.inlineCommentsCollector.collectEvents().consume();
        } else {
            NodeEvent event = (NodeEvent) this.parser.peekEvent();
            Optional<Anchor> anchor = event.getAnchor();
            if (this.parser.checkEvent(Event.ID.Scalar)) {
                node = this.composeScalarNode(anchor, this.blockCommentsCollector.consume());
            } else if (this.parser.checkEvent(Event.ID.SequenceStart)) {
                node = this.composeSequenceNode(anchor);
            } else {
                node = this.composeMappingNode(anchor);
            }
        }
        return node;
    }

    static void replaceAll(Node node, Map<String, Object> variables) {
        switch (node.getNodeType()) {
            case SCALAR: {
                ScalarNode scalarNode = (ScalarNode)node;
                String str = scalarNode.getValue();
                for (Map.Entry<String, Object> var : variables.entrySet()) {
                    str = str.replace(var.getKey(), var.getValue().toString());
                }
                scalarNode.setValue(str);
                return;
            }
            case SEQUENCE: {
                String elementVal;
                Object value;
                SequenceNode seq = (SequenceNode)node;
                boolean containsListMerge = false;
                for (Object element : seq.getValue()) {
                    if (!(element instanceof ScalarNode) || !((value = variables.get(elementVal = ((ScalarNode)element).getValue())) instanceof Collection)) continue;
                    containsListMerge = true;
                    break;
                }
                if (containsListMerge) {
                    ArrayList elements = new ArrayList(seq.getValue().size() + 5);
                    for (Node element : seq.getValue()) {
                        String elementVal2;
                        Object value2;
                        if (element instanceof ScalarNode && (value2 = variables.get(elementVal2 = ((ScalarNode)element).getValue())) instanceof Collection) {
                            Collection mergingValues = (Collection)value2;
                            for (Object mergingValue : mergingValues) {
                                elements.add(new ScalarNode(Tag.STR, mergingValue.toString(), ScalarStyle.SINGLE_QUOTED));
                            }
                            continue;
                        }
                        Composer.replaceAll(element, variables);
                        elements.add(element);
                    }
                    seq.setValue(elements);
                } else {
                    for (Object element : seq.getValue()) {
                        if (!(element instanceof ScalarNode) || (value = variables.get(elementVal = ((ScalarNode)element).getValue())) instanceof Collection) {
                            // empty if block
                        }
                        Composer.replaceAll((Node)element, variables);
                    }
                }
                return;
            }
            case MAPPING: {
                MappingNode map = (MappingNode)node;
                for (NodePair pair : map.getValue()) {
                    Composer.replaceAll(pair.getValue(), variables);
                }
                return;
            }
        }
        throw new IllegalArgumentException("Unknown node type to replace: " + node);
    }

    private void registerAnchor(Anchor anchor, Node node) {
        this.anchors.put(anchor, node);
        node.setAnchor(anchor);
    }

    protected Node composeScalarNode(Optional<Anchor> anchor, List<CommentLine> blockComments) {
        ScalarEvent ev = (ScalarEvent)this.parser.next();
        Tag nodeTag = this.scalarResolver.resolve(new ResolverContext(ev.getValue(), ev.getScalarStyle()));
        ScalarNode node = new ScalarNode(nodeTag, ev.getValue(), ev.getScalarStyle(), ev.getStartMark(), ev.getEndMark());
        anchor.ifPresent(a -> this.registerAnchor(a, node));
        node.setBlockComments(blockComments);
        node.setInLineComments(this.inlineCommentsCollector.collectEvents().consume());
        return node;
    }

    protected SequenceNode composeSequenceNode(Optional<Anchor> anchor) {
        SequenceStartEvent startEvent = (SequenceStartEvent)this.parser.next();
        ArrayList<Node> children = new ArrayList<Node>();
        SequenceNode node = new SequenceNode(Tag.SEQ, children, startEvent.getFlowStyle(), startEvent.getStartMark(), null);
        if (startEvent.isFlow()) {
            node.setBlockComments(this.blockCommentsCollector.consume());
        }
        anchor.ifPresent(a -> this.registerAnchor(a, node));
        while (!this.parser.checkEvent(Event.ID.SequenceEnd)) {
            this.blockCommentsCollector.collectEvents();
            if (this.parser.checkEvent(Event.ID.SequenceEnd)) break;
            children.add(this.composeNode(Optional.of(node)));
        }
        if (startEvent.isFlow()) {
            node.setInLineComments(this.inlineCommentsCollector.collectEvents().consume());
        }
        Event endEvent = this.parser.next();
        node.setEndMark(endEvent.getEndMark());
        this.inlineCommentsCollector.collectEvents();
        if (!this.inlineCommentsCollector.isEmpty()) {
            node.setInLineComments(this.inlineCommentsCollector.consume());
        }
        return node;
    }

    protected Node composeMappingNode(Optional<Anchor> anchor) {
        MappingStartEvent startEvent = (MappingStartEvent)this.parser.next();
        LinkedHashMap<String, NodePair> children = new LinkedHashMap<String, NodePair>();
        MappingNode node = new MappingNode(Tag.MAP, children, startEvent.getFlowStyle(), startEvent.getStartMark(), null);
        if (startEvent.isFlow()) {
            node.setBlockComments(this.blockCommentsCollector.consume());
        }
        anchor.ifPresent(a -> this.registerAnchor(a, node));
        while (!this.parser.checkEvent(Event.ID.MappingEnd)) {
            this.blockCommentsCollector.collectEvents();
            if (this.parser.checkEvent(Event.ID.MappingEnd)) break;
            this.composeMappingChildren(children, node);
        }
        if (startEvent.isFlow()) {
            node.setInLineComments(this.inlineCommentsCollector.collectEvents().consume());
        }
        Event endEvent = this.parser.next();
        node.setEndMark(endEvent.getEndMark());
        this.inlineCommentsCollector.collectEvents();
        if (!this.inlineCommentsCollector.isEmpty()) {
            node.setInLineComments(this.inlineCommentsCollector.consume());
        }
        return node;
    }

    protected void composeMappingChildren(LinkedHashMap<String, NodePair> children, MappingNode node) {
        ScalarNode itemKey = this.composeKeyNode(node);
        if (itemKey.getTag().equals(Tag.MERGE)) {
            node.setMerged(true);
        }
        Node itemValue = this.composeValueNode(node);
        children.put(itemKey.getValue(), new NodePair(itemKey, itemValue));
    }

    protected ScalarNode composeKeyNode(MappingNode node) {
        return (ScalarNode)this.composeNode(Optional.of(node));
    }

    protected Node composeValueNode(MappingNode node) {
        return this.composeNode(Optional.of(node));
    }
}

