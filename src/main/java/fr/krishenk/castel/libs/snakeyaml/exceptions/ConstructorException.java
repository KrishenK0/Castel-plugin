
package fr.krishenk.castel.libs.snakeyaml.exceptions;

public class ConstructorException
extends MarkedYamlEngineException {
    public ConstructorException(String context, Mark contextMark, String problem, Mark problemMark, Throwable cause) {
        super(context, contextMark, problem, problemMark, cause);
    }

    public ConstructorException(String context, Mark contextMark, String problem, Mark problemMark) {
        this(context, contextMark, problem, problemMark, null);
    }
}

