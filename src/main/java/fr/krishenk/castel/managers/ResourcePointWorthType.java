package fr.krishenk.castel.managers;

import fr.krishenk.castel.utils.internal.enumeration.QuickEnumSet;

import java.util.Collections;
import java.util.Objects;

public enum ResourcePointWorthType {
    NORMAL,
    SPECIAL,
    INJECTED;

    public static QuickEnumSet<ResourcePointWorthType> with(ResourcePointWorthType ... types) {
        Objects.requireNonNull(types);
        QuickEnumSet<ResourcePointWorthType> worthTypes = new QuickEnumSet<>(ResourcePointWorthType.values());
        Collections.addAll(worthTypes, types);
        return worthTypes;
    }
}
