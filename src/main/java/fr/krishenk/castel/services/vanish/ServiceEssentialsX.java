package fr.krishenk.castel.services.vanish;

import com.earth2me.essentials.Essentials;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ServiceEssentialsX implements ServiceVanish {
    private static final Essentials ESSENTIALS = (Essentials) Bukkit.getPluginManager().getPlugin("Essentials");

    @Override
    public boolean isVanished(Player p) {
        return ESSENTIALS.getUser(p).isVanished();
    }

    @Override
    public boolean isInGodMode(Player p) {
        return ESSENTIALS.getUser(p).isGodModeEnabled();
    }
}
