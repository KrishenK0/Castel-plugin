package fr.krishenk.castel.constants.group.model.logs.relations;

import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.sql.SQLException;
import java.util.UUID;

abstract class LogGroupRelationshipChangeEvent extends AuditLog {
    private UUID otherGroup;
    private GuildRelation oldRelation;
    private GuildRelation newRelation;

    protected LogGroupRelationshipChangeEvent() {
    }

    protected LogGroupRelationshipChangeEvent(UUID otherGroup, GuildRelation oldRelation, GuildRelation newRelation) {
        this.otherGroup = otherGroup;
        this.oldRelation = oldRelation;
        this.newRelation = newRelation;
    }

    @Override
    public void deserialize(DeserializationContext<SectionableDataGetter> context) throws SQLException {
        super.deserialize(context);
        SectionableDataGetter json = context.getDataProvider();
        this.otherGroup = json.get("other").asUUID();
        this.oldRelation = GuildRelation.valueOf(json.getString("oldRelation"));
        this.newRelation = GuildRelation.valueOf(json.getString("newRelation"));
    }

    @Override
    public void serialize(SerializationContext<SectionableDataSetter> context) {
        super.serialize(context);
        SectionableDataSetter json = context.getDataProvider();
        json.setUUID("other", this.otherGroup);
        json.setString("oldRelation", this.oldRelation.name());
        json.setString("newRelation", this.newRelation.name());
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.parse("old-relation", this.oldRelation.getColor() + this.oldRelation.getName().buildPlain(builder));
        builder.parse("new-relation", this.newRelation.getColor() + this.newRelation.getName().buildPlain(builder));
    }

    public GuildRelation getOldRelation() {
        return oldRelation;
    }

    public GuildRelation getNewRelation() {
        return newRelation;
    }

    public UUID getOtherGroupId() {
        return otherGroup;
    }
}
