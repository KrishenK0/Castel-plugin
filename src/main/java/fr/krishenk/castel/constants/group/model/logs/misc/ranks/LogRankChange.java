package fr.krishenk.castel.constants.group.model.logs.misc.ranks;

import fr.krishenk.castel.constants.group.model.logs.misc.LogPlayerOperator;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;

public abstract class LogRankChange extends LogPlayerOperator {
    private String node;
    private String rank;

    protected LogRankChange() {
    }

    public LogRankChange(CastelPlayer player, Rank rank) {
        super(player.getUUID());
        this.node = rank.getNode();
        this.rank = rank.getColor() + rank.getSymbol() + ' ' + rank.getName();
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.rank = json.getString("rank");
        this.node = json.getString("node");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setString("rank", this.rank);
        json.setString("node", this.node);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.raw("node", this.node);
        builder.parse("rank", this.rank);
    }
}
