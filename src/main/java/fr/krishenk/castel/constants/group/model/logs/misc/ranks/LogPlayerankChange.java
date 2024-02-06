package fr.krishenk.castel.constants.group.model.logs.misc.ranks;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.group.model.logs.misc.LogPlayerOperator;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.Bukkit;

import java.sql.SQLException;
import java.util.UUID;

public class LogPlayerankChange extends LogPlayerOperator {
    private String oldRankNode;
    private String newRankNode;
    private int oldRankPriority;
    private int newRankPriority;
    private String oldRank;
    private String newRank;
    private UUID byPlayer;
    private static final Namespace NS = Namespace.castel("PLAYER_RANK_CHANGE");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogPlayerankChange();
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

    protected LogPlayerankChange() {
    }

    public LogPlayerankChange(CastelPlayer cp, Rank oldRank, Rank newRank, UUID byPlayer) {
        super(cp.getUUID());
        this.byPlayer = byPlayer;
        this.oldRank = oldRank.getColor() + oldRank.getSymbol() + ' ' + oldRank.getName();
        this.newRank = newRank.getColor() + newRank.getSymbol() + ' ' + newRank.getName();
        this.oldRankNode = oldRank.getNode();
        this.newRankNode = newRank.getNode();
        this.oldRankPriority = oldRank.getPriority();
        this.newRankPriority = newRank.getPriority();
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.byPlayer = json.get("byPlayer").asUUID();
        this.oldRankNode = json.getString("oldRankNode");
        this.newRankNode = json.getString("newRankNode");
        this.oldRank = json.getString("oldRank");
        this.newRank = json.getString("newRank");
        this.oldRankPriority = json.getInt("oldRankPriority");
        this.newRankPriority = json.getInt("newRankPriority");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setUUID("byPlayer", this.byPlayer);
        json.setString("oldRankNode", this.oldRankNode);
        json.setString("newRankNode", this.newRankNode);
        json.setString("oldRank", this.oldRank);
        json.setString("newRank", this.newRank);
        json.setInt("oldRankPriority", this.oldRankPriority);
        json.setInt("newRankPriority",this.newRankPriority);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        if (this.byPlayer != null) builder.raw("by-player", Bukkit.getOfflinePlayer(this.byPlayer).getName());
        else builder.raw("by-player", Lang.UNKNOWN);
        builder.raw("old-rank-node", this.oldRankNode);
        builder.raw("new-rank-rode", this.newRankNode);
        builder.raw("old-rank", this.oldRank);
        builder.raw("new-rank", this.newRank);
        builder.raw("old_rank_priority", this.oldRankPriority);
        builder.raw("new_rank_priority",this.newRankPriority);
    }
}
