package fr.krishenk.castel.constants.group.model.logs.misc;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.UUID;

public class LogGuildJoin extends LogPlayerOperator {
    private UUID invitedBy;
    private static final Namespace NS = Namespace.castel("GUILD_JOIN");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildJoin();
        }

        @Override
        public Namespace getNamespace() {
            return NS;
        }
    };

    protected LogGuildJoin() {
    }

    @Override
    public AuditLogProvider getProvider() {
        return PROVIDER;
    }

    public LogGuildJoin(UUID player, UUID invitedBy) {
        super(player);
        this.invitedBy = invitedBy;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        this.invitedBy = context.getDataProvider().get("invitedBy").asUUID();
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        context.getDataProvider().setUUID("invitedBy", this.invitedBy);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        OfflinePlayer inviter = this.getInviter();
        builder.parse("inviter", inviter == null ? "&cNot invited" : inviter.getName());
    }

    public OfflinePlayer getInviter() {
        return this.invitedBy == null ? null : Bukkit.getOfflinePlayer(this.invitedBy);
    }
}
