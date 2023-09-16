package fr.krishenk.castel.locale.messenger;

import fr.krishenk.castel.config.AdvancedMessage;
import fr.krishenk.castel.config.Comment;
import fr.krishenk.castel.config.Path;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.LanguageEntry;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageProvider;
import fr.krishenk.castel.utils.internal.arrays.ArrayUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Objects;

public interface DefinedMessenger extends Messenger {
    @Override
    @NotNull
    default MessageProvider getProvider(@NotNull SupportedLanguage locale) {
        return Objects.requireNonNull(locale.getMessage(this.getLanguageEntry(), true));
    }

    @NotNull LanguageEntry getLanguageEntry();

    @Nullable
    default Comment getComment() {
        Comment comment;
        try {
            comment = this.getClass().getField(this.name()).getAnnotation(Comment.class);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return comment;
    }

    @Nullable
    default AdvancedMessage getAdvancedData() {
        AdvancedMessage advancedMessage;
        try {
            advancedMessage = this.getClass().getField(this.name()).getAnnotation(AdvancedMessage.class);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return advancedMessage;
    }

    @NotNull String name();

    @Nullable String getDefaultValue();

    @Override
    default void sendMessage(@NotNull CommandSender receiver, @NotNull MessageBuilder builder) {
        if (Config.PREFIX.getBoolean()) {
            builder.usePrefix(true);
        }
        SupportedLanguage supportedLanguage = receiver instanceof Player ? CastelPlayer.getCastelPlayer((OfflinePlayer)receiver).getLanguage() : builder.getLanguage();
        builder.lang(supportedLanguage);
//        if (CommandAdminTrack.isTracking(receiver) && this != CommandLang.COMMAND_ADMIN_TRACK_TRACKED) {
//            String file = CastelPlugin.getFolder().relativize(supportedLanguage.getMainLanguageFile()).toString().replace(' ', '*');
//            Object object = supportedLanguage.getAdapter().getConfig().findNode(this.getLanguageEntry().getPath());
//            if (object == null || (object = ((Node)object).getStartMark()) == null || (object = ((Node)object).getWholeMark()) == null) {
//                object = CastelLang.UNKNOWN;
//            }
//            Object line = object;
//            CommandLang.COMMAND_ADMIN_TRACK_TRACKED.getProvider(supportedLanguage).send(receiver, new MessageBuilder().parse("path", StringUtils.join(this.getLanguageEntry().getPath(), " {$sep}-> {$s}")).raw("file", file).raw("raw", LanguageManager.getRawMessage(this, supportedLanguage)).parse("line", line));
//        }
        this.getProvider(supportedLanguage).send(receiver, builder);
    }

    @Nullable
    static Path getAnnotatedPath(@NotNull DefinedMessenger messenger) {
        Path path;
        try {
            path = messenger.getClass().getField(messenger.name()).getAnnotation(Path.class);
        }
        catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        return path;
    }

    @NotNull
    static LanguageEntry getEntry(@Nullable String prefix, @NotNull DefinedMessenger lang, @NotNull int[] group) {
        Path annotatedPath = DefinedMessenger.getAnnotatedPath(lang);
        if (annotatedPath != null) {
            return new LanguageEntry(annotatedPath.value());
        }
        int[] finalGroup = group;
        boolean isCommand = lang.name().startsWith("COMMAND_");
        if (isCommand && group.length == 0) {
            int[] arrn;
            int[] arrn2;
            if (lang.name().startsWith("COMMAND_ADMIN")) {
                arrn2 = new int[]{1, 2, 3};
                arrn = arrn2;
            } else {
                arrn2 = new int[]{1, 2};
                arrn = arrn2;
            }
            finalGroup = arrn;
        }
        String[] grouped = StringUtils.split(StringUtils.getGroupedOption(lang.name(), Arrays.copyOf(finalGroup, finalGroup.length)), '.', false).toArray(new String[0]);
        if (prefix != null && !isCommand) {
            grouped = ArrayUtils.merge(new String[]{prefix}, grouped);
        }
        return new LanguageEntry(grouped);
    }


}

