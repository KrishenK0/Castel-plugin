package fr.krishenk.castel.constants.namespace;

import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;

public class NamespaceRegistery<V extends NamespaceContainer> {
    protected final Map<Namespace, V> registry = new NonNullMap<>();

    public void register(V value) {
        Namespace namespace = value.getNamespace();
        Objects.requireNonNull(namespace, "Cannot register object with null namespace");
        Objects.requireNonNull(value, "Cannot register null object");
        NamespaceContainer prev = this.registry.putIfAbsent(namespace, value);
        if (prev != null) throw new IllegalArgumentException(namespace + " was already registered");
    }

    public V getRegistered(Namespace namespace) {
        return this.registry.get(namespace);
    }

    public boolean isRegistered(Namespace namespace) {
        return this.registry.containsKey(namespace);
    }

    public Map<Namespace, V> getRegistry() {
        return Collections.unmodifiableMap(this.registry);
    }
}
