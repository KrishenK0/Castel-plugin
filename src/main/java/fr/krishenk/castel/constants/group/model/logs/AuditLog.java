package fr.krishenk.castel.constants.group.model.logs;

import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageContextProvider;
import fr.krishenk.castel.utils.time.TimeUtils;

import java.sql.SQLException;

public abstract class AuditLog implements MessageContextProvider {
    private long time;

    public abstract AuditLogProvider getProvider();

    public AuditLog() {
        this.time = System.currentTimeMillis();
    }


    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        this.time = context.getDataProvider().get("time").asLong();
    }

    public void serialize(SerializationContext<SectionableDataSetter> context) {
        context.getDataProvider().setLong("time", this.time);
    }

    public long getTime() {
        return time;
    }

    @Override
    public void addMessageContextEdits(MessageBuilder builder) {
        this.addEdits(builder);
    }

    public void addEdits(MessageBuilder builder) {
        builder.raw("time", TimeUtils.getDateAndTime(this.time).toString());
    }
}
