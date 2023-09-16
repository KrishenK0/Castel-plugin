
package fr.krishenk.castel.libs.snakeyaml.tokens;

import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Objects;

public final class ScalarToken
extends Token {
    private final String value;
    private final ScalarStyle style;

    public ScalarToken(String value, ScalarStyle style, Mark startMark, Mark endMark) {
        super(startMark, endMark);
        this.value = Objects.requireNonNull(value);
        this.style = Objects.requireNonNull(style);
    }

    public String getValue() {
        return this.value;
    }

    public ScalarStyle getStyle() {
        return this.style;
    }

    @Override
    public Token.ID getTokenId() {
        return Token.ID.Scalar;
    }

    @Override
    public String toString() {
        return this.getTokenId().toString() + " style=" + (Object)((Object)this.style) + " value=" + this.value;
    }
}

