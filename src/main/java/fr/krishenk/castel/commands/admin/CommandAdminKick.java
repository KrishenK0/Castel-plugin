package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.events.general.GroupDisband;
import fr.krishenk.castel.events.general.GuildLeaderChangeEvent;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

public class CommandAdminKick extends CastelCommand {
    public CommandAdminKick(CastelParentCommand parent) {
        super("kick", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertArgs(1)) {
            context.sendError(Lang.COMMAND_ADMIN_KICK_USAGE);
            return;
        }

        OfflinePlayer player = context.getOfflinePlayer(0);
        if (player != null) {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            if (guild == null) {
                context.sendError(Lang.COMMAND_ADMIN_KICK_NOT_IN_GUILD, "kicked", player.getName());
            } else {
                if (player.getUniqueId().equals(guild.getLeaderId())) {
                    CastelPlayer nextLeader = Rank.determineNextLeader((ArrayList<CastelPlayer>) guild.getCastelPlayers(), null);
                    if (nextLeader == null) {
                        if (!guild.disband(GroupDisband.Reason.ADMIN).isCancelled())
                            context.sendError(Lang.COMMAND_ADMIN_KICK_KICKED_DISBANDED, "kicked", player.getName(), "guild", guild.getName());
                        return;
                    }

                    if (guild.setLeader(nextLeader, GuildLeaderChangeEvent.Reason.ADMIN).isCancelled()) return;

                    context.sendMessage(Lang.COMMAND_ADMIN_KICK_KICKED_LEADER, "kicked", player.getName(), "guild", guild.getName(), "leader", nextLeader.getOfflinePlayer().getName());
                } else
                    context.sendMessage(Lang.COMMAND_ADMIN_KICK_KICKED, "kicked", player.getName(), "guild", guild.getName());

                cp.leaveGuild(LeaveReason.ADMIN);
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? context.getPlayers(0, p -> CastelPlayer.getCastelPlayer(p).hasGuild()) : emptyTab();
    }
}
