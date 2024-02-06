package fr.krishenk.castel.constants.group.model.logs.misc.ranks;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.Rank;

import java.util.UUID;

public class LogRankDelete extends LogRankExistence {
    public static final Namespace NS = Namespace.castel("RANK_DELETE");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogRankDelete();
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

    protected LogRankDelete() {
    }

    public LogRankDelete(UUID player, Rank rank) {
        super(player, rank);
    }
}
