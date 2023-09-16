package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.events.general.GuildLeaderChangeEvent;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.PlayerUtils;
import org.bukkit.OfflinePlayer;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CommandAdminRank extends CastelCommand {
    public CommandAdminRank(CastelParentCommand parent) {
        super("rank", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(2)) {
            OfflinePlayer player = context.getOfflinePlayer(0);
            if (player != null) {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                context.getSettings().withContext(player);
                if (!cp.hasGuild()) {
                    context.sendError(Lang.COMMAND_ADMIN_RANK_NO_GUILD);
                } else {
                    Guild guild = cp.getGuild();
                    Rank rank = guild.getRanks().get(context.arg(1));
                    context.var("rank", context.arg(1));
                    if (rank == null) context.sendError(Lang.COMMAND_ADMIN_RANK_NOT_FOUND);
                    else {
                        Rank previousRank = cp.getRank();
                        context.var("previous_rank", previousRank.getColor() + previousRank.getSymbol() + ' ' + previousRank.getName()).var("rank", rank.getColor() + rank.getSymbol() + ' ' + rank.getName());
                        if (rank.equals(previousRank)) context.sendMessage(Lang.COMMAND_ADMIN_RANK_SAME_RANK);
                        else if (previousRank.isLeader())
                            context.sendMessage(Lang.COMMAND_ADMIN_RANK_CANT_DEMOTE_LEADER);
                        else if (rank.isLeader()) {
                            if (!guild.setLeader(cp, GuildLeaderChangeEvent.Reason.ADMIN).isCancelled())
                                context.sendMessage(Lang.COMMAND_ADMIN_RANK_SUCCESS_LEADER, "previous_rank", previousRank.getName(), "rank", rank.getName(), "guild", guild.getName());
                        } else {
                            cp.setRank(context.isPlayer() ? CastelPlayer.getCastelPlayer(context.senderAsPlayer()) : null, rank);
                            context.sendMessage(Lang.COMMAND_ADMIN_RANK_SUCCESS);
                        }
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) return context.getPlayers(0);
        else if (context.isAtArg(1)) {
            OfflinePlayer player = PlayerUtils.getOfflinePlayer(context.arg(0));
            if (player == null) return emptyTab();

            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            if (guild == null) return emptyTab();

            Set<String> ranks = guild.getRanks().getRanks().keySet();
            String starts = context.arg(1).toLowerCase();
            return starts.isEmpty() ? new ArrayList<>(ranks) : ranks.stream().filter(x -> x.toLowerCase().startsWith(starts)).collect(Collectors.toList());
        }
        return emptyTab();
    }
}
