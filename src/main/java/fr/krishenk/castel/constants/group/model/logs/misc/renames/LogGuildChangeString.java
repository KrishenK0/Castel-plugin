package fr.krishenk.castel.constants.group.model.logs.misc.renames;

import fr.krishenk.castel.constants.group.model.logs.misc.LogPlayerOperator;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;
import java.util.UUID;

public abstract class LogGuildChangeString extends LogPlayerOperator {
    private String old;
    private String newStr;

    protected LogGuildChangeString() {
    }

    public LogGuildChangeString(UUID player, String old, String newStr) {
        super(player);
        this.old = old;
        this.newStr = newStr;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.old = json.getString("old");
        this.newStr = json.getString("new");
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setString("old", this.old);
        json.setString("new", this.newStr);
    }

    public String getOld() {
        return old;
    }

    public String getNew() {
        return newStr;
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        if (this.old == null) builder.raw("old", Lang.NONE);
        else builder.parse("old", this.old);
        if (this.newStr == null) builder.raw("new", Lang.NONE);
        else builder.parse("new", this.newStr);
    }
}
