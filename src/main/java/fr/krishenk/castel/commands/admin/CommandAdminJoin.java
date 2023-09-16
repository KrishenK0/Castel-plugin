package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.*;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminJoin extends CastelCommand {
    public CommandAdminJoin(CastelParentCommand parent) {
        super("join", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(2)) {
            String[] args = context.args;
            OfflinePlayer player = context.getOfflinePlayer(0);
            if (player != null) {
                Guild guild  = context.getGuild(1);
                if (guild != null) {
                    context.getSettings().withContext(player).other(guild).raw("guild", guild.getName());
                    if (guild.isMember(player)) {
                        context.sendError(Lang.COMMAND_ADMIN_JOIN_ALREADY_IN_GUILD);
                    } else {
                        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                        if (cp.hasGuild()) {
                            if (cp.getRank().isLeader()) {
                                context.sendError(Lang.COMMAND_ADMIN_JOIN_LEADER);
                                return;
                            }

                            if (args.length < 3 || !args[2].equalsIgnoreCase("confirm")) {
                                context.sendError(Lang.COMMAND_ADMIN_JOIN_IN_GUILD);
                                return;
                            }

                            cp.leaveGuild(LeaveReason.ADMIN);
                        }

                        cp.joinGuild(guild);
                        context.sendMessage(Lang.COMMAND_ADMIN_JOIN_SUCCESS);
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) return TabCompleteManager.getPlayers(context.arg(0));
        if (context.isAtArg(1)) return TabCompleteManager.getGuilds(context.arg(0));
        return context.isAtArg(2) ? tabComplete("confirm") : emptyTab();
    }
}
