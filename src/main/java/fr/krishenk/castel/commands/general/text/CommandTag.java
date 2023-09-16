package fr.krishenk.castel.commands.general.text;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.regex.Pattern;

public class CommandTag extends CastelCommand {
    public CommandTag() {
        super("tag", true);
    }

    public static boolean checkTag(String name, Player player) {
        int max = Config.TAGS_MAX_LENGTH.getInt();
        int min = Config.TAGS_MIN_LENGTH.getInt();
        int len = Config.TAGS_IGNORE_COLORS.getBoolean() ? MessageHandler.stripColors(MessageHandler.colorize(name), false).length() : name.length();
        if (len >= min && len <= max) {
            if (!Config.TAGS_ALLOW_NUMBERS.getBoolean() && StringUtils.containsAnyLangNumber(name)) {
                Lang.COMMAND_TAG_NAME_NUMBERS.sendMessage(player);
                return false;
            } else if (!Config.TAGS_ALLOW_NON_ENGLISH.getBoolean() && !StringUtils.isEnglish(name)) {
                Lang.COMMAND_TAG_NAME_ENGLISH.sendMessage(player);
                return false;
            } else if (!Config.TAGS_ALLOW_SYMBOLS.getBoolean() && StringUtils.hasSymbol(name)) {
                Lang.COMMAND_TAG_NAME_HAS_SYMBOLS.sendMessage(player);
                return false;
            } else {
                boolean match = false;
                String smallName = name.toLowerCase();
                for (String regex : Config.TAGS_BLACKLISTED_NAMES.getStringList()) {
                    if (Pattern.compile(regex).matcher(smallName).find()) {
                        match = true;
                        break;
                    }
                }

                if (!match) return true;
                Lang.GUILD_TAG_BLACKLISTED.sendMessage(player, "tag", name);
                return false;
            }
        }
        Lang.COMMAND_TAG_NAME_LENGTH.sendMessage(player, "max", max, "min", min);
        return false;
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (context.assertPlayer() || context.requireArgs(1) || context.assertHasGuild()) {
            return CommandResult.FAILED;
        }
        Player player = context.senderAsPlayer();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        if (!cp.hasPermission(StandardGuildPermission.LORE)) {
            StandardGuildPermission.LORE.sendDeniedMessage(player);
            return CommandResult.FAILED;
        } else {
            String name = StringUtils.buildArguments(context.args, Config.TAGS_ALLOW_SPACES.getBoolean() ? " ": "");
            Guild guild = cp.getGuild();
            if (!cp.isAdmin()) {
                if (!checkTag(name, player)) return CommandResult.FAILED;

                double cost = Config.ECONOMY_COSTS_TAG_GUILD.getDouble();
                if (!guild.hasMoney(cost)) {
                    Lang.COMMAND_TAG_COST.sendMessage(player, "cost", cost);
                    return CommandResult.FAILED;
                }

                guild.addBank(-cost);
            }

            String previousTag = guild.getTag();
            if (guild.renameTag(name, cp).isCancelled()) {
                return CommandResult.FAILED;
            } else {
                MessageBuilder settings = (new MessageBuilder()).parse("tag", previousTag == null ? Lang.NONE : previousTag).withContext(player);
                Lang msg = previousTag == null ? Lang.COMMAND_TAG_SET : Lang.COMMAND_TAG_CHANGED;
                for (Player member : guild.getOnlineMembers()) {
                    msg.sendMessage(member, settings);
                }

                return CommandResult.SUCCESS;
            }
        }
    }
}
