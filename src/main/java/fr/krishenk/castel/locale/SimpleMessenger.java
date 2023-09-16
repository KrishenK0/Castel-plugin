package fr.krishenk.castel.locale;

import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class SimpleMessenger implements ContextualMessenger {
    protected final CommandSender sender;
    protected final MessageBuilder settings;

    public SimpleMessenger(CommandSender sender, MessageBuilder settings) {
        this.sender = sender;
        this.settings = settings;
    }

    @Override
    public MessageBuilder getSettings() {
        return this.settings;
    }

    @Override
    @NotNull
    public CommandSender getSender() {
        return this.sender;
    }
}

