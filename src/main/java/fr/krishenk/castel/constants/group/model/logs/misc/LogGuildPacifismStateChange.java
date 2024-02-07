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

public class LogGuildPacifismStateChange extends AuditLog {
    private UUID player;
    private boolean pacifist;
    private static final Namespace NS = Namespace.castel("GUILD_PACIFISM_CHANGE");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildPacifismStateChange();
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

    public LogGuildPacifismStateChange() {
    }

    public LogGuildPacifismStateChange(UUID player, boolean pacifist) {
        this.player = player;
        this.pacifist = pacifist;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        this.player = context.getDataProvider().get("player").asUUID();
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        context.getDataProvider().setUUID("player", this.player);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.withContext(this.getPlayer());
        builder.raw("pacifist", this.pacifist);
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(this.player);
    }
}
