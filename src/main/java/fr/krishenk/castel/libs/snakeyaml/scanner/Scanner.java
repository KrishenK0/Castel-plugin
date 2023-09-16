
package fr.krishenk.castel.libs.snakeyaml.scanner;

import fr.krishenk.castel.libs.snakeyaml.tokens.Token;

import java.util.Iterator;

public interface Scanner
extends Iterator<Token> {
    public boolean checkToken(Token.ID ... var1);

    public Token peekToken();

    @Override
    public Token next();
}

