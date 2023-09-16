package fr.krishenk.castel.locale;

import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderParser;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public interface ContextualMessenger {
    @NotNull MessageBuilder getSettings();

    @NotNull CommandSender getSender();

    default void sendError(@NotNull Messenger lang, Object ... edits) {
        if (!(edits.length == 0)) {
            this.getSettings().addAll(PlaceholderParser.serializeVariablesIntoContext(Arrays.copyOf(edits, edits.length)));
        }
        lang.sendError(this.getSender(), this.getSettings());
    }

    @Nullable
    default ContextualMessenger var(@NotNull String variable, @Nullable Object replacement) {
        this.getSettings().parse(variable, replacement);
        return this;
    }

    default boolean isPlayer() {
        return this.getSender() instanceof Player;
    }

    default void sendMessage(@NotNull Messenger lang, Object ... edits) {
        this.sendMessage(this.getSender(), lang, Arrays.copyOf(edits, edits.length));
    }

    default void sendMessage(@NotNull CommandSender sender, @NotNull Messenger lang, Object ... edits) {
        if (!(edits.length == 0)) {
            this.getSettings().addAll(PlaceholderParser.serializeVariablesIntoContext(Arrays.copyOf(edits, edits.length)));
        }
        lang.sendMessage(sender, this.getSettings());
    }
}

