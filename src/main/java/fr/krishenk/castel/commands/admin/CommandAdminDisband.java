package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.general.GroupDisband;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminDisband extends CastelCommand {
    public CommandAdminDisband(CastelParentCommand parent) {
        super("disband", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertArgs(1)) {
            context.sendError(Lang.COMMAND_ADMIN_DISBAND_USAGE);
            return;
        }
        boolean silent = false;
        int silentArgIndex = 1;
        Guild guild;
        if (context.argEquals(0, Lang.COMMANDS_TAGS_PLAYERS)) {
            Player player = context.getPlayer(0);
            if (player == null) return;

            guild = CastelPlayer.getCastelPlayer(player).getGuild();
            if (guild == null) {
                context.sendError(Lang.NO_GUILD_TARGET);
                return;
            }

            ++silentArgIndex;
        } else {
            guild = context.getGuild(0);
            if (guild == null) return;
        }

        if (!guild.triggerDisbandEvent(GroupDisband.Reason.ADMIN).isCancelled()) {
            context.sendMessage(Lang.COMMAND_ADMIN_DISBAND_SUCCESS, "guild", guild.getName());
            if (context.assertArgs(3) && context.arg(silentArgIndex).equals("silent") && Config.DISBAND_ANNOUNCE.getBoolean()) {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    Lang.COMMAND_ADMIN_DISBAND_ANNOUNCE.sendMessage(player, "player", context.getSender().getName(), "guild", guild.getName());
                }
            }

            guild.disband(null);
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) {
            List<String> list = context.getGuilds(0);
            list.add(Lang.COMMANDS_TAGS_PLAYERS.parse());
            return list;
        }
        if (context.isAtArg(1))
            return context.argEquals(0, Lang.COMMANDS_TAGS_PLAYERS) ? context.getPlayers(1) : tabComplete("silent");
        return context.isAtArg(2) && context.argEquals(0, Lang.COMMANDS_TAGS_PLAYERS) ? tabComplete("silent") : emptyTab();
    }
}
