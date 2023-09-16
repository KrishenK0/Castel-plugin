package fr.krishenk.castel.constants.group.upgradable;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;

import java.util.Map;

public interface GuildUpgrade extends Upgrade {
    double getScaling(PlaceholderContextProvider provider);

    static <T extends GuildUpgrade> Map<T, Integer> getDefaults(T[] VALUES) {
        Map<T, Integer> upgrades = new NonNullMap<>(VALUES.length);

        for (T upgrade : VALUES) {
            int defLvl = upgrade.getDefaultLevel(PlaceholderContextProvider.EMPTY);
            if (defLvl > 0) upgrades.put(upgrade, defLvl);
        }
        return upgrades;
    }

    boolean isEnabled(PlaceholderContextProvider provider);

    default double getScaling(Guild guild) {
        int lvl = guild.getUpgradeLevel(this);
        if (lvl < 0) lvl = 0;
        return this.getScaling(new PlaceholderContextBuilder().withContext(guild).raw("lvl", lvl));
    }
}
