package fr.krishenk.castel.locale.compiler.builders;

import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.MessagePiece;
import fr.krishenk.castel.locale.compiler.builders.context.ComplexMessageBuilderContextProvider;
import fr.krishenk.castel.locale.compiler.builders.context.MessageBuilderContextProvider;
import fr.krishenk.castel.locale.compiler.builders.context.PlainMessageBuilderContextProvider;
import fr.krishenk.castel.locale.messenger.DefinedMessenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.XComponentBuilder;
import fr.krishenk.castel.utils.internal.arrays.UnsafeArrayList;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public class MessageObjectLinker implements MessageObjectBuilder {
    private final List<Pair<MessagePiece[], MessageBuilder>> pieces = new ArrayList<Pair<MessagePiece[], MessageBuilder>>(10);
    private boolean used;

    public MessageObjectLinker add(MessageObject obj, MessageBuilder settings) {
        this.pieces.add(Pair.of(obj.getPieces(), settings));
        return this;
    }

    public MessageObjectLinker add(DefinedMessenger lang, MessageBuilder settings) {
        return this.add(lang.getMessageObject(settings.getLanguage()), settings);
    }

    public MessageObjectLinker add(String plain) {
        this.pieces.add(Pair.of(new MessagePiece[]{new MessagePiece.Plain(plain)}, null));
        return this;
    }

    @Override
    public XComponentBuilder build(TextComponent first, MessageBuilder settings) {
        this.checkUsed();
        XComponentBuilder compBuilder = new XComponentBuilder();
        ComplexMessageBuilderContextProvider contextProvider = new ComplexMessageBuilderContextProvider(compBuilder, first, settings, MessageCompiler.DEFAULT_COMPILER_SETTINGS);
        this.handleIndividual(contextProvider, settings);
        contextProvider.appendRemaining();
        return compBuilder;
    }

    private void checkUsed() {
        if (this.used) {
            throw new IllegalStateException("This message linker has already been used");
        }
        this.used = true;
    }

    void handleIndividual(MessageBuilderContextProvider contextProvider, MessageBuilder settings) {
        for (Pair<MessagePiece[], MessageBuilder> piece : this.pieces) {
            if (piece.getValue() != null) {
                contextProvider.setSettings(piece.getValue().inheritPlaceholders(settings));
            }
            for (MessagePiece subPiece : piece.getKey()) {
                if (contextProvider.getSettings().ignoreColors && subPiece instanceof MessagePiece.Color) continue;
                contextProvider.build(subPiece);
            }
        }
    }

    @Override
    public String buildPlain(MessageBuilder settings) {
        this.checkUsed();
        PlainMessageBuilderContextProvider contextProvider = new PlainMessageBuilderContextProvider(settings, MessageCompiler.DEFAULT_COMPILER_SETTINGS);
        this.handleIndividual(contextProvider, settings);
        return contextProvider.merge();
    }

    @Override
    public MessageObject evaluatePlaceholdersOnly(MessageBuilder placeholderContextProvider) {
        UnsafeArrayList<MessagePiece> newPieces = UnsafeArrayList.withSize(new MessagePiece[this.pieces.size() * 10]);
        for (Pair<MessagePiece[], MessageBuilder> piece : this.pieces) {
            MessageObject obj = new MessageObject(piece.getKey(), false, MessageCompiler.DEFAULT_COMPILER_SETTINGS);
            obj = obj.evaluatePlaceholdersOnly(piece.getValue());
            newPieces.addAll(obj.getPieces());
        }
        return new MessageObject(newPieces.toArray(), false, MessageCompiler.DEFAULT_COMPILER_SETTINGS);
    }
}

