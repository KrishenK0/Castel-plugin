package fr.krishenk.castel.constants.metadata;

import fr.krishenk.castel.constants.land.DeserializationContext;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.namespace.NamespaceContainer;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import lombok.NonNull;

import java.util.Objects;

public abstract class CastelMetadataHandler implements NamespaceContainer {
    private final @NonNull Namespace namespace;

    protected CastelMetadataHandler(@NonNull Namespace namespace) {
        this.namespace = Objects.requireNonNull(namespace);
        if (namespace.getNamespace().equals("castel")) {
            throw new IllegalArgumentException("Metadata handler namespace cannot be castel.");
        }
    }

    @Override
    public Namespace getNamespace() {
        return this.namespace;
    }

    public abstract  @NonNull CastelMetadata deserialize(@NonNull CastelObject<?> obj, @NonNull DeserializationContext<SectionableDataGetter> context);

    public int hashCode() {
        return this.namespace.hashCode();
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof CastelMetadataHandler)) {
            return false;
        }
        return this.namespace.equals(((CastelMetadataHandler) obj).namespace);
    }

    public String toString() {
        return this.getClass().getSimpleName() + "[" + this.namespace.asString() + "]";
    }
}
