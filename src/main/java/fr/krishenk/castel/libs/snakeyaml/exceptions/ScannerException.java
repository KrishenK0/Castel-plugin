
package fr.krishenk.castel.libs.snakeyaml.exceptions;

public class ScannerException
extends MarkedYamlEngineException {
    public ScannerException(String context, Mark contextMark, String problem, Mark problemMark) {
        super(context, contextMark, problem, problemMark, null);
    }

    public ScannerException(String problem, Mark problemMark) {
        super(null, null, problem, problemMark, null);
    }
}

