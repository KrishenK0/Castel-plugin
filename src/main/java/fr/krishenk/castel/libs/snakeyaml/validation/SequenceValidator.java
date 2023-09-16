
package fr.krishenk.castel.libs.snakeyaml.validation;

import fr.krishenk.castel.libs.snakeyaml.nodes.Node;
import fr.krishenk.castel.libs.snakeyaml.nodes.SequenceNode;

public class SequenceValidator
implements NodeValidator {
    private final NodeValidator type;
    private final NodeValidator elements;

    public SequenceValidator(NodeValidator type, NodeValidator elements) {
        this.type = type;
        this.elements = elements;
    }

    @Override
    public ValidationFailure validate(ValidationContext context) {
        if (!(context.getNode() instanceof SequenceNode)) {
            return context.err("Expected " + this.getName());
        }
        SequenceNode seq = (SequenceNode)context.getNode();
        for (Node item : seq.getValue()) {
            this.elements.validate(context.delegate(context.getRelatedKey(), item));
        }
        this.type.validate(context);
        return null;
    }

    @Override
    public String getName() {
        return "a " + this.type.getName() + " of " + this.elements.getName();
    }

    public String toString() {
        return "SequenceValidator<" + this.type + ">{" + this.elements + '}';
    }
}

