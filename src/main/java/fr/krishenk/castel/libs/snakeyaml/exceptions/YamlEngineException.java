
package fr.krishenk.castel.libs.snakeyaml.exceptions;

public class YamlEngineException
extends RuntimeException {
    public YamlEngineException(String message) {
        super(message);
    }

    public YamlEngineException(Throwable cause) {
        super(cause);
    }

    public YamlEngineException(String message, Throwable cause) {
        super(message, cause);
    }
}

