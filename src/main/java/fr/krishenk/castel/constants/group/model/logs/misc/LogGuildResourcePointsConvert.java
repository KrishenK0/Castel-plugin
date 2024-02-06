package fr.krishenk.castel.constants.group.model.logs.misc;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.internal.FastUUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.sql.SQLException;
import java.util.UUID;

public class LogGuildResourcePointsConvert extends AuditLog {
    private UUID player;
    private long amountBefore;
    private long amountAfter;
    private static final Namespace NS = Namespace.castel("GUILD_RESOURCE_POINTS_CONVERT");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildResourcePointsConvert();
        }

        @Override
        public Namespace getNamespace() {
            return NS;
        }
    };

    @Override
    public AuditLogProvider getProvider() {
        return null;
    }

    protected LogGuildResourcePointsConvert() {
    }

    public LogGuildResourcePointsConvert(UUID player, long amountBefore, long amountAfter) {
        this.player = player;
        this.amountBefore = amountBefore;
        this.amountAfter = amountAfter;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.player = FastUUID.fromString(json.getString("player"));
        this.amountBefore = json.getLong("amountBefore");
        this.amountAfter = json.getLong("amountAfter");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setUUID("player", this.player);
        json.setLong("amountBefore", this.amountBefore);
        json.setLong("amountAfter", this.amountAfter);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.withContext(this.getPlayer());
        builder.raw("amount-before", this.amountBefore);
        builder.raw("amount-after", this.amountAfter);
    }

    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(player);
    }
}
