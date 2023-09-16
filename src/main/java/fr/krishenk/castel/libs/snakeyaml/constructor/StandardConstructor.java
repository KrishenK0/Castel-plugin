
package fr.krishenk.castel.libs.snakeyaml.constructor;

import fr.krishenk.castel.libs.snakeyaml.api.ConstructNode;
import fr.krishenk.castel.libs.snakeyaml.api.LoadSettings;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ConstructorException;
import fr.krishenk.castel.libs.snakeyaml.exceptions.DuplicateKeyException;
import fr.krishenk.castel.libs.snakeyaml.nodes.*;

import java.util.*;

public class StandardConstructor
extends BaseConstructor {
    public StandardConstructor(LoadSettings settings) {
        super(settings);
        this.tagConstructors.put(Tag.NULL, new ConstructYamlNull());
        this.tagConstructors.put(Tag.BOOL, new ConstructYamlBool());
        this.tagConstructors.put(Tag.INT, new ConstructYamlInt());
        this.tagConstructors.put(Tag.FLOAT, new ConstructYamlFloat());
        this.tagConstructors.put(Tag.STR, new ConstructYamlStr());
        this.tagConstructors.put(Tag.SEQ, new ConstructYamlSeq());
        this.tagConstructors.put(Tag.MAP, new ConstructYamlMap());
        this.tagConstructors.put(Tag.ALIAS, new ConstructAlias());
        this.tagConstructors.putAll(settings.getTagConstructors());
    }

    void processDuplicateKeys(MappingNode node) {
        Collection<NodePair> nodeValue = node.getValue();
        HashSet<String> keys = new HashSet<String>(nodeValue.size());
        for (NodePair tuple : nodeValue) {
            ScalarNode keyNode = tuple.getKey();
            if (keys.add(keyNode.getValue())) continue;
            throw new DuplicateKeyException(node.getStartMark(), keyNode, tuple.getKey().getStartMark());
        }
    }

    private LinkedHashMap<String, NodePair> mergeNode(MappingNode node, boolean isPreffered, Map<Object, Integer> key2index, LinkedHashMap<String, NodePair> values) {
        Iterator<Map.Entry<String, NodePair>> iter = node.getPairs().entrySet().iterator();
        block4: while (iter.hasNext()) {
            Map.Entry<String, NodePair> entry = iter.next();
            NodePair nodeTuple = entry.getValue();
            ScalarNode keyNode = nodeTuple.getKey();
            Node valueNode = nodeTuple.getValue();
            if (keyNode.getTag().equals(Tag.MERGE)) {
                iter.remove();
                switch (valueNode.getNodeType()) {
                    case MAPPING: {
                        MappingNode mn = (MappingNode)valueNode;
                        this.mergeNode(mn, false, key2index, values);
                        continue;
                    }
                    case SEQUENCE: {
                        SequenceNode sn = (SequenceNode)valueNode;
                        Collection vals = sn.getValue();
                        for (Object subnode : vals) {
                            if (!(subnode instanceof MappingNode)) {
                                throw new ConstructorException("while constructing a mapping", node.getStartMark(), "expected a mapping for merging, but found " + ((Node) subnode).getNodeType(), ((Node) subnode).getStartMark());
                            }
                            MappingNode mnode = (MappingNode)subnode;
                            this.mergeNode(mnode, false, key2index, values);
                        }
                        continue;
                    }
                    default: {
                        throw new ConstructorException("while constructing a mapping", node.getStartMark(), "expected a mapping or list of mappings for merging, but found " + valueNode.getNodeType(), valueNode.getStartMark());
                    }
                }
            }
            Object key = this.constructObject(keyNode);
            if (!key2index.containsKey(key)) {
                values.put(entry.getKey(), nodeTuple);
                key2index.put(key, values.size() - 1);
                continue;
            }
            if (!isPreffered) continue;
            values.put(entry.getKey(), nodeTuple);
        }
        return values;
    }

    protected void flattenMapping(MappingNode node) {
        this.processDuplicateKeys(node);
        if (node.isMerged()) {
            node.setValue(this.mergeNode(node, true, new HashMap<Object, Integer>(), new LinkedHashMap<String, NodePair>()));
        }
    }

    @Override
    protected void constructMapping2ndStep(MappingNode node, Map<Object, Object> mapping) {
        this.flattenMapping(node);
        super.constructMapping2ndStep(node, mapping);
    }

    public class ConstructYamlNull
    implements ConstructNode {
        @Override
        public Object construct(Node node) {
            if (node != null) {
                StandardConstructor.this.constructScalar((ScalarNode)node);
            }
            return null;
        }
    }

    public class ConstructYamlBool
    implements ConstructNode {
        @Override
        public Object construct(Node node) {
            String val = StandardConstructor.this.constructScalar((ScalarNode)node);
            if ((val = val.toLowerCase(Locale.ENGLISH)).equals("true")) {
                return true;
            }
            if (val.equals("false")) {
                return false;
            }
            throw new IllegalArgumentException("Unknown boolean value: " + val);
        }
    }

    public class ConstructYamlInt
    implements ConstructNode {
        @Override
        public Object construct(Node node) {
            String value = StandardConstructor.this.constructScalar((ScalarNode)node);
            int radix = 10;
            if (value.startsWith("0x")) {
                radix = 16;
                value = value.substring(2);
            } else if (value.charAt(0) == '#') {
                radix = 16;
                value = value.substring(1);
            }
            try {
                return Integer.valueOf(value, radix);
            }
            catch (NumberFormatException e) {
                return Long.valueOf(value, radix);
            }
        }
    }

    public class ConstructYamlFloat
    implements ConstructNode {
        @Override
        public Object construct(Node node) {
            String value = StandardConstructor.this.constructScalar((ScalarNode)node);
            return Double.parseDouble(value);
        }
    }

    public class ConstructYamlStr
    implements ConstructNode {
        @Override
        public Object construct(Node node) {
            return StandardConstructor.this.constructScalar((ScalarNode)node);
        }
    }

    public class ConstructYamlSeq
    implements ConstructNode {
        @Override
        public Object construct(Node node) {
            return StandardConstructor.this.constructSequence((SequenceNode)node);
        }
    }

    public class ConstructYamlMap
    implements ConstructNode {
        @Override
        public Object construct(Node node) {
            return StandardConstructor.this.constructMapping((MappingNode)node);
        }
    }

    public class ConstructAlias
    implements ConstructNode {
        @Override
        public Object construct(Node node) {
            return null;
        }
    }
}

