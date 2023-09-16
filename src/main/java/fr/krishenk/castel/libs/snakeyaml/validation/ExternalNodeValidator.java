
package fr.krishenk.castel.libs.snakeyaml.validation;

public class ExternalNodeValidator
implements NodeValidator {
    private final String validatorName;

    public ExternalNodeValidator(String validatorName) {
        this.validatorName = validatorName;
    }

    @Override
    public ValidationFailure validate(ValidationContext context) {
        NodeValidator externalValidator = context.getValidator(this.validatorName);
        if (externalValidator == null) {
            throw new IllegalStateException("Unknown external validator: '" + this.validatorName + "' " + context.getNode().getStartMark());
        }
        return externalValidator.validate(context);
    }

    @Override
    public String getName() {
        return this.validatorName;
    }

    public String toString() {
        return "ExternalNodeValidator{" + this.validatorName + '}';
    }
}

