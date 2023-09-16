
package fr.krishenk.castel.libs.snakeyaml.validation;

import fr.krishenk.castel.libs.snakeyaml.api.Dump;
import fr.krishenk.castel.libs.snakeyaml.api.DumpSettings;
import fr.krishenk.castel.libs.snakeyaml.api.SimpleWriter;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentLine;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.nodes.*;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

public final class Validator {
    public static List<ValidationFailure> validate(MappingNode root, NodeValidator validator, Map<String, NodeValidator> validatorMap) {
        Objects.requireNonNull(root);
        Objects.requireNonNull(validator);
        Objects.requireNonNull(validatorMap);
        ArrayList<ValidationFailure> errors = new ArrayList<ValidationFailure>();
        validator.validate(new ValidationContext(root, validatorMap, errors));
        return errors;
    }

    static void removeComments(Node node) {
        boolean alreadyHaveNewLine = false;
        Iterator<CommentLine> iterator = node.getBlockComments().iterator();
        while (iterator.hasNext()) {
            if (iterator.next().getValue().equals("\n")) {
                if (alreadyHaveNewLine) {
                    iterator.remove();
                    continue;
                }
                alreadyHaveNewLine = true;
                continue;
            }
            iterator.remove();
        }
        node.setInLineComments(null);
    }

    static void implicitSchemaOf(MappingNode root) {
        for (Map.Entry<String, NodePair> pair : root.getPairs().entrySet()) {
            Node newValue;
            Node val = pair.getValue().getValue();
            Validator.removeComments(pair.getValue().getKey());
            if (val instanceof SequenceNode) {
                MappingNode mappingNode = new MappingNode();
                newValue = mappingNode;
                mappingNode.put("(type)", new ScalarNode(Tag.STR, "list", ScalarStyle.PLAIN));
                mappingNode.put("(elements)", new ScalarNode(Tag.STR, "str", ScalarStyle.PLAIN));
            } else if (val instanceof MappingNode) {
                newValue = val;
                Validator.implicitSchemaOf((MappingNode)val);
            } else {
                ScalarNode scalarNode = (ScalarNode)val;
                String tagVal = scalarNode.getTag().getValue();
                if (scalarNode.getTag() == Tag.FLOAT) {
                    tagVal = "decimal";
                }
                newValue = new ScalarNode(Tag.STR, tagVal, ScalarStyle.PLAIN);
            }
            pair.getValue().setValue(newValue);
        }
    }

    public static void implicitSchemaGenerator(MappingNode root, Path to) {
        Dump dumper = new Dump(new DumpSettings());
        Validator.implicitSchemaOf(root);
        try (BufferedWriter writer = Files.newBufferedWriter(to, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);){
            dumper.dumpNode(root, new SimpleWriter(writer));
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    static NodeValidator parseStandardScalarType(Node option) {
        String type;
        int minLen = 0;
        int maxLen = 0;
        if (option.getNodeType() == NodeType.SCALAR) {
            type = ((ScalarNode)option).getValue();
            if (type.startsWith("Enum<")) {
                Class<Enum> enumerate;
                String className = type.substring("Enum<".length(), type.length() - 1);
                try {
                    enumerate = (Class<Enum>) Class.forName(className);
                    if (!enumerate.isEnum()) {
                        throw new IllegalStateException("Class '" + className + "' is not an enum");
                    }
                }
                catch (ClassNotFoundException e) {
                    throw new IllegalStateException("Couldn't find class '" + className + '\'');
                }
                return new EnumValidator(enumerate);
            }
            StandardSequenceValidator.Type standardList = StandardSequenceValidator.getStandardType(type);
            if (standardList != null) {
                return new SequenceValidator(new StandardSequenceValidator(standardList, 0, 0), new StandardValidator(StandardValidator.Type.STR, 0, 0));
            }
        } else if (option.getNodeType() == NodeType.MAPPING) {
            LinkedHashMap<String, NodePair> validatorSection = ((MappingNode)option).getPairs();
            NodePair typeNode = validatorSection.get("(type)");
            NodePair min = validatorSection.get("(min)");
            NodePair max = validatorSection.get("(max)");
            type = ((ScalarNode)typeNode.getValue()).getValue();
            if (min != null) {
                minLen = (Integer)min.getValue().getParsed();
            }
            if (max != null) {
                maxLen = (Integer)max.getValue().getParsed();
            }
            if (maxLen == 0 && minLen != 0) {
                maxLen = Integer.MAX_VALUE;
            }
            if (minLen == 0 && maxLen < 0) {
                minLen = Integer.MIN_VALUE;
            }
        } else {
            throw new IllegalStateException("Unexpected validation node: " + option.getStartMark());
        }
        StandardValidator.Type standardType = StandardValidator.getStandardType(type);
        if (standardType != null) {
            return new StandardValidator(standardType, minLen, maxLen);
        }
        return new ExternalNodeValidator(type);
    }

    static NodeValidator parseStandardSequenceType(Node option) {
        String type;
        int minLen = 0;
        int maxLen = 0;
        if (option.getNodeType() == NodeType.SCALAR) {
            type = ((ScalarNode)option).getValue();
        } else if (option.getNodeType() == NodeType.MAPPING) {
            LinkedHashMap<String, NodePair> validatorSection = ((MappingNode)option).getPairs();
            NodePair typeNode = validatorSection.get("(type)");
            NodePair min = validatorSection.get("(min)");
            NodePair max = validatorSection.get("(max)");
            type = ((ScalarNode)typeNode.getValue()).getValue();
            if (min != null) {
                minLen = (Integer)min.getValue().getParsed();
            }
            if (max != null) {
                maxLen = (Integer)max.getValue().getParsed();
            }
        } else {
            throw new IllegalStateException("Unexpected validation node: " + option.getStartMark());
        }
        StandardSequenceValidator.Type standardType = StandardSequenceValidator.getStandardType(type);
        if (standardType != null) {
            return new StandardSequenceValidator(standardType, minLen, maxLen);
        }
        return new ExternalNodeValidator(type);
    }

    static Map<String, NodeValidator> createMappedValidator(MappingNode node) {
        HashMap<String, NodeValidator> repeatedValidator = new HashMap<String, NodeValidator>(node.getPairs().size());
        for (Map.Entry<String, NodePair> entryRule : node.getPairs().entrySet()) {
            String key = entryRule.getKey();
            if (key.charAt(0) == '(' && key.charAt(key.length() - 1) == ')') continue;
            Node rule = entryRule.getValue().getValue();
            NodeValidator parsed = Validator.parseValidator(rule);
            repeatedValidator.put(key, parsed);
        }
        return repeatedValidator;
    }

    static NodeValidator parseStandardMapValidator(MappingNode mappingValidator) {
        Node optionalNode;
        NodePair generalValidatorPair = mappingValidator.getPairs().get("(values)");
        NodeValidator valuesValidator = generalValidatorPair == null ? null : Validator.parseValidator(generalValidatorPair.getValue());
        NodeValidator[] extendedValidators = null;
        NodePair extendsNode = mappingValidator.getPairs().get("(extends)");
        if (extendsNode != null) {
            if (extendsNode.getValue() instanceof SequenceNode) {
                SequenceNode extendList = (SequenceNode)extendsNode.getValue();
                ArrayList<NodeValidator> extendedParsed = new ArrayList<NodeValidator>(extendList.getValue().size());
                for (Node node : extendList.getValue()) {
                    extendedParsed.add(Validator.parseValidator(node));
                }
                extendedValidators = extendedParsed.toArray(new NodeValidator[0]);
            } else {
                extendedValidators = new NodeValidator[]{Validator.parseValidator(extendsNode.getValue())};
            }
        }
        NodeValidator keyValidator = null;
        NodePair keysPair = mappingValidator.getPairs().get("(keys)");
        if (keysPair != null) {
            keyValidator = Validator.parseValidator(keysPair.getValue());
        }
        boolean isOptional = (optionalNode = mappingValidator.getNode("(optional)")) != null && optionalNode.getParsed() == Boolean.TRUE;
        ArrayList<String> requiredKeys = null;
        NodePair requiredKeysPair = mappingValidator.getPairs().get("(required)");
        if (requiredKeysPair != null) {
            if (!(requiredKeysPair.getValue() instanceof SequenceNode)) {
                throw new IllegalStateException("Expected a list here " + requiredKeysPair.getValue().getWholeMark());
            }
            requiredKeys = new ArrayList<String>();
            SequenceNode seq = (SequenceNode)requiredKeysPair.getValue();
            for (Node item : seq.getValue()) {
                requiredKeys.add(((ScalarNode)item).getValue());
            }
        }
        ArrayList<String> valueValidatorKeys = null;
        NodePair valuesKeysPair = mappingValidator.getPairs().get("(values-keys)");
        if (valuesKeysPair != null) {
            if (!(valuesKeysPair.getValue() instanceof SequenceNode)) {
                throw new IllegalStateException("Expected a list here " + valuesKeysPair.getValue().getWholeMark());
            }
            valueValidatorKeys = new ArrayList<String>();
            SequenceNode seq = (SequenceNode)valuesKeysPair.getValue();
            for (Node item : seq.getValue()) {
                valueValidatorKeys.add(((ScalarNode)item).getValue());
            }
        }
        return new StandardMappingValidator(extendedValidators, keyValidator, valuesValidator, valueValidatorKeys, Validator.createMappedValidator(mappingValidator), requiredKeys, isOptional);
    }

    public static NodeValidator parseSchema(MappingNode node) {
        return Validator.parseStandardMapValidator(node);
    }

    static NodeValidator parseValidator(Node localValidator) {
        if (localValidator.getNodeType() == NodeType.SCALAR) {
            return Validator.parseStandardScalarType(localValidator);
        }
        if (localValidator.getNodeType() == NodeType.SEQUENCE) {
            SequenceNode seq = (SequenceNode)localValidator;
            if (seq.getFlowStyle() == FlowStyle.FLOW) {
                HashSet<String> vals = new HashSet<String>(seq.getValue().size());
                for (Node item : seq.getValue()) {
                    ScalarNode scalarItem = (ScalarNode)item;
                    vals.add(scalarItem.getValue().toLowerCase());
                }
                return new FixedValuedValidator(vals);
            }
            ArrayList<NodeValidator> union = new ArrayList<NodeValidator>(seq.getValue().size());
            for (Node node : seq.getValue()) {
                union.add(Validator.parseValidator(node));
            }
            return new UnionValidator(union.toArray(new NodeValidator[0]));
        }
        MappingNode mappingValidator = (MappingNode)localValidator;
        NodePair elementsNode = mappingValidator.getPairs().get("(elements)");
        if (elementsNode != null) {
            NodeValidator mainValidator = Validator.parseStandardSequenceType(localValidator);
            NodeValidator elementsValidator = Validator.parseStandardScalarType(elementsNode.getValue());
            return new SequenceValidator(mainValidator, elementsValidator);
        }
        if (!mappingValidator.getPairs().containsKey("(type)")) {
            return Validator.parseStandardMapValidator(mappingValidator);
        }
        return Validator.parseStandardScalarType(localValidator);
    }
}

