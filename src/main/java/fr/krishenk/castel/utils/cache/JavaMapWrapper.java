package fr.krishenk.castel.utils.cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class JavaMapWrapper<K, V> implements PeekableMap<K, V> {
    private final ConcurrentHashMap<K, V> cache;
    private final CacheLoader<K, V> loader;

    public JavaMapWrapper(ConcurrentHashMap<K, V> cache, CacheLoader<K, V> loader) {
        this.cache = cache;
        this.loader = loader;
    }

    @Override
    public int size() {
        return this.cache.size();
    }

    @Override
    public boolean isEmpty() {
        return this.cache.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return this.cache.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(Object key) {
        V data = this.cache.get(key);
        if (data != null) return data;
        try {
            data = this.loader.load((K) key);
            if (data != null) this.put((K) key, data);
            return data;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    @Nullable
    public V put(K key, V value) {
        this.cache.put(key, value);
        return null;
    }

    @Override
    public V remove(Object key) {
        this.cache.remove(key);
        return null;
    }

    @Override
    public void putAll(@NonNull Map<? extends K, ? extends V> m) {
        this.cache.putAll(m);
    }

    @Override
    public void clear() {
        this.cache.clear();
    }

    @Override
    @NotNull
    public Set<K> keySet() {
        return this.cache.keySet();
    }

    @Override
    @NotNull
    public Collection<V> values() {
        return this.cache.values();
    }

    @Override
    @NotNull
    public Set<Map.Entry<K, V>> entrySet() {
        return this.cache.entrySet();
    }

    @Override
    public V peek(K key) {
        return this.cache.get(key);
    }

    @Override
    public V getIfPresent(K key) {
        return this.cache.get(key);
    }
}
