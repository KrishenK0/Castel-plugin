
package fr.krishenk.castel.libs.snakeyaml.validation;

import fr.krishenk.castel.libs.snakeyaml.nodes.Node;
import fr.krishenk.castel.libs.snakeyaml.nodes.ScalarNode;

import java.util.Collection;
import java.util.Map;

public final class ValidationContext {
    private final ScalarNode relatedKey;
    private final Node node;
    private final Map<String, NodeValidator> validatorMap;
    private final Collection<ValidationFailure> exceptions;

    ValidationContext(ScalarNode relatedKey, Node node, Map<String, NodeValidator> validatorMap, Collection<ValidationFailure> exceptions) {
        this.relatedKey = relatedKey;
        this.node = node;
        this.validatorMap = validatorMap;
        this.exceptions = exceptions;
    }

    public ValidationContext(Node node, Map<String, NodeValidator> validatorMap, Collection<ValidationFailure> exceptions) {
        this(null, node, validatorMap, exceptions);
    }

    public ValidationContext delegate(ScalarNode relatedKey, Node node) {
        return new ValidationContext(relatedKey, node, this.validatorMap, this.exceptions);
    }

    public ScalarNode getRelatedKey() {
        return this.relatedKey;
    }

    public Map<String, NodeValidator> getValidatorMap() {
        return this.validatorMap;
    }

    public Collection<ValidationFailure> getExceptions() {
        return this.exceptions;
    }

    public ValidationFailure fail(ValidationFailure failure) {
        this.exceptions.add(failure);
        return failure;
    }

    public ValidationFailure err(String message) {
        return this.fail(new ValidationFailure(ValidationFailure.Severity.ERROR, this.node, null, message));
    }

    public void warn(String message) {
        this.fail(new ValidationFailure(ValidationFailure.Severity.WARNING, this.node, null, message));
    }

    public Node getNode() {
        return this.node;
    }

    public NodeValidator getValidator(String name) {
        return this.validatorMap.get(name);
    }
}

