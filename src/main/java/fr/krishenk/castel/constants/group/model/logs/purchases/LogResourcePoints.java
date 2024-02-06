package fr.krishenk.castel.constants.group.model.logs.purchases;

import fr.krishenk.castel.constants.group.model.logs.misc.LogPlayerOperator;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;
import java.util.UUID;

public abstract class LogResourcePoints extends LogPlayerOperator {
    private long resourcePoints;
    protected LogResourcePoints() {}

    public LogResourcePoints(UUID player, long resourcePoints) {
        super(player);
        this.resourcePoints = resourcePoints;
    }

    public long getResourcePoints() {
        return resourcePoints;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        this.resourcePoints = context.getDataProvider().getLong("resourcePoints");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        context.getDataProvider().setLong("resourcePoints", this.resourcePoints);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.raw("resource_points", this.resourcePoints);
    }
}
