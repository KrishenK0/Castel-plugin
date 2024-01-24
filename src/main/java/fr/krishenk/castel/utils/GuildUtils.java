package fr.krishenk.castel.utils;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GuildUtils {

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
}
