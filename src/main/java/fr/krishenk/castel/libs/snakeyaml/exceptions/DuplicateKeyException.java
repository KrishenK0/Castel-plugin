
package fr.krishenk.castel.libs.snakeyaml.exceptions;

public class DuplicateKeyException
extends ConstructorException {
    public DuplicateKeyException(Mark contextMark, Object key, Mark problemMark) {
        super("while constructing a mapping", contextMark, "found duplicate key " + key.toString(), problemMark);
    }
}

