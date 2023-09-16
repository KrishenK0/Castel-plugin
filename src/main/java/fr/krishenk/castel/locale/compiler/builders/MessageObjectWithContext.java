package fr.krishenk.castel.locale.compiler.builders;

import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.MessagePiece;
import fr.krishenk.castel.locale.compiler.builders.context.ComplexMessageBuilderContextProvider;
import fr.krishenk.castel.locale.compiler.builders.context.PlainMessageBuilderContextProvider;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.XComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class MessageObjectWithContext implements MessageObjectBuilder {
    private final MessageObject message;
    private final MessageBuilder settings;

    public MessageObjectWithContext(MessageObject message, MessageBuilder settings) {
        this.message = message;
        this.settings = settings;
    }

    @Override
    public XComponentBuilder build(TextComponent first, MessageBuilder _s) {
        XComponentBuilder compBuilder = new XComponentBuilder();
        ComplexMessageBuilderContextProvider contextProvider = new ComplexMessageBuilderContextProvider(compBuilder, first, this.settings, this.message.getCompilerSettings());
        for (MessagePiece piece : this.message.getPieces()) {
            if (this.settings.ignoreColors && piece instanceof MessagePiece.Color) continue;
            contextProvider.build(piece);
        }
        contextProvider.appendRemaining();
        return compBuilder;
    }

    @Override
    public String buildPlain(MessageBuilder _s) {
        PlainMessageBuilderContextProvider contextProvider = new PlainMessageBuilderContextProvider(this.settings, this.message.getCompilerSettings());
        for (MessagePiece piece : this.message.getPieces()) {
            if (this.settings.ignoreColors && piece instanceof MessagePiece.Color) continue;
            piece.build(contextProvider);
        }
        return contextProvider.merge();
    }

    @Override
    public MessageObject evaluatePlaceholdersOnly(MessageBuilder placeholderContextProvider) {
        return this.message.evaluatePlaceholdersOnly(this.settings);
    }
}

