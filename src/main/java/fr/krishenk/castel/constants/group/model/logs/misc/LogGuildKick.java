package fr.krishenk.castel.constants.group.model.logs.misc;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.events.members.LeaveReason;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.UUID;

public class LogGuildKick extends LogGuildLeave {
    private UUID kicker;
    private static final Namespace NS = Namespace.castel("GUILD_KICK");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildKick();
        }

        @Override
        public Namespace getNamespace() {
            return NS;
        }
    };

    @Override
    public AuditLogProvider getProvider() {
        return super.getProvider();
    }

    protected LogGuildKick() {
    }

    public LogGuildKick(UUID player, LeaveReason reason, UUID kicker) {
        super(player, reason);
        this.kicker = kicker;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        this.kicker = context.getDataProvider().get("kicker").asUUID();
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        context.getDataProvider().setUUID("kicker", this.kicker);
    }

    public OfflinePlayer getKicker() {
        return Bukkit.getOfflinePlayer(this.kicker);
    }
}
