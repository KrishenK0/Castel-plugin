package fr.krishenk.castel.utils.cache;

import com.github.benmanes.caffeine.cache.LoadingCache;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class CaffeineWrapper<K, V> implements PeekableMap<K, V> {
    private final LoadingCache<K, V> cache;

    public CaffeineWrapper(LoadingCache<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public int size() {
        this.cache.cleanUp();
        return (int)this.cache.estimatedSize();
    }

    @Override
    public boolean isEmpty() {
        return this.size() != 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.cache.getIfPresent((K) key) != null;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(Object key) {
        return (V)this.cache.get((K) key);
    }

    @Override
    @Nullable
    public V put(K key, V value) {
        this.cache.put(key, value);
        return null;
    }

    @Override
    public V remove(Object key) {
        this.cache.invalidate((K) key);
        return null;
    }

    @Override
    public void putAll( Map<? extends K, ? extends V> m) {
        this.cache.putAll(m);
    }

    @Override
    public void clear() {
        this.cache.invalidateAll();
    }

    @Override
    public Set<K> keySet() {
        return this.cache.asMap().keySet();
    }

    @Override
    public Collection<V> values() {
        return this.cache.asMap().values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return this.cache.asMap().entrySet();
    }

    @Override
    public V peek(K key) {
        return (V)this.cache.getIfPresent(key);
    }

    @Override
    public V getIfPresent(K key) {
        return (V)this.cache.getIfPresent(key);
    }
}
