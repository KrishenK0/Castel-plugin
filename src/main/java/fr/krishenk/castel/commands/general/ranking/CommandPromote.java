package fr.krishenk.castel.commands.general.ranking;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.TabCompleteManager;
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

public class CommandPromote extends CastelCommand {
    public CommandPromote() {
        super("promote", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1) && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (!cp.hasPermission(StandardGuildPermission.MANAGE_RANKS)) {
                StandardGuildPermission.MANAGE_RANKS.sendDeniedMessage(player);
            } else {
                OfflinePlayer promoting = context.getOfflinePlayer(0);
                if (promoting != null) {
                    CastelPlayer promotingCp = CastelPlayer.getCastelPlayer(promoting);
                    if (!cp.isInSameGuildAs(cp)) Lang.COMMAND_PROMOTE_NOT_IN_GUILD.sendError(player, "promoted", promoting.getName());
                    else promote(cp, promotingCp);
                }
            }
        }
    }

    public static boolean promote(CastelPlayer promoterCp, CastelPlayer promotedCp) {
        Player promoter = promoterCp.getPlayer();
        OfflinePlayer promoting = promotedCp.getOfflinePlayer();
        Rank promotedRank = promotedCp.getRank();
        if (!promoterCp.isAdmin() && !promoterCp.getRank().isHigherThan(promotedRank)) {
            Lang.COMMAND_PROMOTE_CANT_PROMOTE.sendError(promoter, "promoted", promoting.getName());
            return false;
        } else if (!promotedRank.canBePromoted()) {
            Lang.COMMAND_PROMOTE_LEADER.sendError(promoter, "promoted", promoting.getName(), "rank", promotedRank.getColor() + promotedRank.getName());
            return false;
        } else {
            PlayerRankChangeEvent newRankEvent = promotedCp.promote(promoterCp);
            if (newRankEvent.isCancelled()) return false;
            else {
                Rank newRank = newRankEvent.getRank();
                for (Player member : promotedCp.getGuild().getOnlineMembers()) {
                    Lang.COMMAND_PROMOTE_PROMOTED.sendMessage(member, promoter, "rank", newRank.getColor() + newRank.getName(), "promoted", promoting.getName());
                }
                return true;
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (!context.assertPlayer()) {
            if (!context.assertHasGuild()) {
                if (context.argsLengthEquals(1)) {
                    return TabCompleteManager.getGuildPlayers(context.getGuild(), context.arg(0), (p) -> p != context.getSender());
                }
            } else return Collections.singletonList(Lang.NO_GUILD_DEFAULT.parse(context.senderAsPlayer()));
        }
        return new ArrayList<>();
    }
}
