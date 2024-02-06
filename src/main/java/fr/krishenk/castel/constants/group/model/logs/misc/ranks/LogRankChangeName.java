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

public class LogRankChangeName extends LogRankChange {
    private String newName;
    private String oldName;
    public static final Namespace NS = Namespace.castel("RANK_CHANGE_NAME");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogRankChangeName();
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

    protected LogRankChangeName() {
    }

    public LogRankChangeName(CastelPlayer player, Rank rank, String newName) {
        super(player, rank);
        this.oldName = rank.getName();
        this.newName = newName;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.oldName = json.getString("oldName");
        this.newName = json.getString("newName");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setString("oldName", this.oldName);
        json.setString("newName", this.newName);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.parse("old-name", this.oldName);
        builder.parse("new-name", this.newName);
    }
}
