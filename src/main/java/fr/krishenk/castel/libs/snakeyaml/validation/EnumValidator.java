
package fr.krishenk.castel.libs.snakeyaml.validation;

import fr.krishenk.castel.libs.snakeyaml.nodes.NodeType;
import fr.krishenk.castel.libs.snakeyaml.nodes.ScalarNode;

import java.util.Locale;
import java.util.Objects;

public class EnumValidator
implements NodeValidator {
    private final Class<Enum> enumerator;

    public EnumValidator(Class<Enum> enumerator) {
        this.enumerator = Objects.requireNonNull(enumerator);
    }

    @Override
    public ValidationFailure validate(ValidationContext context) {
        if (context.getNode().getNodeType() != NodeType.SCALAR) {
            return context.err("Expected a " + this.enumerator.getSimpleName() + " type, but got an option of type '" + context.getNode().getTag().getValue());
        }
        ScalarNode scalarNode = (ScalarNode)context.getNode();
        try {
            Enum enumerate = Enum.valueOf(this.enumerator, scalarNode.getValue().toUpperCase(Locale.ENGLISH));
            scalarNode.cacheConstructed(enumerate);
        }
        catch (IllegalArgumentException ex) {
            return context.err("Expected a " + this.enumerator.getSimpleName() + " type, but got '" + scalarNode.getValue() + '\'');
        }
        return null;
    }

    @Override
    public String getName() {
        return this.enumerator.getSimpleName();
    }

    public String toString() {
        return "EnumValidator{" + this.enumerator.getName() + '}';
    }
}

