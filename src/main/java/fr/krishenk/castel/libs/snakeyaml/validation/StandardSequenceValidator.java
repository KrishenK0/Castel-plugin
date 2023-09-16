
package fr.krishenk.castel.libs.snakeyaml.validation;

import fr.krishenk.castel.libs.snakeyaml.nodes.Node;
import fr.krishenk.castel.libs.snakeyaml.nodes.SequenceNode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.IntFunction;

public class StandardSequenceValidator
implements NodeValidator {
    private final Type type;
    private final int minLen;
    private final int maxLen;

    public StandardSequenceValidator(Type type, int minLen, int maxLen) {
        if ((minLen != 0 || maxLen != 0) && minLen >= maxLen) {
            throw new IllegalArgumentException("Validation range cannot be equal or smaller one greater than the bigger one: " + minLen + " - " + maxLen);
        }
        this.type = Objects.requireNonNull(type);
        this.minLen = minLen;
        this.maxLen = maxLen;
    }

    public String toString() {
        return "StandardSequenceValidator<" + (Object)((Object)this.type) + '>' + (this.maxLen == 0 && this.minLen == 0 ? "" : "{" + this.minLen + '-' + this.maxLen + '}');
    }

    @Override
    public ValidationFailure validate(ValidationContext context) {
        if (!(context.getNode() instanceof SequenceNode)) {
            return context.err("Standard sequence validation cannot be used on node: " + (Object)((Object)context.getNode().getNodeType()));
        }
        SequenceNode seq = (SequenceNode)context.getNode();
        int length = seq.getValue().size();
        if (this.minLen != 0 || this.maxLen != 0) {
            if (length < this.minLen) {
                return context.err("Value's length must be greater than " + this.minLen);
            }
            if (length > this.maxLen) {
                return context.err("Value's length must be less than " + this.maxLen);
            }
        }
        Collection collection = (Collection)this.type.constructor.apply(length);
        for (Node item : seq.getValue()) {
            String parsed = String.valueOf(item.getParsed());
            boolean changed = collection.add(parsed);
            if (changed || this.type != Type.SET) continue;
            context.delegate(context.getRelatedKey(), item).warn("Duplicated value '" + parsed + "' in set");
        }
        return null;
    }

    @Override
    public String getName() {
        return this.type.name;
    }

    public static Type getStandardType(String str) {
        switch (str) {
            case "list": {
                return Type.LIST;
            }
            case "set": {
                return Type.SET;
            }
        }
        return null;
    }

    public enum Type {
        LIST("list", len -> new ArrayList(len)),
        SET("set", len -> new HashSet(len));
        private final String name;
        private final IntFunction<Collection<Object>> constructor;

        Type(String name, IntFunction<Collection<Object>> constructor) {
            this.name = name;
            this.constructor = constructor;
        }

        public IntFunction<Collection<Object>> getConstructor() {
            return this.constructor;
        }

        public String getName() {
            return this.name;
        }
    }
}

