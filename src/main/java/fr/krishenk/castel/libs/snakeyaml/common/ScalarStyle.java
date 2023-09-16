
package fr.krishenk.castel.libs.snakeyaml.common;

public enum ScalarStyle {
    DOUBLE_QUOTED('\"'),
    SINGLE_QUOTED('\''),
    LITERAL('|'),
    FOLDED('>'),
    AUTO('@'),
    PLAIN(':');
    private final char ch;

    ScalarStyle(char ch) {
        this.ch = ch;
    }

    public String toString() {
        return "ScalarStyle{ " + this.name() + " }";
    }
}

