package fr.krishenk.castel.constants.group.model.relationships;

import fr.krishenk.castel.constants.group.model.GuildRequest;

import java.util.UUID;

public class GuildRelationshipRequest extends GuildRequest {
    private final GuildRelation relation;

    public GuildRelationshipRequest(GuildRelation relation, UUID sender, long acceptTime, long timestamp) {
        super(sender, acceptTime, timestamp);
        this.relation = relation;
    }

    public GuildRelationshipRequest(GuildRelation relation, UUID sender, long acceptTime) {
        this(relation, sender, acceptTime, System.currentTimeMillis());
    }

    public GuildRelation getRelation() {
        return relation;
    }
}
