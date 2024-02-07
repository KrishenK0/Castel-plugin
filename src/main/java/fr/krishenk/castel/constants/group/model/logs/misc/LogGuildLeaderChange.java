package fr.krishenk.castel.constants.group.model.logs.misc;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.events.general.GuildLeaderChangeEvent;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class LogGuildLeaderChange extends AuditLog {
    private UUID oldLeader;
    private UUID newLeader;
    private GuildLeaderChangeEvent.Reason reason;
    private static final Namespace NS = Namespace.castel("GUILD_LEADER_CHANGE");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildLeaderChange();
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

    protected LogGuildLeaderChange() {
    }

    public LogGuildLeaderChange(UUID oldLeader, UUID newLeader, GuildLeaderChangeEvent.Reason reason) {
        this.oldLeader = oldLeader;
        this.newLeader = newLeader;
        this.reason = reason;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.oldLeader = json.get("oldLeader").asUUID();
        this.newLeader = json.get("newLeader").asUUID();
        this.reason = GuildLeaderChangeEvent.Reason.valueOf(json.getString("reason"));
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setUUID("oldLeader", this.oldLeader);
        json.setUUID("newLeader", this.newLeader);
        json.setString("reason", this.reason.name());
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.raw("old-leader", Bukkit.getOfflinePlayer(this.oldLeader).getName());
        builder.raw("new-leader", Bukkit.getOfflinePlayer(this.newLeader).getName());
        builder.raw("reason", StringUtils.capitalize(this.reason.name()));
    }
}
