package fr.krishenk.castel.config;

import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

public class ConfigUtils {
    public static boolean isInDisabledWorld(NewKeyedConfigAccessor config, Player player, Lang lang) {
        String world = player.getWorld().getName();
        if (config.getStringList().contains(world)) {
            if (lang != null) lang.sendError(player, "world", world);
            return true;
        }
        return false;
    }
}
