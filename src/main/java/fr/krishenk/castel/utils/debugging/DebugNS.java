package fr.krishenk.castel.utils.debugging;

import java.util.Locale;

public interface DebugNS {
    String namespace();

    static DebugNS fromString(String ns) {
        return CastelDebug.Data.NAMES.get(ns.toUpperCase(Locale.ENGLISH));
    }
}
