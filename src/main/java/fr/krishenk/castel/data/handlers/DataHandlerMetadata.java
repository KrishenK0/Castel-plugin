package fr.krishenk.castel.data.handlers;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.constants.metadata.CastelMetadata;
import fr.krishenk.castel.constants.metadata.CastelMetadataHandler;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.constants.metadata.StandardGuildMetadataHandler;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Objects;

public class DataHandlerMetadata {
    public static void deserializeMetadata(SectionableDataGetter provider, CastelObject<?> container) {
        HashMap<CastelMetadataHandler, CastelMetadata> metadata;
        try {
            metadata = provider.get("metadata").asMap(new HashMap<>(), (map, key, value) -> {
                String string;
                try {
                    string = key.asString(() -> {
                        throw new UnsupportedOperationException();
                    });
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                Namespace ns = Namespace.fromString(Objects.requireNonNull(string));
                CastelMetadataHandler metadataHandler = CastelPlugin.getInstance().getMetadataRegistry().getRegistered(ns);
                if (metadataHandler == null)
                    metadataHandler = new StandardGuildMetadataHandler(ns);
                try {
                    CastelMetadata data = metadataHandler.deserialize(container, new DeserializationContext<>(value));
                    map.put(metadataHandler, data);
                } catch (Throwable e) {
                    throw new RuntimeException("Error while deserializing metadata with namespace: " + string + " -> " + ns + " -> " + metadataHandler, e);
                }
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        container.setMetadata(metadata);
    }

    public static void serializeMetadata(SectionableDataSetter provider, CastelObject<?> obj) {
        provider.get("metadata").setMap(obj.getMetadata(), (key, keyProvider, value) -> {
            if (!value.shouldSave(obj)) return;
            keyProvider.setString(key.getNamespace().asNormalizedString());
            try {
                value.serialize(obj, new SerializationContext<>(keyProvider.getValueProvider()));
            } catch (Throwable e) {
                throw new RuntimeException("Error while serializing metadata with namespace: " + key.getNamespace(), e);
            }
        });
    }
}
