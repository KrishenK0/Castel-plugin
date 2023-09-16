package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.*;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminResourcePoints extends CastelCommand {
    public CommandAdminResourcePoints(CastelParentCommand parent) {
        super("resourcepoints", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(2)) {
            CommandSender sender = context.getSender();
            byte index;
            Guild guild;
            if (context.argEquals(0, Lang.COMMANDS_TAGS_IDENTIFIER_PLAYERS)) {
                OfflinePlayer player = context.getPlayer(1);
                if (player == null) return;

                guild = CastelPlayer.getCastelPlayer(player).getGuild();
                if (guild == null) {
                    Lang.COMMAND_ADMIN_RESOURCEPOINTS_PLAYER_NO_GUILD.sendMessage(sender);
                    return;
                }

                index = 2;
            } else {
                guild = context.getGuild(0);
                if (guild == null) return;
                index = 1;
            }

            String action;
            String number;
            if (context.assertArgs(index + 2)) {
                action = context.arg(index);
                number = context.arg(index + 1);
            } else {
                action = "add";
                number = context.arg(index);
            }

            SetterHandler.SetterResult result = SetterHandler.eval(action, guild.getResourcePoints(), number);
            if (result == SetterHandler.SetterResult.NOT_NUMBER) Lang.INVALID_NUMBER.sendMessage(sender, "arg", number);
            else if (result == SetterHandler.SetterResult.UNKNOWN)
                Lang.COMMAND_ADMIN_RESOURCEPOINTS_INVALID_ACTION.sendMessage(sender, "action", action);
            else {
                guild.setResourcePoints(Math.max(0L, (long) result.getValue()));
                Lang.COMMAND_ADMIN_RESOURCEPOINTS_DONE.sendMessage(sender, "guild", guild.getName(), "rp", StringUtils.toFancyNumber(guild.getResourcePoints()));
                for (Player member : guild.getOnlineMembers()) {
                    Lang.COMMAND_ADMIN_RESOURCEPOINTS_ADDED.sendMessage(member, "rp", StringUtils.toFancyNumber(guild.getResourcePoints()));
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) {
            List<String> guilds = context.getGuilds(0);
            guilds.add(Lang.COMMANDS_TAGS_IDENTIFIER_PLAYERS.parse());
            return guilds;
        }
        if (context.isAtArg(1))
            return context.argEquals(0, Lang.COMMANDS_TAGS_IDENTIFIER_PLAYERS) ? context.getPlayers(1) : SetterHandler.tabComplete(context.arg(1));
        if (context.isAtArg(2))
            return context.argEquals(0, Lang.COMMANDS_TAGS_IDENTIFIER_PLAYERS) ? SetterHandler.tabComplete(context.arg(2)) : tabComplete("<amount>");
        return context.isAtArg(3) && context.argEquals(0, Lang.COMMANDS_TAGS_IDENTIFIER_PLAYERS) ? tabComplete("<amount>") : emptyTab();
    }
}
