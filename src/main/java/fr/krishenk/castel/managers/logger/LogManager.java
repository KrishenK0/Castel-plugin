package fr.krishenk.castel.managers.logger;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.logs.lands.LogGuildClaim;
import fr.krishenk.castel.constants.group.model.logs.lands.LogGuildUnclaim;
import fr.krishenk.castel.constants.group.model.logs.misc.*;
import fr.krishenk.castel.constants.group.model.logs.misc.bank.LogGuildBankChange;
import fr.krishenk.castel.constants.group.model.logs.misc.ranks.*;
import fr.krishenk.castel.constants.group.model.logs.misc.renames.LogGuildChangeLore;
import fr.krishenk.castel.constants.group.model.logs.misc.renames.LogGuildChangeTag;
import fr.krishenk.castel.constants.group.model.logs.misc.renames.LogGuildRename;
import fr.krishenk.castel.constants.group.model.logs.relations.LogGuildRelationshipChangeEvent;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.GuildInvite;
import fr.krishenk.castel.events.general.*;
import fr.krishenk.castel.events.general.ranks.*;
import fr.krishenk.castel.events.lands.ClaimLandEvent;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.events.members.GuildJoinEvent;
import fr.krishenk.castel.events.members.GuildLeaveEvent;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class LogManager implements Listener {
    private static final Map<UUID, Map<Class<?>, Map<Object, BulkLogCollector>>> BULK_COLLECTOR = new HashMap<>();
    public static final long COLLECTOR_TIME_LIMIT = TimeUtils.millisToTicks(Duration.ofSeconds(5L).toMillis());

    @EventHandler
    public void onGuildCreate(GuildCreateEvent event) {
        CastelLogger.getMain().log("A guild named " + event.getGuild().getName() + " has been created for " + event.getGuild().getLeader().getOfflinePlayer().getName());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGuildDisband(GuildDisbandEvent event) {
        CastelLogger.getMain().log(event.getGuild().getName() + " has been disbanded for " + event.getReason().name());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGuildMemberJoin(GuildJoinEvent event) {
        OfflinePlayer player = event.getPlayer().getOfflinePlayer();
        GuildInvite invite = event.getMetadata(GuildInvite.NAMESPACE);
        event.getGuild().log(new LogGuildJoin(player.getUniqueId(), invite == null ? null : invite.getSender()));
        CastelLogger.getMain().log(player.getName() + " has joined " + event.getGuild().getName() + " guild.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGuildMemberLeave(GuildLeaveEvent event) {
        if (event.getReason() == LeaveReason.KICKED) return;
        OfflinePlayer player = event.getPlayer().getOfflinePlayer();
        Guild guild = event.getPlayer().getGuild();
        guild.log(new LogGuildLeave(player.getUniqueId(), event.getReason()));
        CastelLogger.getMain().log(player.getName() + " has left " + guild.getName() + " guild.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onClaimEvent(ClaimLandEvent event) {
        UUID player = event.getPlayer() == null ? null : event.getPlayer().getUUID();
        Guild guild = event.getGuild();
        guild.log(new LogGuildClaim(player, event.getLandLocations()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onUnclaimEvent(UnclaimLandEvent event) {
        UUID player = event.getPlayer() == null ? null : event.getPlayer().getUUID();
        Guild guild = event.getGuild();
        guild.log(new LogGuildUnclaim(player, event.getLandLocations()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRpDonate(GroupResourcePointConvertEvent event) {
        Group group = event.getGroup();
        if (!(group instanceof Guild)) return;
        group.log(new LogGuildResourcePointsConvert(event.getCastelPlayer().getUUID(), group.getResourcePoints(), group.getResourcePoints() + event.getAmount()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGuildRename(GroupRenameEvent event) {
        Group group = event.getGroup();
        if (!(group instanceof  Guild)) return;
        group.log(new LogGuildRename(event.getPlayer().getUUID(), event.getOldName(), event.getNewName()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLoreChange(GuildLoreChangeEvent event) {
        Group group = event.getGroup();
        if (!(group instanceof Guild)) return;
        group.log(new LogGuildChangeLore(event.getPlayer().getUUID(), event.getOldLore(), event.getNewLore()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLoreChange(GroupRenameTagEvent event) {
        Group group = event.getGroup();
        if (!(group instanceof Guild)) return;
        group.log(new LogGuildChangeTag(event.getPlayer().getUUID(), event.getOldTag(), event.getNewTag()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPacifismStateChange(GuildPacifismStateChangeEvent event) {
        Guild guild = event.getGuild();
        CastelPlayer cp = event.getPlayer();
        if (cp == null) return;
        guild.log(new LogGuildPacifismStateChange(guild.getId(), event.isPacifist()));
        CastelLogger.getMain().log(cp.getOfflinePlayer().getName() + " has changed their guild " + guild.getName() + " pacifism state: " + guild.isPacifist());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRelationChange(GroupRelationshipChangeEvent event) {
        if (event.getFirst() instanceof Guild) {
            Guild first = (Guild) event.getFirst();
            Guild second = (Guild) event.getSecond();
            first.log(new LogGuildRelationshipChangeEvent(second.getId(), event.getOldRelation(), event.getNewRelation()));
            second.log(new LogGuildRelationshipChangeEvent(first.getId(), event.getOldRelation(), event.getNewRelation()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onLeaderChange(GuildLeaderChangeEvent event) {
        event.getGuild().log(new LogGuildLeaderChange(event.getOldLeader().getUUID(), event.getNewLeader().getUUID(), event.getReason()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankChange(PlayerRankChangeEvent event) {
        Group group = event.getGroup();
        CastelPlayer cp = event.getByPlayer();
        if (cp == null) return;
        group.log(new LogPlayerankChange(event.getPlayer(), event.getOldRank(), event.getRank(), cp.getUUID()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInvite(GuildInviteEvent event) {
        event.getGuild().log(new LogGuildInvite(event.getPlayer().getUUID(), event.getInviter().getUUID(), event.getAcceptTime()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankCreate(RankCreateEvent event) {
        event.getGroup().log(new LogRankCreate(event.getPlayer().getUUID(), event.getRank()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankDelete(RankDeleteEvent event) {
        event.getGroup().log(new LogRankDelete(event.getPlayer().getUUID(), event.getRank()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankNameChange(RankNameChangeEvent event) {
        event.getGroup().log(new LogRankChangeName(event.getPlayer(), event.getRank(), event.getNewName()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankSymbolChange(RankSymbolChangeEvent event) {
        event.getGroup().log(new LogRankChangeSymbol(event.getPlayer(), event.getRank(), event.getNewSymbol()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankColorChange(RankColorChangeEvent event) {
        event.getGroup().log(new LogRankChangeColor(event.getPlayer(), event.getRank(), event.getNewColor()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankPriorityChange(RankPriorityChangeEvent event) {
        event.getGroup().log(new LogRankChangePriority(event.getPlayer(), event.getRank(), event.getNewPriority()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankMaxClaimsChange(RankMaxClaimsChangeEvent event) {
        event.getGroup().log(new LogRankChangeMaxClaims(event.getPlayer(), event.getRank(), event.getNewMaxClaims()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRankMaterialChange(RankMaterialChangeEvent event) {
        event.getGroup().log(new LogRankChangeMaterial(event.getPlayer(), event.getRank(), event.getNewMaterial()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onServerTax(GroupServerTaxPayEvent event) {
        Group group = event.getGroup();
        if (!(group instanceof Guild)) return;
        group.log(new LogGroupServerTaxPay(event.getAmount()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onGroupBankChange(GuildBankChangeEvent event) {
        Group group = event.getGroup();
        CastelPlayer cp = event.getPlayer();
        if (!(group instanceof Guild) || cp == null) return;
        System.out.println(event.getOldBank());
        System.out.println(event.getNewBank());
        group.log(new LogGuildBankChange(cp.getUUID(), event.getOldBank(), event.getNewBank()));
    }
}
