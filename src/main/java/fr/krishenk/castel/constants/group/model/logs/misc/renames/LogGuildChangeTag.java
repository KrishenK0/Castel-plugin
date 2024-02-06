package fr.krishenk.castel.constants.group.model.logs.misc.renames;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.namespace.Namespace;

import java.util.UUID;

public class LogGuildChangeTag extends LogGuildChangeString {
    private static final Namespace NS = Namespace.castel("GUILD_CHANGE_TAG");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider(){

        @Override
        public AuditLog construct() {
            return new LogGuildChangeTag();
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

    protected LogGuildChangeTag() {
    }

    public LogGuildChangeTag(UUID player, String old, String newStr) {
        super(player, old, newStr);
    }
}
