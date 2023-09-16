package fr.krishenk.castel.utils.debugging;

import java.util.LinkedHashSet;
import java.util.Set;

public class StacktraceSettings {
    public final StacktraceSettings INSTANCE = new StacktraceSettings();
    public static boolean isWhitelist = true;
    public static Set<DebugNS> list = new LinkedHashSet<>();

    private StacktraceSettings() {}

}
