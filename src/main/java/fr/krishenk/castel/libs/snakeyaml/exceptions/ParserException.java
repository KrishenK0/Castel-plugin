
package fr.krishenk.castel.libs.snakeyaml.exceptions;

public class ParserException
extends MarkedYamlEngineException {
    public ParserException(String context, Mark contextMark, String problem, Mark problemMark) {
        super(context, contextMark, problem, problemMark, null);
    }

    public ParserException(String problem, Mark problemMark) {
        super(null, null, problem, problemMark, null);
    }
}

