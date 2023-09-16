package fr.krishenk.castel.constants.group.upgradable;

import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;

public interface Upgrade {
    double getUpgradeCost(PlaceholderContextProvider provider);

    int getMaxLevel(PlaceholderContextProvider provider);

    Messenger getDisplayName();

    String getDataName();

    int getDefaultLevel(PlaceholderContextProvider provider);
}
