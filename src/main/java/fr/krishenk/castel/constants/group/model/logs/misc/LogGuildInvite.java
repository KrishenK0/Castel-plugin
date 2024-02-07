package fr.krishenk.castel.constants.group.model.logs.misc;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.time.TimeFormatter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.UUID;

public class LogGuildInvite extends LogPlayerOperator {
    private UUID invitedBy;
    private long acceptTime;
    private static final Namespace NS = Namespace.castel("GUILD_INVITE");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildInvite();
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

    protected LogGuildInvite() {
    }

    public LogGuildInvite(UUID player, UUID invitedBy, long acceptTime) {
        super(player);
        this.invitedBy = invitedBy;
        this.acceptTime = acceptTime;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.invitedBy = json.get("invitedBy").asUUID();
        this.acceptTime = json.getLong("acceptTime");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setUUID("invitedBy", this.invitedBy);
        json.setLong("acceptTime", this.acceptTime);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.parse("inviter", this.getInviter());
        builder.raw("accept-time", TimeFormatter.of(this.acceptTime));
    }

    public OfflinePlayer getInviter() {
        return this.invitedBy == null ? null : Bukkit.getOfflinePlayer(this.invitedBy);
    }
}
