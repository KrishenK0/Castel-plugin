
package fr.krishenk.castel.libs.snakeyaml.exceptions;

import java.util.Objects;

public class ComposerException
extends MarkedYamlEngineException {
    public ComposerException(String context, Mark contextMark, String problem, Mark problemMark) {
        super(context, contextMark, problem, problemMark);
        Objects.requireNonNull(context);
    }

    public ComposerException(String problem, Mark problemMark) {
        super("", null, problem, problemMark);
    }
}

