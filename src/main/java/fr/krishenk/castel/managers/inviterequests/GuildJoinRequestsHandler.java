package fr.krishenk.castel.managers.inviterequests;

import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.metadata.CastelMetadata;
import fr.krishenk.castel.constants.metadata.CastelMetadataHandler;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import lombok.NonNull;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildJoinRequestsHandler extends CastelMetadataHandler {
    public static final GuildJoinRequestsHandler INSTANCE = new GuildJoinRequestsHandler();

    private GuildJoinRequestsHandler() {
        super(JoinRequests.GUILD_NAMESPACE);
    }

    @Override
    public @NonNull CastelMetadata deserialize(@NonNull CastelObject<?> obj, @NonNull DeserializationContext<SectionableDataGetter> context) {
        Map<UUID, Long> requests = null;
        try {
            requests = context.getDataProvider().asMap(new HashMap<>(), (map, k, v) -> {
                try {
                    map.put(k.asUUID(), v.asLong());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return new GuildJoinRequestsMeta(requests);
    }
}
