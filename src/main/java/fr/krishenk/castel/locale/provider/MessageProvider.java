package fr.krishenk.castel.locale.provider;

import fr.krishenk.castel.locale.compiler.MessageObject;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MessageProvider {
    private final @Nullable MessageObject message;

    public MessageProvider(@Nullable MessageObject message) {
        this.message = message;
    }

    public @Nullable MessageObject getMessage() {
        return this.message;
    }

    public void handleExtraServices(CommandSender receiver, MessageBuilder builder) {
    }

    public void send(CommandSender receiver, MessageBuilder builder) {
        if (this.message != null) {
            if (receiver instanceof Player) {
                Player.Spigot spigot = ((Player)receiver).spigot();
                for (BaseComponent[] components : this.message.build(builder).create()) {
                    spigot.sendMessage(components);
                }
            } else {
                receiver.sendMessage(this.message.buildPlain(builder));
            }
        }
        this.handleExtraServices(receiver, builder);
    }
}
