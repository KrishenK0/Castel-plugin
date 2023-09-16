package fr.krishenk.castel.events;

import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.namespace.NamespaceMetadataContainer;
import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import org.bukkit.event.Event;

import java.util.Map;

public abstract class CastelEvent extends Event implements NamespaceMetadataContainer {
    private final Map<Namespace, Object> metadata = new NonNullMap<>();

    public CastelEvent() { }

    public CastelEvent(boolean isAsync) {
        super(isAsync);
    }

    @Override
    public Map<Namespace, Object> getMetadata() {
        return this.metadata;
    }

    public <T> T getMetadata(Namespace namespace) {
        Object obj = this.metadata.get(namespace);
        return  obj == null ? null : (T)obj;
    }
}
