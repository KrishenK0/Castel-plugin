package fr.krishenk.castel.constants.group.model.logs.purchases;

import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;
import java.util.UUID;

public abstract class LogGuildUpgrade extends LogResourcePoints {
    private int oldLevel;
    private int newLevel;

    public LogGuildUpgrade() {
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public LogGuildUpgrade(UUID player, long resourcePoints, int oldLevel, int newLevel) {
        super(player, resourcePoints);
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.oldLevel = json.getInt("oldLevel");
        this.newLevel = json.getInt("newLevel");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setInt("oldLevel", this.oldLevel);
        json.setInt("newLevel", this.newLevel);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.raw("old_level", this.oldLevel);
        builder.raw("new_level", this.newLevel);
    }
}
