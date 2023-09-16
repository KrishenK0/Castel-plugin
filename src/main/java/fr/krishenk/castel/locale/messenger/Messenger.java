package fr.krishenk.castel.locale.messenger;

import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.LanguageManager;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageProvider;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Objects;

public interface Messenger {
    default String parse(@Nullable Player player, Object ... edits) {
        return this.parse(LanguageManager.localeOf(player), new MessageBuilder().raws(edits).withContext(player));
    }

    default String parse(MessageBuilder settings) {
        return this.parse(settings.getLanguage(), settings);
    }

    default String parse(@Nullable OfflinePlayer player, Object ... edits) {
        return this.parse(LanguageManager.localeOf(player), new MessageBuilder().raws(edits).withContext(player));
    }

    default String parse(SupportedLanguage locale, @NonNull MessageBuilder builder) {
        MessageObject obj = this.getMessageObject(locale);
        return obj == null ? null : obj.buildPlain(builder);
    }

    default String parse(CommandSender sender, Object ... edits) {
        if (!(sender instanceof OfflinePlayer)) {
            return this.parse(edits);
        }
        return this.parse((OfflinePlayer)sender, edits);
    }

    MessageProvider getProvider(SupportedLanguage var1);

    default MessageObject getMessageObject(SupportedLanguage locale) {
        MessageProvider provider = this.getProvider(locale);
        return provider == null ? null : provider.getMessage();
    }

    default String parse(Object ... edits) {
        return this.parse(null, edits);
    }

    default void sendError(CommandSender sender, MessageBuilder settings) {
        if (sender instanceof Player) {
            Player player = (Player)sender;
            Config.errorSound(player);
        }
        this.sendMessage(sender, settings);
    }

    default void sendError(CommandSender sender, Object ... edits) {
        this.sendError(sender, new MessageBuilder().raws(edits).withContext(sender));
    }

    default void sendMessage(CommandSender receiver, Object ... edits) {
        this.sendMessage(receiver, new MessageBuilder().raws(edits).withContext(receiver));
    }

    default void sendMessage(CommandSender receiver, MessageBuilder builder) {
        if (Config.PREFIX.getBoolean()) {
            builder.usePrefix(true);
        }
        SupportedLanguage locale = builder.getLanguage();
        if (receiver instanceof Player) {
            Player player = (Player)receiver;
            locale = CastelPlayer.getCastelPlayer(player).getLanguage();
            builder.lang(locale);
        }
        SupportedLanguage finalLocale = locale;
        MessageProvider provider = Objects.requireNonNull(this.getProvider(locale), () -> "Message for locale '" + finalLocale + "' is null: " + this);
        provider.send(receiver, builder);
    }

    default void sendMessage(CommandSender receiver) {
        this.sendMessage(receiver, new MessageBuilder().withContext(receiver));
    }
}

