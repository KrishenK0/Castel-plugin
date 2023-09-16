package fr.krishenk.castel.constants.metadata;

import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.CastelGson;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import lombok.NonNull;

import java.sql.SQLException;

public class StandardGuildMetadataHandler extends CastelMetadataHandler {
    public StandardGuildMetadataHandler(Namespace namespace) {
        super(namespace);
    }

    @Override
    public @NonNull CastelMetadata deserialize(@NonNull CastelObject<?> obj, @NonNull DeserializationContext<SectionableDataGetter> context) {
        try {
            return new StandardGuildMetadata(CastelGson.GSON.fromJson(context.getDataProvider().asString(), Object.class));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
