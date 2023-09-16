package fr.krishenk.castel.commands.general.ranking;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.TabCompleteManager;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.general.ranks.PlayerRankChangeEvent;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommandDemote extends CastelCommand {
    public CommandDemote() {
        super("demote", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1)) {
            Player player = context.senderAsPlayer();
            CastelPlayer demoterCp = CastelPlayer.getCastelPlayer(player);
            if (!demoterCp.hasGuild()) {
                Lang.NO_GUILD_DEFAULT.sendMessage(player);
            } else if (!demoterCp.hasPermission(StandardGuildPermission.MANAGE_RANKS)) {
                StandardGuildPermission.MANAGE_RANKS.sendDeniedMessage(player);
            } else {
                OfflinePlayer promoting = context.getOfflinePlayer(0);
                if (promoting != null) {
                    CastelPlayer demotingCp = CastelPlayer.getCastelPlayer(promoting);
                    if (!demoterCp.isInSameGuildAs(demotingCp)) {
                        Lang.COMMAND_DEMOTE_NOT_IN_GUILD.sendError(player, "demoted", promoting.getName());
                    } else if (!demoterCp.getRank().isHigherThan(demotingCp.getRank())) {
                        Lang.COMMAND_DEMOTE_CANT_DEMOTE.sendError(player, "demoted", promoting.getName());
                    } else {
                        Guild guild = demoterCp.getGuild();
                        if (guild.getRanks().isMemberRank(demotingCp.getRank())) {
                            Lang.COMMAND_DEMOTE_MEMBER.sendError(player);
                        } else {
                            PlayerRankChangeEvent event = demotingCp.demote(demoterCp);
                            if (!event.isCancelled()) {
                                Rank rank = event.getRank();
                                for (Player member : guild.getOnlineMembers()) {
                                    Lang.COMMAND_DEMOTE_DEMOTED.sendMessage(member, player, "rank", rank.getColor() + rank.getName(), "demoted", promoting.getName());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (!context.assertPlayer()) {
            if (!context.assertHasGuild()) {
                if (!context.assertArgs(1)) return TabCompleteManager.getGuildPlayers(context.getGuild(), context.arg(0), p -> p != context.getSender());
            } else return Collections.singletonList(Lang.NO_GUILD_DEFAULT.parse(context.senderAsPlayer()));
        }

        return new ArrayList<>();
    }
}
