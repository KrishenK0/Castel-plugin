package fr.krishenk.castel.locale.compiler;

public abstract class MessageTokenHandler {
    public abstract MessageTokenResult consumeUntil(MessageCompiler compiler);
}
