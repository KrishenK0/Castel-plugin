package fr.krishenk.castel.locale;

import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.XComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

public interface MessageObjectBuilder {
    XComponentBuilder build(TextComponent var1, MessageBuilder var2);

    default XComponentBuilder build(MessageBuilder settings) {
        return this.build(new TextComponent(), settings);
    }

    String buildPlain(MessageBuilder var1);

    MessageObject evaluatePlaceholdersOnly(MessageBuilder var1);
}