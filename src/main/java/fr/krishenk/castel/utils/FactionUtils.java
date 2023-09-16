package fr.krishenk.castel.utils;

import com.massivecraft.factions.FPlayer;
import com.massivecraft.factions.Faction;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class FactionUtils {

    public static List<String> onlinePlayer(Faction faction) {
        List<String> listOnline = new ArrayList<>();
        for (FPlayer fPlayer : faction.getFPlayers()) {
            if (fPlayer.isOnline())
                listOnline.add(fPlayer.getNameAndTitle());
        }
        return listOnline;
    }

    public static List<String> offlinePlayer(Faction faction) {
        List<String> listOffline = new ArrayList<>();
        for (FPlayer fPlayer : faction.getFPlayers()) {
            if (fPlayer.isOffline())
                listOffline.add(fPlayer.getNameAndTitle());
        }
        return listOffline;
    }

    public static String getLeaderName(Faction faction) {
        FPlayer leader = faction.getFPlayerLeader();
        return leader == null ? ChatColor.RED + "System" : leader.getName();
    }

    public static String getLeaderId(Faction faction) {
        FPlayer leader = faction.getFPlayerLeader();
        return leader == null ? "0" : leader.getId();
    }
}
