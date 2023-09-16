package fr.krishenk.castel.utils.internal.enumeration;

import fr.krishenk.castel.data.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class QuickEnumMap<K extends Enum<K>, V> implements Map<K, V> {
    private final transient V[] vals;
    private transient int size = 0;
    private final transient K[] universe;


    public QuickEnumMap(K[] universe) {
        this.universe = universe;
        this.vals = (V[]) new Object[universe.length];
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public boolean containsValue(Object value) {
        for (V val : this.vals) {
            if (value.equals(val)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean containsKey(Object key) {
        return this.get(key) != null;
    }

    @Override
    public V get(Object key) {
        Objects.requireNonNull(key, "QuickEnumMap may not contain null keys");
        return this.vals[((Enum<?>)key).ordinal()];
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        Objects.requireNonNull(key, "QuickEnumMap may not contain null keys");
        Objects.requireNonNull(value, "QuickEnumMap may not contain null values");
        int index = key.ordinal();
        V oldValue = this.vals[index];
        this.vals[index] = value;
        if (oldValue == null)  {
            ++this.size;
        }
        return oldValue;
    }

    @Override
    public V remove(Object key) {
        Objects.requireNonNull(key, "QuickEnumMap may not contain null keys");
        int index = ((Enum<?>)key).ordinal();
        V oldValue = this.vals[index];
        this.vals[index] = null;
        if (oldValue != null)  {
            --this.size;
        }
        return oldValue;
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        Arrays.fill(this.vals, null);
        this.size = 0;
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        QuickEnumSet<K> keys = new QuickEnumSet<>(this.universe);
        for (int i = 0; i < this.vals.length; i++) {
            if (vals[i] == null) continue;
            keys.add(this.universe[i]);
        }
        return Collections.unmodifiableSet(keys);
    }

    @NotNull
    @Override
    public Collection<V> values() {
        ArrayList<V> values = new ArrayList<>(this.size);
        for (V val : this.vals) {
            if (val == null) continue;
            values.add(val);
        }
        return Collections.unmodifiableCollection(values);
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        HashSet<Pair<K, V>> entries = new HashSet<>(this.size);
        for (int i = 0; i < this.vals.length; i++) {
            V val = this.vals[i];
            if (val == null) continue;
            entries.add(Pair.of(this.universe[i], val));
        }
        return Collections.unmodifiableSet(entries);
    }

    @Override
    public boolean equals(Object obj) {
        throw new UnsupportedOperationException("Cannot check equality between QuickEnumMap");
    }

    @Override
    public int hashCode() {
        throw new UnsupportedOperationException("Cannot generate hashcode for QuickEnumMap");
    }
}
