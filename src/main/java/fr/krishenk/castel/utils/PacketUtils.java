package fr.krishenk.castel.utils;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.GuildInvite;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.*;

public class PacketUtils {

    public static class Server {
        public static List<String> onlinePlayer() {
            List<String> list = new ArrayList<>();
            for (OfflinePlayer offlinePlayer : CastelPlugin.getInstance().getServer().getOfflinePlayers()) {
                list.add(offlinePlayer.getName());
            }
            return list;
        }
    }

    public static class GUILD {
        public static List<String> onlinePlayer(Guild guild) {
            List<String> listOnline = new ArrayList<>();
            for (Player player : guild.getOnlineMembers()) {
                listOnline.add(player.getName());
            }
            return listOnline;
        }

        public static List<String> offlinePlayer(Guild guild) {
            List<String> listOffline = new ArrayList<>();
            for (CastelPlayer cPlayer : guild.getCastelPlayers()) {
                if (!cPlayer.getOfflinePlayer().isOnline())
                    listOffline.add(cPlayer.getOfflinePlayer().getName());
            }
            return listOffline;
        }

        public static String getLeaderName(Guild guild) {
            CastelPlayer leader = guild.getLeader();
            return leader == null ? ChatColor.RED + "System" : leader.getOfflinePlayer().getName();
        }

        public static Map<String, String> getInvitationSent(Guild guild) {
            Map<String, String> invites = new HashMap<>();
            for (OfflinePlayer offlinePlayer : Bukkit.getOfflinePlayers()) {
                CastelPlayer cp = CastelPlayer.getCastelPlayer(offlinePlayer);
                if (!cp.getInvites().isEmpty()) {
                    for (Map.Entry<UUID, GuildInvite> entry : cp.getInvites().entrySet()) {
                        CastelPlayer sender = entry.getValue().getCastelPlayer();
                        if (guild.isMember(sender))
                            invites.put(cp.getOfflinePlayer().getName(), sender.getOfflinePlayer().getName());
                    }
                }
            }
            return invites;
        }

    }

}
