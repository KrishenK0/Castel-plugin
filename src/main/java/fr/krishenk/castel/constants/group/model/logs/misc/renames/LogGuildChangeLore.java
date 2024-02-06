package fr.krishenk.castel.constants.group.model.logs.misc.renames;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.namespace.Namespace;

import java.util.UUID;

public class LogGuildChangeLore extends LogGuildChangeString {
    private static final Namespace NS = Namespace.castel("GUILD_CHANGE_LORE");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider(){

        @Override
        public AuditLog construct() {
            return new LogGuildChangeLore();
        }

        @Override
        public Namespace getNamespace() {
            return NS;
        }
    };

    @Override
    public AuditLogProvider getProvider() {
        return PROVIDER;
    }

    protected LogGuildChangeLore() {
    }

    public LogGuildChangeLore(UUID player, String old, String newStr) {
        super(player, old, newStr);
    }
}
