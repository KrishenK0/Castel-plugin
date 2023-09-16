package fr.krishenk.castel.locale.compiler.builders;

import fr.krishenk.castel.locale.LanguageEntry;
import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.MessagePiece;
import fr.krishenk.castel.locale.messenger.DefinedMessenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageProvider;
import fr.krishenk.castel.utils.XComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.function.Function;

public class RawLanguageEntryObjectBuilder implements MessageObjectBuilder {
    private final LanguageEntry messagePath;
    private final Function<String, String> processor;

    public RawLanguageEntryObjectBuilder(DefinedMessenger message, Function<String, String> processor) {
        this.messagePath = message.getLanguageEntry();
        this.processor = processor;
    }

    private String toPlain(MessageBuilder _s) {
        MessageProvider message = _s.getLanguage().getMessage(this.messagePath, true);
        if (message == null) {return null;}
        String plain = message.getMessage().buildPlain(_s);

        try {
            return this.processor.apply(plain);
        }
        catch (Throwable ex) {
            throw new RuntimeException("Error while running raw language entry processor for entry: " + message, ex);
        }
    }

    @Override
    public XComponentBuilder build(TextComponent first, MessageBuilder _s) {
        return MessageCompiler.compile(this.toPlain(_s)).build(first, _s);
    }

    @Override
    public String buildPlain(MessageBuilder _s) {
        return this.toPlain(_s);
    }

    @Override
    public MessageObject evaluatePlaceholdersOnly(MessageBuilder _s) {
        return new MessageObject(new MessagePiece[]{new MessagePiece.Plain(this.toPlain(_s))}, false, MessageCompilerSettings.all());
    }
}

