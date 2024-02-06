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

public class LogRankChangeMaxClaims extends LogRankChange {
    private int newMaxClaims;
    private int oldMaxClaims;
    public static final Namespace NS = Namespace.castel("RANK_CHANGE_MAX_CLAIMS");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogRankChangeMaxClaims();
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

    protected LogRankChangeMaxClaims() {
    }

    public LogRankChangeMaxClaims(CastelPlayer player, Rank rank, int newMaxClaims) {
        super(player, rank);
        this.oldMaxClaims = rank.getMaxClaims();
        this.newMaxClaims = newMaxClaims;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.oldMaxClaims = json.getInt("oldMaxClaims");
        this.newMaxClaims = json.getInt("newMaxClaims");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setInt("oldMaxClaims", this.oldMaxClaims);
        json.setInt("newMaxClaims", this.newMaxClaims);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.parse("old-max-claims", this.oldMaxClaims);
        builder.parse("new-max-claims", this.newMaxClaims);
    }
}
