
package fr.krishenk.castel.libs.snakeyaml.constructor;

import fr.krishenk.castel.libs.snakeyaml.api.ConstructNode;
import fr.krishenk.castel.libs.snakeyaml.api.LoadSettings;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ConstructorException;
import fr.krishenk.castel.libs.snakeyaml.exceptions.YamlEngineException;
import fr.krishenk.castel.libs.snakeyaml.nodes.*;

import java.util.*;

public abstract class BaseConstructor {
    protected final LoadSettings settings;
    protected final Map<Tag, ConstructNode> tagConstructors = new HashMap<Tag, ConstructNode>();

    public BaseConstructor(LoadSettings settings) {
        this.settings = settings;
    }

    public Object construct(MappingNode node) {
        try {
            return this.constructObject(node);
        }
        catch (YamlEngineException e) {
            throw e;
        }
        catch (RuntimeException e) {
            throw new YamlEngineException(e);
        }
    }

    public Object constructObject(Node node) {
        Object parsed;
        Objects.requireNonNull(node, "Node cannot be null");
        if (node.getParsed() != null) {
            return node.getParsed();
        }
        ConstructNode constructor = this.findConstructorFor(node).orElseThrow(() -> new ConstructorException(null, null, "could not determine a constructor for the tag " + node.getTag(), node.getStartMark()));
        try {
            parsed = constructor.construct(node);
        }
        catch (ConstructorException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new ConstructorException(null, null, "error while constructing object for node with tag " + node.getTag(), node.getStartMark(), ex);
        }
        node.cacheConstructed(parsed);
        return parsed;
    }

    protected Optional<ConstructNode> findConstructorFor(Node node) {
        Tag tag = node.getTag();
        ConstructNode ctor = this.settings.getTagConstructors().get(tag);
        if (ctor == null) {
            ctor = this.tagConstructors.get(tag);
        }
        if (ctor == null) {
            return Optional.empty();
        }
        return Optional.of(ctor);
    }

    protected String constructScalar(ScalarNode node) {
        return node.getValue();
    }

    protected List<Object> constructSequence(SequenceNode node) {
        List result = this.settings.getDefaultList().apply(node.getValue().size());
        for (Node child : node.getValue()) {
            result.add(this.constructObject(child));
        }
        return result;
    }

    protected Map<Object, Object> constructMapping(MappingNode node) {
        Map mapping = this.settings.getDefaultMap().apply(node.getValue().size());
        this.constructMapping2ndStep(node, mapping);
        return mapping;
    }

    protected void constructMapping2ndStep(MappingNode node, Map<Object, Object> mapping) {
        Collection<NodePair> nodeValue = node.getValue();
        for (NodePair tuple : nodeValue) {
            ScalarNode keyNode = tuple.getKey();
            Node valueNode = tuple.getValue();
            Object key = this.constructObject(keyNode);
            if (key != null) {
                try {
                    key.hashCode();
                }
                catch (Exception e) {
                    throw new ConstructorException("while constructing a mapping", node.getStartMark(), "found unacceptable key " + key, tuple.getKey().getStartMark(), e);
                }
            }
            mapping.put(key, this.constructObject(valueNode));
        }
    }
}

