package fr.krishenk.castel.managers.daily;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.events.general.GroupDisband;
import fr.krishenk.castel.events.general.GuildLeaderChangeEvent;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import fr.krishenk.castel.utils.ConditionProcessor;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

public class InactivityManager {
    public InactivityManager() {}

    protected static boolean disband(Guild guild) {
        ConditionalCompiler.LogicalOperand guildExcludeCond = Config.INACTIVITY_GUILD_EXCLUDE_CONDITION.getManager().getCondition();
        boolean result = ConditionProcessor.process(guildExcludeCond, (new PlaceholderContextBuilder()).withContext(guild));
        if (result) return false;
        AtomicBoolean disbanded = new AtomicBoolean(false);
        Bukkit.getScheduler().runTask(CastelPlugin.getInstance(), () -> {
            disbanded.set(!guild.disband(GroupDisband.Reason.INACTIVITY).isCancelled());
            if (disbanded.get() && Config.INACTIVITY_GUILD_ANNOUNCE.getBoolean()) {
                Lang.INACTIVITY_ANNOUNCE_GUILD.sendEveryoneMessage("guild", guild.getName());
            }
        });
        return disbanded.get();
    }

    protected static void handleInactiveMember(Guild guild, OfflinePlayer member) {
        ConditionalCompiler.LogicalOperand guildExcludeCond = Config.INACTIVITY_MEMBER_EXCLUDE_CONDITION.getManager().getCondition();
        boolean result = ConditionProcessor.process(guildExcludeCond, (new PlaceholderContextBuilder()).withContext(member));
        if(!result) {
            CastelPlayer highestAfterLeader;
            if (member.getUniqueId().equals(guild.getLeaderId())) {
                if (Config.INACTIVITY_MEMBER_DISBAND_GUILD_IF_LEADER.getBoolean()) {
                    disband(guild);
                } else if (guild.getMembers().size() > 1) {
                    highestAfterLeader = Rank.determineNextLeader((ArrayList<CastelPlayer>) guild.getCastelPlayers(), null);
                    if (guild.setLeader(highestAfterLeader, GuildLeaderChangeEvent.Reason.INACTIVITY).isCancelled()) return;
                } else disband(guild);
            }

            highestAfterLeader = CastelPlayer.getCastelPlayer(member);
            if (!highestAfterLeader.hasGuild()) {
                MessageHandler.sendConsolePluginMessage("&4Unknown guild for player &e" + member.getName() + " &4while kicking them due to inactivity &e" + guild.getName() + " &4guild. Removing them...");
                guild.getMembers().remove(member.getUniqueId());
            } else {
                CastelPlayer finalHighestAfterLeader = highestAfterLeader;
                Bukkit.getScheduler().runTask(CastelPlugin.getInstance(), () -> finalHighestAfterLeader.leaveGuild(LeaveReason.INACTIVITY));
                if (Config.INACTIVITY_MEMBER_ANNOUNCE.getBoolean())
                    Lang.INACTIVITY_ANNOUNCE_PLAYER.sendEveryoneMessage("player", member.getName());
            }
        }
    }
}
