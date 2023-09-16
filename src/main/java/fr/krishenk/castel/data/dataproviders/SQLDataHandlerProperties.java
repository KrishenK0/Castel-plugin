package fr.krishenk.castel.data.dataproviders;

import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

public class SQLDataHandlerProperties {
    public static final Companion Companion = new Companion();
    private final Map<String, Integer> associateNamedData;

    public SQLDataHandlerProperties(@NotNull String[] ignorableKeys) {
        Map<String, Integer> destination = new LinkedHashMap<>();
        for (int i = 0; i < ignorableKeys.length; i++) {
            destination.put(ignorableKeys[i], i+1);
        }
        this.associateNamedData = destination;
    }

    public Map<String, Integer> getAssociateNamedData() {
        return this.associateNamedData;
    }

    public static final String[] ofLocation(String prefix) {
        return Companion.ofLocation(prefix);
    }

    public static final String[] ofSimpleLocation(String prefix) {
        return Companion.ofSimpleLocation(prefix);
    }

    public static class Companion {
        public Companion() {}

        public final String[] ofLocation(String prefix) {
            Object[] views = new String[]{prefix + "_yaw", prefix + "_pitch"};
            return (String[]) ArrayUtils.addAll(this.ofSimpleLocation(prefix), views);
        }

        public final String[] ofSimpleLocation(@NotNull String prefix) {
            String[] strings = new String[]{prefix + "_world", prefix + "_x", prefix + "_y", prefix + "_z"};
            return strings;
        }
    }
}
