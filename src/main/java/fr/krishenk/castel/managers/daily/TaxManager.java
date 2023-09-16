package fr.krishenk.castel.managers.daily;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.lang.Config;

import java.util.concurrent.TimeUnit;

public class TaxManager {
    public TaxManager() {}

    public static boolean needsToPayTaxes(Guild guild) {
        if (guild.isPermanent()) return false;
        Long guildsTaxAge = Config.TAX_GUILDS_AGE.getTimeMillis(TimeUnit.DAYS);
        if (guildsTaxAge == null) return true;
        return System.currentTimeMillis() - guild.getSince() > guildsTaxAge;
    }

//    public static void handleNationTaxes()
}
