
package fr.krishenk.castel.libs.snakeyaml.tokens;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Objects;

public final class AnchorToken
extends Token {
    private final Anchor value;

    public AnchorToken(Anchor value, Mark startMark, Mark endMark) {
        super(startMark, endMark);
        Objects.requireNonNull(value);
        this.value = value;
    }

    public Anchor getValue() {
        return this.value;
    }

    @Override
    public Token.ID getTokenId() {
        return Token.ID.Anchor;
    }
}

