
package fr.krishenk.castel.libs.snakeyaml.api;

import fr.krishenk.castel.libs.snakeyaml.nodes.MappingNode;
import fr.krishenk.castel.libs.snakeyaml.nodes.Node;
import fr.krishenk.castel.libs.snakeyaml.nodes.NodePair;
import fr.krishenk.castel.libs.snakeyaml.nodes.NodeType;
import fr.krishenk.castel.libs.snakeyaml.validation.NodeValidator;
import fr.krishenk.castel.libs.snakeyaml.validation.StandardMappingValidator;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;

public final class Updater {
    public static void copyOldToNew(String k, UpdateResult result, MappingNode old, MappingNode newRoot, LinkedList<Node> path, NodeValidator validator) {
        StandardMappingValidator mapValidator;
        if (validator instanceof StandardMappingValidator && (mapValidator = (StandardMappingValidator)validator).getValueValidator() != null) {
            old.getPairs().entrySet().forEach(e -> newRoot.getPairs().putIfAbsent((String)e.getKey(), (NodePair)e.getValue()));
        }
        Iterator<NodePair> iter = newRoot.getPairs().values().iterator();
        while (iter.hasNext()) {
            NodePair newPair = iter.next();
            String key = newPair.getKey().getValue();
            NodePair oldPair = old.getPairs().get(key);
            if (oldPair != null) {
                boolean optional = false;
                if (validator instanceof StandardMappingValidator) {
                    StandardMappingValidator mapValidator2 = (StandardMappingValidator)validator;
                    boolean bl = optional = mapValidator2.isOptional() && (mapValidator2.getValueValidatorKeys() == null || mapValidator2.getValueValidatorKeys().contains(key));
                }
                if (!optional && newPair.getValue().getNodeType() == NodeType.MAPPING && oldPair.getValue().getNodeType() == NodeType.MAPPING) {
                    LinkedList<Node> newPath = new LinkedList<Node>(path);
                    newPath.addLast(newPair.getKey());
                    NodeValidator newValidator = validator == null || !(validator instanceof StandardMappingValidator) ? null : ((StandardMappingValidator)validator).getValidatorForEntry(key);
                    Updater.copyOldToNew(key, result, (MappingNode)oldPair.getValue(), (MappingNode)newPair.getValue(), newPath, newValidator);
                    continue;
                }
                if (!optional && newPair.getValue().getNodeType() == NodeType.MAPPING) continue;
                newPair.setValue(oldPair.getValue());
                continue;
            }
            if (validator instanceof StandardMappingValidator) {
                boolean isSpecific;
                StandardMappingValidator mapValidator3 = (StandardMappingValidator)validator;
                Set<String> requiredKeys = mapValidator3.getRequiredKeys();
                boolean bl = isSpecific = mapValidator3.getSpecificValidators() != null && mapValidator3.getSpecificValidators().containsKey(key);
                if (!(isSpecific || requiredKeys != null && requiredKeys.contains(key))) {
                    iter.remove();
                    continue;
                }
            }
            LinkedList<Node> newPath = new LinkedList<Node>(path);
            newPath.addLast(newPair.getKey());
            newPath.addLast(newPair.getValue());
            result.addPath(newPath);
        }
    }

    public static UpdateResult updateConfig(MappingNode oldRoot, MappingNode newRoot, NodeValidator validator, Path to, Dump dumper) throws IOException {
        UpdateResult result = new UpdateResult();
        Updater.copyOldToNew(null, result, oldRoot, newRoot, new LinkedList<Node>(), validator);
        try (BufferedWriter writer = Files.newBufferedWriter(to, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);){
            dumper.dumpNode(newRoot, new SimpleWriter(writer));
        }
        return result;
    }
}

