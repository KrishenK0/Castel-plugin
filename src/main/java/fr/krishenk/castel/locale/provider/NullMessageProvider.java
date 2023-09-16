package fr.krishenk.castel.locale.provider;

import fr.krishenk.castel.locale.compiler.MessageCompilerSettings;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.MessagePiece;
import org.bukkit.command.CommandSender;
import org.checkerframework.checker.nullness.qual.NonNull;

public class NullMessageProvider extends MessageProvider {
    private static final NullMessageProvider INSTANCE = new NullMessageProvider();
    private static final MessageObject OBJECT = new MessageObject(new MessagePiece[0], false, MessageCompilerSettings.none());

    private NullMessageProvider() {
        super(null);
    }

    public static NullMessageProvider getInstance() {
        return INSTANCE;
    }

    @Override
    public @NonNull MessageObject getMessage() {
        return OBJECT;
    }

    @Override
    public void handleExtraServices(CommandSender receiver, MessageBuilder builder) {
    }

    @Override
    public void send(CommandSender receiver, MessageBuilder builder) {
    }
}