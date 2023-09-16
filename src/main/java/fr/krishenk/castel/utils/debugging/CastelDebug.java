package fr.krishenk.castel.utils.debugging;

import java.util.HashMap;
import java.util.Map;

public enum CastelDebug implements DebugNS {
    EXPLOSIONS,
    LANGUAGE_MISSING$ENTRIES,
    CHAT,
    GENERALIZED$EVENT$WATCHER,
    INVALID$REGEX,
    FALL$DAMAGE,
    PLACEHOLDER,
    COMMAND_TOP,
    DOWNLOAD,
    STRUCTURE_BREAK,
    SAVE_ALL,
    CHUNK$SNAPSHOT,
    LAND$VISUALIZERS,
    UNKNOWN$PLACEHOLDER;
    private final String namespace;

    CastelDebug() {
        this.namespace = this.name().replace('_', '/').replace('$', '-');
        Data.NAMES.put(this.namespace, this);
    }

    public static DebugNS register(String name) {
        DebugNS object = new DebugNS() {
            @Override
            public String namespace() {
                return name;
            }
        };
        Data.NAMES.put(name, object);
        return object;
    }

    @Override
    public String namespace() {
        return this.namespace;
    }

    public static class Data {
        protected static final Map<String, DebugNS> NAMES = new HashMap<>(10);
    }
}
