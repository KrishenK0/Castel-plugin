
package fr.krishenk.castel.libs.snakeyaml.validation;

public interface NodeValidator {
    public ValidationFailure validate(ValidationContext var1);

    default public String getName() {
        return "a " + this.getClass().getSimpleName();
    }
}

