package fr.krishenk.castel.managers.inviterequests;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.metadata.CastelMetadata;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.general.GuildDisbandEvent;
import fr.krishenk.castel.events.members.GuildJoinEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.*;

public class JoinRequests implements Listener {
    public static final Namespace PLAYER_NAMESPACE = new Namespace("JoinRequests", "PLAYER");
    public static final Namespace GUILD_NAMESPACE = new Namespace("JoinRequests", "GUILD");

    public JoinRequests() {}

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public final void onJoinGuild(GuildJoinEvent event) {
        CastelPlayer cp = event.getPlayer();
        Set<UUID> requests = getJoinRequests(cp);
        for (UUID joinRequest : requests) {
            Guild guild = Guild.getGuild(joinRequest);
            if (guild != null) requests.remove(cp.getUUID());
        }
        requests.clear();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onGuildDisband(GuildDisbandEvent event) {
        Objects.requireNonNull(event, "event must not be null");
        Guild guild = event.getGuild();
        Objects.requireNonNull(guild, "kingdom must not be null");
        Set<UUID> joinRequests = getJoinRequests(guild).keySet();

        List<CastelPlayer> players = new ArrayList<>(joinRequests.size());
        for (UUID playerId : joinRequests) {
            CastelPlayer player = CastelPlayer.getCastelPlayer(playerId);
            players.add(player);
        }

        for (CastelPlayer player : players) {
            Objects.requireNonNull(player, "player must not be null");
            getJoinRequests(player).remove(guild.getDataKey());
        }
    }

    public static void registerMetaHandlers() {
        CastelPlugin.getInstance().getMetadataRegistry().register(GuildJoinRequestsHandler.INSTANCE);
        CastelPlugin.getInstance().getMetadataRegistry().register(PlayerJoinRequestsHandler.INSTANCE);
    }

    public static Set<UUID> getJoinRequests(CastelPlayer player) {
        CastelMetadata data = player.getMetadata().get(PlayerJoinRequestsHandler.INSTANCE);
        if (data == null) {
            data = new PlayerJoinRequestsMeta(new HashSet<>());
            player.getMetadata().put(PlayerJoinRequestsHandler.INSTANCE, data);
        }
        return (Set<UUID>) data.getValue();
    }

    public static Map<UUID, Long> getJoinRequests(Guild guild) {
        CastelMetadata data = guild.getMetadata().get(GuildJoinRequestsHandler.INSTANCE);
        if (data == null) {
            data = new GuildJoinRequestsMeta(new HashMap<>());
            guild.getMetadata().put(GuildJoinRequestsHandler.INSTANCE, data);
        }
        return (Map<UUID, Long>) data.getValue();
    }

    public static void sendJoinRequestTo(CastelPlayer player, Guild guild) {
        Set<UUID> playerRequests = getJoinRequests(player);
        playerRequests.add(guild.getDataKey());
        Map<UUID, Long> guildRequests = getJoinRequests(guild);
        guildRequests.put(guild.getId(), System.currentTimeMillis());
    }

    public void processJoinRequest(Guild guild, CastelPlayer player, boolean accepted, CastelPlayer by) {
        getJoinRequests(guild).remove(player.getUUID());
        getJoinRequests(player).remove(guild.getDataKey());
        if (accepted)
            player.joinGuild(guild, (event) -> event.getMetadata().put(JoinRequests.GUILD_NAMESPACE, by.getUUID()));
    }
}
