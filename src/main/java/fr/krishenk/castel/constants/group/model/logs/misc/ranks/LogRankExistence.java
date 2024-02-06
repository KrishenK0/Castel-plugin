package fr.krishenk.castel.constants.group.model.logs.misc.ranks;

import fr.krishenk.castel.constants.group.model.logs.misc.LogPlayerOperator;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.player.Rank;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.data.handlers.DataHandlerRank;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.managers.structures.RankEditor;

import java.sql.SQLException;
import java.util.UUID;

public abstract class LogRankExistence extends LogPlayerOperator {
    private Rank rank;

    protected LogRankExistence() {
    }

    public LogRankExistence(UUID player, Rank rank) {
        super(player);
        this.rank = rank;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.rank = DataHandlerRank.deserializeRank(json.get("rank").asSection().getString("node"), json.get("rank").asSection());
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter section = context.getDataProvider().createSection("rank");
        DataHandlerRank.serializeRank(this.rank, section);
        section.setString("node", rank.getNode());
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.inheritPlaceholders(RankEditor.getRankEdits(this.rank));
    }
}
