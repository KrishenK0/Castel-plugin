package fr.krishenk.castel.constants.group.model.logs.misc.ranks;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;

public class LogRankChangePriority extends LogRankChange {
    private int newPriority;
    private int oldPriority;
    public static final Namespace NS = Namespace.castel("RANK_CHANGE_PRIORITY");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogRankChangePriority();
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

    protected LogRankChangePriority() {
    }

    public LogRankChangePriority(CastelPlayer player, Rank rank, int newPriority) {
        super(player, rank);
        this.oldPriority = rank.getPriority();
        this.newPriority = newPriority;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.oldPriority = json.getInt("oldPriority");
        this.newPriority = json.getInt("newPriority");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setInt("oldPriority", this.oldPriority);
        json.setInt("newPriority", this.newPriority);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.parse("old-priority", this.oldPriority);
        builder.parse("new-priority", this.newPriority);
    }
}
