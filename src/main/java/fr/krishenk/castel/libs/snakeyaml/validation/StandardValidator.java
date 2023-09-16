
package fr.krishenk.castel.libs.snakeyaml.validation;

import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.nodes.ScalarNode;
import fr.krishenk.castel.libs.snakeyaml.nodes.Tag;

import java.util.Objects;

public class StandardValidator
implements NodeValidator {
    private final Type type;
    private final int minLen;
    private final int maxLen;

    public StandardValidator(Type type, int minLen, int maxLen) {
        if (minLen != 0 || maxLen != 0) {
            if (minLen >= maxLen) {
                throw new IllegalArgumentException("Validation range cannot be equal or smaller one greater than the bigger one: " + minLen + " - " + maxLen);
            }
            if (type != Type.INT && type != Type.DECIMAL) {
                throw new IllegalArgumentException("Cannot have range validation for type: " + (Object)((Object)type));
            }
        }
        this.type = Objects.requireNonNull(type);
        this.minLen = minLen;
        this.maxLen = maxLen;
    }

    public String toString() {
        return "StandardValidator<" + (Object)((Object)this.type) + '>' + (this.maxLen == 0 && this.minLen == 0 ? "" : "{" + this.minLen + '-' + this.maxLen + '}');
    }

    static Type getTypeFromTag(Tag tag) {
        if (tag == Tag.STR) {
            return Type.STR;
        }
        if (tag == Tag.INT) {
            return Type.INT;
        }
        if (tag == Tag.FLOAT) {
            return Type.DECIMAL;
        }
        if (tag == Tag.BOOL) {
            return Type.BOOL;
        }
        if (tag == Tag.NULL) {
            return Type.NULL;
        }
        return null;
    }

    @Override
    public ValidationFailure validate(ValidationContext context) {
        if (!(context.getNode() instanceof ScalarNode)) {
            return context.fail(new ValidationFailure(ValidationFailure.Severity.ERROR, context.getNode(), context.getRelatedKey().getStartMark(), "Wrong type, expected '" + this.type.name + "' but got '" + context.getNode().getTag().getValue() + '\''));
        }
        if (this.type == Type.ANY) {
            return null;
        }
        ScalarNode scalarNode = (ScalarNode)context.getNode();
        Type type = StandardValidator.getTypeFromTag(scalarNode.getTag());
        if (type == null) {
            return context.err("Expected " + this.type.name + ", but got '" + scalarNode.getTag() + "'");
        }
        if (type == Type.NULL) {
            return null;
        }
        if (type != this.type) {
            if (this.type == Type.STR) {
                if (scalarNode.getScalarStyle() == ScalarStyle.PLAIN) {
                    context.warn("Expected a text here, got '" + type.name + "' instead. If this was intended, surround the value with single or double quotes.");
                }
                context.getNode().cacheConstructed(scalarNode.getValue());
            } else if (this.type != Type.DECIMAL || type != Type.INT) {
                return context.err("Wrong type, expected '" + this.type.name + "' but got '" + type.name + '\'');
            }
        }
        scalarNode.setTag(type.getTag());
        if (this.minLen != 0 || this.maxLen != 0) {
            int length = this.type == Type.STR ? scalarNode.getParsed().toString().length() : ((Number)scalarNode.getParsed()).intValue();
            if (length < this.minLen) {
                return context.err("Value's length must be greater than " + this.minLen);
            }
            if (length > this.maxLen) {
                return context.err("Value's length must be less than " + this.maxLen);
            }
        }
        return null;
    }

    @Override
    public String getName() {
        return this.type.name;
    }

    public static Type getStandardType(String str) {
        switch (str) {
            case "int": {
                return Type.INT;
            }
            case "decimal": {
                return Type.DECIMAL;
            }
            case "bool": {
                return Type.BOOL;
            }
            case "str": {
                return Type.STR;
            }
            case "null": {
                return Type.NULL;
            }
            case "any": {
                return Type.ANY;
            }
        }
        return null;
    }

    public enum Type {
        INT("integer", Tag.INT),
        DECIMAL("decimal", Tag.FLOAT),
        BOOL("boolean", Tag.BOOL),
        STR("text", Tag.STR),
        NULL("null", Tag.NULL),
        ANY("any scalar", null);
        private final String name;
        private final Tag tag;

        Type(String name, Tag tag) {
            this.name = name;
            this.tag = tag;
        }

        public Tag getTag() {
            return this.tag;
        }

        public String getName() {
            return this.name;
        }
    }
}

