package fr.krishenk.castel.locale.messenger;

import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageProvider;
import org.bukkit.command.CommandSender;

public class StaticMessenger implements Messenger {
    private final MessageProvider provider;

    public StaticMessenger(String str) {
        this.provider = new MessageProvider(MessageCompiler.compile(str));
    }

    public StaticMessenger(MessageProvider provider) {
        this.provider = provider;
    }

    @Override
    public MessageProvider getProvider(SupportedLanguage locale) {
        return this.provider;
    }

    @Override
    public void sendMessage(CommandSender receiver, MessageBuilder builder) {
        this.getProvider(LanguageManager.localeOf(receiver)).send(receiver, builder);
    }
}
