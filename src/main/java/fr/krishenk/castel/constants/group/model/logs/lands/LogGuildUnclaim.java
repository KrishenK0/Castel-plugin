package fr.krishenk.castel.constants.group.model.logs.lands;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.namespace.Namespace;

import java.util.Set;
import java.util.UUID;

public class LogGuildUnclaim extends LogGuildLand {
    private static final Namespace NS = Namespace.castel("GUILD_UNCLAIM");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildUnclaim();
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

    protected LogGuildUnclaim() {
    }

    public LogGuildUnclaim(UUID player, Set<SimpleChunkLocation> lands) {
        super(player, lands);
    }
}
