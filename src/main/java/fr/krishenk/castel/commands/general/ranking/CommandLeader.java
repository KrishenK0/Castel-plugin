package fr.krishenk.castel.commands.general.ranking;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.TabCompleteManager;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.general.GuildLeaderChangeEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandLeader extends CastelCommand {
    public CommandLeader() {
        super("leader", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1) && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (!cp.getRank().isLeader()) Lang.COMMAND_LEADER_ONLY_LEADER.sendError(player);
            else {
                OfflinePlayer leader = context.getOfflinePlayer(0);
                if (leader != null) {
                    CastelPlayer leaderCp = CastelPlayer.getCastelPlayer(leader);
                    if (leaderCp.hasGuild() && leaderCp.isInSameGuildAs(cp)) {
                        if (leaderCp.getUUID().equals(cp.getUUID())) {
                            Lang.COMMAND_LEADER_SELF.sendError(player);
                        } else {
                            Guild guild = cp.getGuild();
                            if (!guild.setLeader(leaderCp, GuildLeaderChangeEvent.Reason.LEADER_DECISION).isCancelled()) {
                                Object[] edits = new Object[]{"player", player.getName(), "rank", leaderCp.getRank().getName(), "leader", leader.getName()};
                                if (Config.ANNOUNCEMENTS_LEADER.getBoolean()) {
                                    Lang.COMMAND_LEADER_SET.sendEveryoneMessage(edits);
                                } else {
                                    for (Player member : guild.getOnlineMembers()) {
                                        Lang.COMMAND_LEADER_SET.sendMessage(member, edits);
                                    }
                                }
                            }
                        }
                    } else {
                        Lang.COMMAND_LEADER_NOT_IN_GUILD.sendError(player, "leader", leader.getName());
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (!context.assertPlayer()) {
            if (!context.assertHasGuild()) {
                if (!context.assertArgs(1)) {
                    return TabCompleteManager.getGuildPlayers(context.getGuild(), context.arg(0));
                }
            } else return tabComplete(Lang.NO_GUILD_DEFAULT.parse(context.senderAsPlayer()));
        }
        return emptyTab();
    }
}
