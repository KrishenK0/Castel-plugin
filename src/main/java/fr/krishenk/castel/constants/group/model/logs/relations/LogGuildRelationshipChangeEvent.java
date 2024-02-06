package fr.krishenk.castel.constants.group.model.logs.relations;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.logs.AuditLog;
import fr.krishenk.castel.constants.group.model.logs.AuditLogProvider;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.util.UUID;

public class LogGuildRelationshipChangeEvent extends LogGroupRelationshipChangeEvent {
    private static final Namespace NS = Namespace.castel("GUILD_RELATION_CHANGE");
    public static final AuditLogProvider PROVIDER = new AuditLogProvider() {
        @Override
        public AuditLog construct() {
            return new LogGuildRelationshipChangeEvent();
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

    protected LogGuildRelationshipChangeEvent() {
    }

    public LogGuildRelationshipChangeEvent(UUID otherGroup, GuildRelation oldRelation, GuildRelation newRelation) {
        super(otherGroup, oldRelation, newRelation);
    }

    @Override
    public void addEdits(MessageBuilder builder) {
        super.addEdits(builder);
        builder.withContext(Guild.getGuild(super.getOtherGroupId()));
    }
}
