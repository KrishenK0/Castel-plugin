package fr.krishenk.castel.constants.group.model.logs.purchases;

import fr.krishenk.castel.constants.group.upgradable.Upgrade;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;
import java.util.UUID;

public abstract class LogGuildGeneralUpgrade extends LogGuildUpgrade {
    private Upgrade upgrade;

    public LogGuildGeneralUpgrade() {
    }

    public LogGuildGeneralUpgrade(UUID player, long resourcePoints, int oldLevel, int newLevel, Upgrade upgrade) {
        super(player, resourcePoints, oldLevel, newLevel);
        this.upgrade = upgrade;
    }

    public Upgrade getUpgrade() {
        return upgrade;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        this.upgrade = this.getUpgradeFromString(context.getDataProvider().getString("upgrade"));
    }

    protected abstract Upgrade getUpgradeFromString(String upgradeName);

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        context.getDataProvider().setString("upgrade", this.upgrade.getDataName());
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.raw("upgrade", this.upgrade.getDataName());
    }
}
