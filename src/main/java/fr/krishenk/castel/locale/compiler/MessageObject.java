package fr.krishenk.castel.locale.compiler;

import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.builders.context.ComplexMessageBuilderContextProvider;
import fr.krishenk.castel.locale.compiler.builders.context.PlainMessageBuilderContextProvider;
import fr.krishenk.castel.locale.provider.AdvancedMessageProvider;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageProvider;
import fr.krishenk.castel.utils.XComponentBuilder;
import fr.krishenk.castel.utils.internal.arrays.UnsafeArrayList;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public class MessageObject implements MessageObjectBuilder {
    public static final MessageObject NULL = new MessageObject(new MessagePiece[]{new MessagePiece.Plain("")}, false, MessageCompilerSettings.none());
    protected final MessagePiece[] pieces;
    private final Boolean usePrefix;
    private final MessageCompilerSettings compilerSettings;

    public MessageObject(MessagePiece[] pieces, Boolean usePrefix, MessageCompilerSettings compilerSettings) {
        this.pieces = pieces;
        this.usePrefix = usePrefix;
        this.compilerSettings = compilerSettings;
    }

    public MessageCompilerSettings getCompilerSettings() {
        return this.compilerSettings;
    }

    public MessagePiece validateAll(Predicate<MessagePiece> validator) {
        for (MessagePiece piece : this.pieces) {
            if (validator.test(piece)) continue;
            return piece;
        }
        return null;
    }

    public MessagePiece[] getPieces() {
        return this.pieces;
    }

    public List<MessageObject> splitBy(Predicate<MessagePiece> predicate) {
        ArrayList<MessageObject> pieces = new ArrayList<MessageObject>(1);
        ArrayList<MessagePiece> pieceBuilder = new ArrayList<MessagePiece>(this.pieces.length / 2);
        for (MessagePiece piece : this.pieces) {
            if (predicate.test(piece)) {
                MessageObject partialObj = new MessageObject(pieceBuilder.toArray(new MessagePiece[0]), this.usePrefix, this.compilerSettings);
                pieces.add(partialObj);
                pieceBuilder.clear();
                continue;
            }
            pieceBuilder.add(piece);
        }
        if (!pieceBuilder.isEmpty()) {
            MessageObject partialObj = new MessageObject(pieceBuilder.toArray(new MessagePiece[0]), this.usePrefix, this.compilerSettings);
            pieces.add(partialObj);
        }
        return pieces;
    }

    public boolean hasPiece(Predicate<MessagePiece> filter) {
        return Arrays.stream(this.pieces).anyMatch(filter);
    }

    public int count(Predicate<MessagePiece> filter) {
        return (int)Arrays.stream(this.pieces).filter(filter).count();
    }

    public MessageObject merge(MessageObject other) {
        MessagePiece[] pieces = new MessagePiece[this.pieces.length + other.pieces.length];
        System.arraycopy(this.pieces, 0, pieces, 0, this.pieces.length);
        System.arraycopy(other.pieces, 0, pieces, this.pieces.length, other.pieces.length);
        return new MessageObject(pieces, false, this.compilerSettings);
    }

    public MessageObject findLastColors() {
        List<MessagePiece> colors = this.findColorPieces(1, true);
        if (colors.isEmpty()) {
            return null;
        }
        return new MessageObject(colors.toArray(new MessagePiece[0]), false, this.compilerSettings);
    }

    public List<MessagePiece> findColorPieces(int colorIndex, boolean backwards) {
        return ColorAccessor.of(this.pieces, colorIndex, backwards);
    }

    @Override
    public XComponentBuilder build(TextComponent first, MessageBuilder settings) {
        XComponentBuilder compBuilder = new XComponentBuilder();
        ComplexMessageBuilderContextProvider contextProvider = new ComplexMessageBuilderContextProvider(compBuilder, first, settings, this.compilerSettings);
        if (this.shouldUsePrefix(settings)) {
            for (MessagePiece piece : MessageObject.getPrefix(settings).pieces) {
                contextProvider.build(piece);
            }
        }
        for (MessagePiece piece : this.pieces) {
            if (settings.ignoreColors && piece instanceof MessagePiece.Color) continue;
            contextProvider.build(piece);
        }
        contextProvider.appendRemaining();
        return compBuilder;
    }

    private static MessageObject getPrefix(MessageBuilder settings) {
        return CastelLang.PREFIX.getMessageObject(settings.getLanguage());
    }

    private boolean shouldUsePrefix(MessageBuilder settings) {
        if (this.usePrefix != null) {
            return this.usePrefix;
        }
        if (settings.usePrefix != null) {
            return settings.usePrefix;
        }
        return false;
    }

    @Override
    public String buildPlain(MessageBuilder settings) {
        PlainMessageBuilderContextProvider contextProvider = new PlainMessageBuilderContextProvider(settings, this.compilerSettings);
        if (this.shouldUsePrefix(settings)) {
            for (MessagePiece piece : MessageObject.getPrefix(settings).pieces) {
                piece.build(contextProvider);
            }
        }
        for (MessagePiece piece : this.pieces) {
            if (settings.ignoreColors && piece instanceof MessagePiece.Color) continue;
            piece.build(contextProvider);
        }
        return contextProvider.merge();
    }

    @Override
    public MessageObject evaluatePlaceholdersOnly(MessageBuilder placeholderContextProvider) {
        UnsafeArrayList<MessagePiece> newPieces = UnsafeArrayList.withSize(new MessagePiece[this.pieces.length]);
        for (MessagePiece piece : this.pieces) {
            if (piece instanceof MessagePiece.Variable) {
                MessagePiece.Variable variable = (MessagePiece.Variable)piece;
                Object translated = variable.getPlaceholder(placeholderContextProvider);
                if (translated == null) {
                    newPieces.add(new MessagePiece.Plain(variable.getPlaceholder().rebuild()));
                    continue;
                }
                if (translated instanceof MessageObjectBuilder) {
                    MessageObjectBuilder obj = (MessageObjectBuilder)translated;
                    newPieces.addAll(obj.evaluatePlaceholdersOnly(placeholderContextProvider).pieces);
                    continue;
                }
                if (!(translated instanceof PlaceholderTranslationContext)) {
                    newPieces.add(new MessagePiece.Plain(translated.toString()));
                    continue;
                }
                MessageObject varObj = variable.getCompiled(translated);
                newPieces.addAll(varObj.pieces);
                continue;
            }
            if (piece instanceof MessagePiece.ColorAccessor) {
                newPieces.addAll(((MessagePiece.ColorAccessor)piece).getLastColors(placeholderContextProvider));
                continue;
            }
            newPieces.add(piece);
        }
        return new MessageObject(newPieces.toArray(), this.usePrefix, this.compilerSettings);
    }

    public MessageProvider getSimpleProvider() {
        return new MessageProvider(this);
    }

    public AdvancedMessageProvider getExtraProvider() {
        return new AdvancedMessageProvider(this, null, null);
    }

    public String toString() {
        StringBuilder str = new StringBuilder(this.pieces.length * 50);
        for (MessagePiece piece : this.pieces) {
            str.append("| ").append(piece);
        }
        return str.toString();
    }
}


