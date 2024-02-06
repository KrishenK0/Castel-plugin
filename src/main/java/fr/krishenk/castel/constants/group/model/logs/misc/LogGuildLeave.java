package fr.krishenk.castel.constants.group.model.logs.misc;

import com.google.common.base.Enums;
import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;
import java.util.UUID;

public class LogGuildLeave extends LogPlayerOperator {

    private LeaveReason reason;
    private static final Namespace NS = Namespace.castel("GUILD_LEAVE");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildLeave();
        }

        @Override
        public Namespace getNamespace() {
            return NS;
        }
    };

    protected LogGuildLeave() {
    }

    @Override
    public AuditLogProvider getProvider() {
        return PROVIDER;
    }

    public LogGuildLeave(UUID player, LeaveReason reason) {
        super(player);
        this.reason = reason;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.reason = Enums.getIfPresent(LeaveReason.class, json.getString("reason")).orNull();
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        context.getDataProvider().setString("reason", this.reason.name());
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.raw("reason", this.reason.name());
    }

    public LeaveReason getReason() {
        return reason;
    }
}
