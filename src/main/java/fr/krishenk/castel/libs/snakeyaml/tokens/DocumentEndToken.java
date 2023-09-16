
package fr.krishenk.castel.libs.snakeyaml.tokens;

import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

public final class DocumentEndToken
extends Token {
    public DocumentEndToken(Mark startMark, Mark endMark) {
        super(startMark, endMark);
    }

    @Override
    public Token.ID getTokenId() {
        return Token.ID.DocumentEnd;
    }
}

