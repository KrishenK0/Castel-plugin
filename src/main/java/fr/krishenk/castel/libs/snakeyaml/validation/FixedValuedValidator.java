
package fr.krishenk.castel.libs.snakeyaml.validation;

import fr.krishenk.castel.libs.snakeyaml.nodes.NodeType;
import fr.krishenk.castel.libs.snakeyaml.nodes.ScalarNode;

import java.util.Arrays;
import java.util.Set;

public class FixedValuedValidator
implements NodeValidator {
    private final Set<String> acceptedValues;

    public FixedValuedValidator(Set<String> acceptedValues) {
        this.acceptedValues = acceptedValues;
    }

    @Override
    public ValidationFailure validate(ValidationContext context) {
        if (context.getNode().getNodeType() != NodeType.SCALAR) {
            return context.err("Expected a simple scalar value here, but got a " + context.getNode().getTag().getValue() + " instead");
        }
        ScalarNode scalarNode = (ScalarNode)context.getNode();
        String val = scalarNode.getValue().toLowerCase();
        if (this.acceptedValues.contains(val)) {
            return null;
        }
        return context.err("Unexpected value '" + scalarNode.getValue() + "' expected one of " + Arrays.toString(this.acceptedValues.toArray()));
    }

    @Override
    public String getName() {
        return "one of " + this.acceptedValues;
    }

    public String toString() {
        return "FixedValuedValidator" + this.acceptedValues;
    }
}

