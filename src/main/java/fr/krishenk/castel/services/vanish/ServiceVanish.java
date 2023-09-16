package fr.krishenk.castel.services.vanish;

import fr.krishenk.castel.services.Service;
import org.bukkit.entity.Player;

public interface ServiceVanish extends Service {
    boolean isVanished(Player p);

    boolean isInGodMode(Player p);
}
