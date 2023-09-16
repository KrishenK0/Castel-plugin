package fr.krishenk.castel.managers.inviterequests;

import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.metadata.CastelMetadata;
import fr.krishenk.castel.constants.metadata.CastelMetadataHandler;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import lombok.NonNull;

import java.sql.SQLException;
import java.util.HashSet;

public class PlayerJoinRequestsHandler extends CastelMetadataHandler {
    public static final PlayerJoinRequestsHandler INSTANCE = new PlayerJoinRequestsHandler();

    private PlayerJoinRequestsHandler() {
        super(JoinRequests.PLAYER_NAMESPACE);
    }

    @Override
    public @NonNull CastelMetadata deserialize(@NonNull CastelObject<?> obj, @NonNull DeserializationContext<SectionableDataGetter> context) {
        try {
            return new PlayerJoinRequestsMeta(context.getDataProvider().asCollection(new HashSet<>(), (c, x) -> {
                try {
                    c.add(x.asUUID());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
