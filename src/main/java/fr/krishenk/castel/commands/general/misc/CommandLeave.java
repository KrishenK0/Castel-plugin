package fr.krishenk.castel.commands.general.misc;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

public class CommandLeave extends CastelCommand {

    public CommandLeave() {
        super("leave", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (!cp.hasGuild()) {
                Lang.NO_GUILD_DEFAULT.sendError(player);
            } else if (cp.getRank().isLeader()) {
                Lang.COMMAND_LEAVE_LEADER.sendError(player);
            } else {
                Guild guild = cp.getGuild();
                cp.leaveGuild(LeaveReason.LEFT);
                Lang.COMMAND_LEAVE_SUCCESS.sendMessage(player, "guild", guild.getName());
                for (Player member : guild.getOnlineMembers()) {
                    Lang.COMMAND_LEAVE_ANNOUNCE.sendMessage(member, "left", player.getName());
                }
            }
        }
    }
}
