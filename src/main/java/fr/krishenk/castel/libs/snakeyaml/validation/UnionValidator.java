
package fr.krishenk.castel.libs.snakeyaml.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UnionValidator
implements NodeValidator {
    private final NodeValidator[] union;

    public UnionValidator(NodeValidator[] union) {
        this.union = union;
    }

    @Override
    public ValidationFailure validate(ValidationContext context) {
        ArrayList<ValidationFailure> allFails = new ArrayList<ValidationFailure>();
        for (NodeValidator validator : this.union) {
            ArrayList<ValidationFailure> capturedFails = new ArrayList<ValidationFailure>();
            ValidationContext innerContext = new ValidationContext(context.getRelatedKey(), context.getNode(), context.getValidatorMap(), capturedFails);
            ValidationFailure directResult = validator.validate(innerContext);
            if (directResult == null || directResult.getSeverity() == ValidationFailure.Severity.WARNING) {
                context.getExceptions().addAll(capturedFails);
                return null;
            }
            allFails.addAll(capturedFails);
        }
        return context.fail(new ValidationFailure(ValidationFailure.Severity.ERROR, context.getNode(), null, "None of the types matched: " + allFails.stream().map(ValidationFailure::getMessage).collect(Collectors.joining(", "))));
    }

    @Override
    public String getName() {
        return "one of " + Arrays.toString(this.union);
    }

    public String toString() {
        return "UnionValidator{" + Arrays.toString(this.union) + '}';
    }
}

