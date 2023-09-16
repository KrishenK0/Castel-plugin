package fr.krishenk.castel.utils.internal.nonnull;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

public class NonNullMap<K, V> implements Map<K, V> {
    private final Map<K, V> map;

    public NonNullMap(Map<K, V> map) {
        this.map = Objects.requireNonNull(map);
    }

    public NonNullMap(int size) {
        this.map = new HashMap(size);
    }

    public NonNullMap() {
        this.map = new HashMap();
    }

    public static <K, V> NonNullMap<K, V> of(Map<K, V> map) {
        if (map instanceof NonNullMap) {
            return (NonNullMap)map;
        }
        return new NonNullMap<K, V>(map);
    }

    public static <K, V> NonNullMap<K, V> checked(Map<K, V> map) {
        if (map instanceof NonNullMap) {
            return (NonNullMap)map;
        }
        for (Map.Entry<K, V> entry : map.entrySet()) {
            NonNullMap.assertNonNullKey(entry.getKey());
            NonNullMap.assertNonNullValue(entry.getValue());
        }
        return new NonNullMap<K, V>(map);
    }

    public static <K, V> NonNullMap<K, V> copyOf(Map<K, V> map) {
        NonNullMap<K, V> newMap = new NonNullMap<K, V>(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            NonNullMap.assertNonNullKey(entry.getKey());
            NonNullMap.assertNonNullValue(entry.getValue());
            newMap.put(entry.getKey(), entry.getValue());
        }
        return newMap;
    }

    @Override
    public int size() {
        return this.map.size();
    }

    @Override
    public boolean isEmpty() {
        return this.map.isEmpty();
    }

    static void assertNonNullKey(Object key) {
        if (key == null) {
            throw new NullPointerException("Cannot contain null keys");
        }
    }

    static void assertNonNullValue(Object value) {
        if (value == null) {
            throw new NullPointerException("Cannot contain null values");
        }
    }

    @Override
    public boolean containsKey(Object key) {
        NonNullMap.assertNonNullKey(key);
        return this.map.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        NonNullMap.assertNonNullValue(value);
        return this.map.containsValue(value);
    }

    @Override
    public V get(Object key) {
        NonNullMap.assertNonNullKey(key);
        return this.map.get(key);
    }

    @Override
    @Nullable
    public V put(K key, V value) {
        NonNullMap.assertNonNullKey(key);
        NonNullMap.assertNonNullValue(value);
        return this.map.put(key, value);
    }

    @Override
    public V remove(Object key) {
        NonNullMap.assertNonNullKey(key);
        return this.map.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        this.map.putAll(m);
    }

    @Override
    public void clear() {
        this.map.clear();
    }

    @Override
    @NotNull
    public Set<K> keySet() {
        return this.map.keySet();
    }

    @Override
    @NotNull
    public Collection<V> values() {
        return this.map.values();
    }

    @Override
    @NotNull
    public Set<Map.Entry<K, V>> entrySet() {
        return this.map.entrySet();
    }

    @Override
    public V getOrDefault(Object key, V defaultValue) {
        NonNullMap.assertNonNullKey(key);
        NonNullMap.assertNonNullValue(defaultValue);
        return this.map.getOrDefault(key, defaultValue);
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        this.map.forEach(action);
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        this.map.replaceAll(function);
    }

    @Override
    @Nullable
    public V putIfAbsent(K key, V value) {
        NonNullMap.assertNonNullKey(key);
        NonNullMap.assertNonNullValue(value);
        return this.map.putIfAbsent(key, value);
    }

    @Override
    public boolean remove(Object key, Object value) {
        NonNullMap.assertNonNullKey(key);
        NonNullMap.assertNonNullValue(value);
        return this.map.remove(key, value);
    }

    @Override
    public boolean replace(K key, V oldValue, V newValue) {
        NonNullMap.assertNonNullKey(key);
        NonNullMap.assertNonNullValue(oldValue);
        NonNullMap.assertNonNullValue(newValue);
        return this.map.replace(key, oldValue, newValue);
    }

    @Override
    @Nullable
    public V replace(K key, V value) {
        NonNullMap.assertNonNullKey(key);
        NonNullMap.assertNonNullValue(value);
        return this.map.replace(key, value);
    }

    @Override
    public V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        NonNullMap.assertNonNullKey(key);
        return this.map.computeIfAbsent((K)key, mappingFunction);
    }

    @Override
    public V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        NonNullMap.assertNonNullKey(key);
        return this.map.computeIfPresent((K)key, (BiFunction<? super K, ? super V, ? extends V>) remappingFunction);
    }

    @Override
    public V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        NonNullMap.assertNonNullKey(key);
        return this.map.compute((K)key, (BiFunction<? super K, ? super V, ? extends V>) remappingFunction);
    }

    @Override
    public V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        NonNullMap.assertNonNullKey(key);
        return this.map.merge(key, (V)value, (BiFunction<? super V, ? super V, ? extends V>) remappingFunction);
    }
}
