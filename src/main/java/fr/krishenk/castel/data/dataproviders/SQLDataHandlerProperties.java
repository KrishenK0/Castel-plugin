package fr.krishenk.castel.data.dataproviders;

import org.apache.commons.lang.ArrayUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class SQLDataHandlerProperties {
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

    public static String[] ofLocation(String prefix) {
        Object[] views = new String[]{prefix + "_yaw", prefix + "_pitch"};
        return (String[]) ArrayUtils.addAll(ofSimpleLocation(prefix), views);
    }

    public static String[] ofSimpleLocation(String prefix) {
        return new String[]{prefix + "_world", prefix + "_x", prefix + "_y", prefix + "_z"};
    }
}
