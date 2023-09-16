package fr.krishenk.castel.data;

import java.util.Map;

public class Pair<K, V> implements Map.Entry<K, V> {
    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public static <K, V> Pair<K, V> of(K key, V value) {
        return new Pair<K, V>(key, value);
    }

    public static <K, V> Pair<K, V> empty() {
        return new Pair<K, V>(null, null);
    }

    @Override
    public K getKey() {
        return this.key;
    }

    public void setKey(K key) {
        this.key = key;
    }

    @Override
    public V getValue() {
        return this.value;
    }

    @Override
    public V setValue(V value) {
        this.value = value;
        return this.value;
    }

    public boolean isKeyPresent() {
        return this.key != null;
    }

    public boolean isValuePresent() {
        return this.value != null;
    }

    public boolean areBothPresent() {
        return this.isKeyPresent() && this.isValuePresent();
    }

    public boolean areBothNull() {
        return !this.isKeyPresent() && !this.isValuePresent();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Map.Entry)) {
            return false;
        }
        Map.Entry entry = (Map.Entry)obj;
        return (this.key == null ? entry.getKey() == null : this.key.equals(entry.getKey())) && (this.value == null ? entry.getValue() == null : this.value.equals(entry.getValue()));
    }

    @Override
    public int hashCode() {
        return (this.key == null ? 0 : this.key.hashCode()) ^ (this.value == null ? 0 : this.value.hashCode());
    }

    public String toString() {
        return "Pair{" + this.key + " | " + this.value + '}';
    }
}
