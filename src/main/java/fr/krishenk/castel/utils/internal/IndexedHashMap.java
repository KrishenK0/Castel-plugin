package fr.krishenk.castel.utils.internal;

import fr.krishenk.castel.utils.internal.arrays.UnsafeArrayList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class IndexedHashMap<K, V> {
    private final Map<K, V> map = new UnsafeHashMap();
    private final UnsafeArrayList<K> list;

    public IndexedHashMap(K[] sample) {
        this.list = UnsafeArrayList.withSize(sample);
    }

    public K[] asArray() {
        return this.list.getArray();
    }

    public K[] iterator() {
        return this.list.getArray();
    }

    public V get(K key) {
        return this.map.get(key);
    }

    public K at(int i) {
        return i < this.list.size ? (K)this.list.getArray()[i] : null;
    }

    public V get(K key, V def) {
        return this.map.getOrDefault(key, def);
    }

    public int size() {
        return this.list.size;
    }

    public void add(K key, V val) {
        this.map.put(key, val);
        this.list.add(key);
    }

    public void set(K[] keys, Function<Integer, V> converter) {
        this.clear();
        this.list.setArray(keys);
        for (int i = 0; i < keys.length; ++i) {
            this.map.put(keys[i], converter.apply(i));
        }
    }

    public <U> List<U> subList(int skip, int limit, Function<K, U> converter) {
        if (skip >= this.list.size) {
            return new ArrayList();
        }
        ArrayList<U> filtered = new ArrayList<U>(limit);
        int count = 0;
        int index = 0;
        while (count - skip <= limit && index < this.list.size) {
            U item = converter.apply(this.list.getArray()[index++]);
            if (item == null || ++count <= skip) continue;
            filtered.add(item);
        }
        return filtered;
    }

    public void clear() {
        this.map.clear();
        this.list.clear();
    }
}

