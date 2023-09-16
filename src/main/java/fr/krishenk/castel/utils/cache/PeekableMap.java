package fr.krishenk.castel.utils.cache;

import java.util.Map;

public interface PeekableMap<K, V> extends Map<K, V> {
    public V peek(K key);
    public V getIfPresent(K key);
}
