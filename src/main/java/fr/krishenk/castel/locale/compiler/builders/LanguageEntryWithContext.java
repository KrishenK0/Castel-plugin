package fr.krishenk.castel.locale.compiler.builders;

import fr.krishenk.castel.locale.LanguageEntry;
import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.messenger.DefinedMessenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageProvider;
import fr.krishenk.castel.utils.XComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public class LanguageEntryWithContext implements MessageObjectBuilder {
    private final LanguageEntry messagePath;
    private final MessageBuilder settings;

    public LanguageEntryWithContext(DefinedMessenger message, MessageBuilder settings) {
        this.messagePath = message.getLanguageEntry();
        this.settings = settings;
    }

    public LanguageEntryWithContext(String ... path) {
        this.messagePath = new LanguageEntry(path);
        this.settings = MessageBuilder.DEFAULT;
    }

    public MessageObjectWithContext toObject(MessageBuilder _s) {
        MessageProvider message = _s.getLanguage().getMessage(this.messagePath, false);
        return message == null ? null : new MessageObjectWithContext(message.getMessage(), this.settings);
    }

    @Override
    public XComponentBuilder build(TextComponent first, MessageBuilder _s) {
        MessageObjectWithContext obj = this.toObject(_s);
        return obj == null ? new XComponentBuilder(this.messagePath.toString()) : obj.build(first, _s);
    }

    @Override
    public String buildPlain(MessageBuilder _s) {
        MessageObjectWithContext obj = this.toObject(_s);
        return obj == null ? this.messagePath.toString() : obj.buildPlain(_s);
    }

    @Override
    public MessageObject evaluatePlaceholdersOnly(MessageBuilder _s) {
        MessageObjectWithContext obj = this.toObject(_s);
        return obj == null ? null : obj.evaluatePlaceholdersOnly(_s);
    }
}

