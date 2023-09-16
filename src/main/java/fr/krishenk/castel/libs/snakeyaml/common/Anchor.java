
package fr.krishenk.castel.libs.snakeyaml.common;

import fr.krishenk.castel.libs.snakeyaml.exceptions.EmitterException;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ScannerException;

import java.util.Objects;

public class Anchor {
    private final String identifier;

    public Anchor(String id, Mark startMark) {
        Objects.requireNonNull(id);
        if (id.isEmpty()) {
            throw new IllegalArgumentException("Empty anchor");
        }
        for (int i = 0; i < id.length(); ++i) {
            char ch = id.charAt(i);
            if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z' || ch >= '0' && ch <= '9' || ch == '-') continue;
            String problem = "Invalid character '" + ch + "' in the anchor: " + id;
            if (startMark == null) {
                throw new EmitterException(problem);
            }
            throw new ScannerException(problem, startMark);
        }
        this.identifier = id;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public String toString() {
        return this.identifier;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Anchor)) {
            return false;
        }
        Anchor anchor1 = (Anchor)o;
        return Objects.equals(this.identifier, anchor1.identifier);
    }

    public int hashCode() {
        return this.identifier.hashCode();
    }
}

