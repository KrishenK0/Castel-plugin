package fr.krishenk.castel.locale.compiler.builders.context;

import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.MessagePiece;
import fr.krishenk.castel.locale.provider.MessageBuilder;

public abstract class MessageBuilderContextProvider {
    private final MessageCompilerSettings compilerSettings;
    private MessageBuilder settings;

    protected MessageBuilderContextProvider(MessageCompilerSettings compilerSettings, MessageBuilder settings) {
        this.compilerSettings = compilerSettings;
        this.settings = settings;
    }

    public void setSettings(MessageBuilder settings) {
        this.settings = settings;
    }

    public MessageBuilder getSettings() {
        return this.settings;
    }

    public MessageCompilerSettings getCompilerSettings() {
        return this.compilerSettings;
    }

    public abstract void build(MessagePiece piece);
}


