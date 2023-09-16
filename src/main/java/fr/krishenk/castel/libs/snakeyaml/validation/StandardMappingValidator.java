
package fr.krishenk.castel.libs.snakeyaml.validation;

import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;
import fr.krishenk.castel.libs.snakeyaml.nodes.MappingNode;
import fr.krishenk.castel.libs.snakeyaml.nodes.NodePair;
import fr.krishenk.castel.libs.snakeyaml.nodes.NodeType;
import fr.krishenk.castel.libs.snakeyaml.nodes.Tag;

import java.util.*;

public class StandardMappingValidator
implements NodeValidator {
    private final NodeValidator keyValidator;
    private final NodeValidator valueValidator;
    private final Map<String, NodeValidator> specificValidators;
    private final HashSet<String> requiredKeys;
    private final HashSet<String> valueValidatorKeys;
    private final NodeValidator[] extendedValidators;
    private final boolean optional;

    public StandardMappingValidator(NodeValidator[] extendedValidators, NodeValidator keyValidator, NodeValidator validators, Collection<String> valueValidatorKeys, Map<String, NodeValidator> specificValidators, Collection<String> requiredKeys, boolean isOptional) {
        this.extendedValidators = extendedValidators;
        this.keyValidator = keyValidator;
        this.valueValidatorKeys = valueValidatorKeys == null ? null : new HashSet<String>(valueValidatorKeys);
        this.requiredKeys = requiredKeys == null ? null : new HashSet<String>(requiredKeys);
        this.valueValidator = validators;
        this.specificValidators = specificValidators;
        this.optional = isOptional;
    }

    public Set<String> getRequiredKeys() {
        return this.requiredKeys;
    }

    public Set<String> getValueValidatorKeys() {
        return this.valueValidatorKeys;
    }

    public Map<String, NodeValidator> getSpecificValidators() {
        return this.specificValidators;
    }

    public NodeValidator getKeyValidator() {
        return this.keyValidator;
    }

    public boolean isOptional() {
        return this.optional;
    }

    public NodeValidator getValueValidator() {
        return this.valueValidator;
    }

    public NodeValidator[] getExtendedValidators() {
        return this.extendedValidators;
    }

    public NodeValidator getValidatorForEntry(String key) {
        NodeValidator validator;
        if (this.specificValidators != null && (validator = this.specificValidators.get(key)) != null) {
            return validator;
        }
        if (this.valueValidator != null && (this.valueValidatorKeys == null || this.valueValidatorKeys.contains(key))) {
            return this.valueValidator;
        }
        return null;
    }

    @Override
    public ValidationFailure validate(ValidationContext context) {
        if (this.extendedValidators != null) {
            for (NodeValidator extendedValidator : this.extendedValidators) {
                ValidationFailure res = extendedValidator.validate(context);
                if (res == null) continue;
                return res;
            }
        }
        if (context.getNode().getNodeType() != NodeType.MAPPING) {
            if (this.optional && context.getNode().getTag() == Tag.NULL) {
                return null;
            }
            return context.err("Expected a mapping section, instead got " + context.getNode().getNodeType().name().toLowerCase());
        }
        MappingNode mapping = (MappingNode)context.getNode();
        Set requiredKeys = null;
        if (this.requiredKeys != null) {
            requiredKeys = (Set)this.requiredKeys.clone();
        }
        for (Map.Entry<String, NodePair> entry : mapping.getPairs().entrySet()) {
            NodeValidator validator;
            ValidationFailure failure;
            NodePair pair = entry.getValue();
            if (this.keyValidator != null && (failure = this.keyValidator.validate(context.delegate(pair.getKey(), pair.getKey()))) != null) {
                failure.setMessage("Disallowed key type. " + failure.getMessage());
            }
            if (requiredKeys != null) {
                requiredKeys.remove(entry.getKey());
            }
            if (this.specificValidators != null && (validator = this.specificValidators.get(entry.getKey())) != null) {
                validator.validate(context.delegate(pair.getKey(), pair.getValue()));
                if (!(pair.getValue() instanceof MappingNode)) continue;
            }
            if (this.valueValidator == null || this.valueValidatorKeys != null && !this.valueValidatorKeys.contains(entry.getKey())) continue;
            this.valueValidator.validate(context.delegate(pair.getKey(), pair.getValue()));
        }
        if (requiredKeys != null && !requiredKeys.isEmpty()) {
            return context.err("Missing required entries " + Arrays.toString(requiredKeys.toArray())).withMarker(context.getRelatedKey() == null ? new Mark("[ROOT]", 0, 0, 0, new char[0], 0) : context.getRelatedKey().getStartMark());
        }
        return null;
    }

    @Override
    public String getName() {
        return "a section";
    }

    static int findLongestString(Collection<String> strings) {
        int longest = 0;
        for (String str : strings) {
            longest = Math.max(longest, str.length());
        }
        return longest;
    }

    static String repeat(int times) {
        StringBuilder builder = new StringBuilder(times);
        for (int i = 0; i < times; ++i) {
            builder.append(' ');
        }
        return builder.toString();
    }

    static String padRight(String str, int num) {
        int diff = num - str.length();
        return str + StandardMappingValidator.repeat(diff);
    }

    static String toString(NodeValidator validator, String rootSpaces) {
        StringBuilder builder = new StringBuilder(100);
        String spaces = rootSpaces + "  ";
        if (validator instanceof StandardMappingValidator) {
            StandardMappingValidator mappingValidator = (StandardMappingValidator)validator;
            builder.append("StandardMappingValidator").append(mappingValidator.optional ? "?" : "").append(" {").append('\n');
            if (mappingValidator.keyValidator != null) {
                builder.append(spaces).append("keyValidator=").append(StandardMappingValidator.toString(mappingValidator.keyValidator, rootSpaces + "  ")).append('\n');
            }
            if (mappingValidator.requiredKeys != null && !mappingValidator.requiredKeys.isEmpty()) {
                builder.append(spaces).append("requiredKeys=").append(mappingValidator.requiredKeys).append('\n');
            }
            if (mappingValidator.valueValidator != null) {
                builder.append(spaces).append("valueValidator=").append(StandardMappingValidator.toString(mappingValidator.valueValidator, rootSpaces + "  ")).append('\n');
            }
            if (mappingValidator.specificValidators != null && !mappingValidator.specificValidators.isEmpty()) {
                int longest = StandardMappingValidator.findLongestString(mappingValidator.specificValidators.keySet());
                String innerSpaces = spaces + StandardMappingValidator.repeat(longest) + "    ";
                builder.append(spaces).append("specificValidator={\n");
                for (Map.Entry<String, NodeValidator> entry : mappingValidator.specificValidators.entrySet()) {
                    builder.append(spaces).append("  ").append(StandardMappingValidator.padRight(entry.getKey(), longest)).append(" -> ").append(StandardMappingValidator.toString(entry.getValue(), innerSpaces + "  ")).append('\n');
                }
                builder.append(spaces).append("}\n");
            }
            builder.append(rootSpaces).append('}');
        } else {
            builder.append(validator.toString());
        }
        return builder.toString();
    }

    public String toString() {
        return StandardMappingValidator.toString(this, "");
    }
}

