package fr.krishenk.castel.utils.internal.identity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class QuantumIdentityHashMap<K, V> implements Map<K, V>, Serializable, Cloneable {
    private static final int DEFAULT_CAPACITY = 32;
    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 0x20000000;
    private static final long serialVersionUID = 8188218128353913216L;
    transient Object[] table;
    int size;
    transient int modCount;
    private transient Set<Map.Entry<K, V>> entrySet;
    private transient Set<K> keySet;
    private transient Collection<V> values;

    public QuantumIdentityHashMap() {
        this.init(32);
    }

    public QuantumIdentityHashMap(int expectedMaxSize) {
        if (expectedMaxSize < 0) {
            throw new IllegalArgumentException("expectedMaxSize is negative: " + expectedMaxSize);
        }
        this.init(QuantumIdentityHashMap.capacity(expectedMaxSize));
    }

    public QuantumIdentityHashMap(Map<? extends K, ? extends V> m) {
        this((int)((double)(1 + m.size()) * 1.1));
        this.putAll(m);
    }

    private static int capacity(int expectedMaxSize) {
        return expectedMaxSize > 0xAAAAAAA ? 0x20000000 : (expectedMaxSize <= 2 ? 4 : Integer.highestOneBit(expectedMaxSize + (expectedMaxSize << 1)));
    }

    private static int hash(Object x, int length) {
        int h = System.identityHashCode(x);
        return (h << 1) - (h << 8) & length - 1;
    }

    private static int nextKeyIndex(int i, int len) {
        return i + 2 < len ? i + 2 : 0;
    }

    private void init(int initCapacity) {
        this.table = new Object[2 * initCapacity];
    }

    @Override
    public int size() {
        return this.size;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    @Override
    public V get(Object key) {
        Object[] tab = this.table;
        int len = tab.length;
        int i = QuantumIdentityHashMap.hash(key, len);
        Object item;
        while ((item = tab[i]) != key) {
            if (item == null) {
                return null;
            }
            i = QuantumIdentityHashMap.nextKeyIndex(i, len);
        }
        return (V)tab[i + 1];
    }

    @Override
    public boolean containsKey(Object key) {
        Object[] tab = this.table;
        int len = tab.length;
        int i = QuantumIdentityHashMap.hash(key, len);
        Object item;
        while ((item = tab[i]) != key) {
            if (item == null) {
                return false;
            }
            i = QuantumIdentityHashMap.nextKeyIndex(i, len);
        }
        return true;
    }

    @Override
    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    private boolean containsMapping(Object key, Object value) {
        Object[] tab = this.table;
        int len = tab.length;
        int i = QuantumIdentityHashMap.hash(key, len);
        Object item;
        while ((item = tab[i]) != key) {
            if (item == null) {
                return false;
            }
            i = QuantumIdentityHashMap.nextKeyIndex(i, len);
        }
        return tab[i + 1] == value;
    }

    @Override
    public V put(@NonNull K key, @Nullable V value) {
        int len;
        int s;
        Objects.requireNonNull(key, "Cannot put null key");
        Object[] tab;
        int i;
        do {
            Object item;
            tab = this.table;
            len = tab.length;
            i = QuantumIdentityHashMap.hash(key, len);
            while ((item = tab[i]) != null) {
                if (item == key) {
                    Object oldValue = tab[i + 1];
                    tab[i + 1] = value;
                    return (V) oldValue;
                }
                i = QuantumIdentityHashMap.nextKeyIndex(i, len);
            }
        } while ((s = this.size + 1) + (s << 1) > len && this.resize(len));
        ++this.modCount;
        tab[i] = key;
        tab[i + 1] = value;
        this.size = s;
        return null;
    }

    private boolean resize(int newCapacity) {
        int newLength = newCapacity * 2;
        int oldLength = this.table.length;
        if (oldLength == 0x40000000) {
            if (this.size == 0x1FFFFFFF) {
                throw new IllegalStateException("Capacity exhausted.");
            }
            return false;
        }
        if (oldLength >= newLength) {
            return false;
        }
        Object[] oldTable = this.table;
        Object[] newTable = new Object[newLength];
        for (int j = 0; j < oldLength; j += 2) {
            Object key = oldTable[j];
            if (key == null) continue;
            Object value = oldTable[j + 1];
            oldTable[j] = null;
            oldTable[j + 1] = null;
            int i = QuantumIdentityHashMap.hash(key, newLength);
            while (newTable[i] != null) {
                i = QuantumIdentityHashMap.nextKeyIndex(i, newLength);
            }
            newTable[i] = key;
            newTable[i + 1] = value;
        }
        this.table = newTable;
        return true;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        int n = m.size();
        if (n == 0) {
            return;
        }
        if (n > this.size) {
            this.resize(QuantumIdentityHashMap.capacity(n));
        }
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            this.put(e.getKey(), e.getValue());
        }
    }

    @Override
    public V remove(@NonNull Object key) {
        Objects.requireNonNull(key, "Quantum Identity HashMap cannot contain null keys");
        Object[] tab = this.table;
        int len = tab.length;
        int i = QuantumIdentityHashMap.hash(key, len);
        while (true) {
            Object item;
            if ((item = tab[i]) == key) {
                ++this.modCount;
                --this.size;
                Object oldValue = tab[i + 1];
                tab[i + 1] = null;
                tab[i] = null;
                this.closeDeletion(i);
                return (V)oldValue;
            }
            if (item == null) {
                return null;
            }
            i = QuantumIdentityHashMap.nextKeyIndex(i, len);
        }
    }

    private boolean removeMapping(Object key, Object value) {
        Object[] tab = this.table;
        int len = this.table.length;
        int i = QuantumIdentityHashMap.hash(key, len);
        while (true) {
            Object item;
            if ((item = tab[i]) == key) {
                if (tab[i + 1] != value) {
                    return false;
                }
                ++this.modCount;
                --this.size;
                tab[i] = null;
                tab[i + 1] = null;
                this.closeDeletion(i);
                return true;
            }
            if (item == null) {
                return false;
            }
            i = QuantumIdentityHashMap.nextKeyIndex(i, len);
        }
    }

    private void closeDeletion(int d) {
        Object item;
        Object[] tab = this.table;
        int len = tab.length;
        int i = QuantumIdentityHashMap.nextKeyIndex(d, len);
        while ((item = tab[i]) != null) {
            int r = QuantumIdentityHashMap.hash(item, len);
            if (i < r && (r <= d || d <= i) || r <= d && d <= i) {
                tab[d] = item;
                tab[d + 1] = tab[i + 1];
                tab[i] = null;
                tab[i + 1] = null;
                d = i;
            }
            i = QuantumIdentityHashMap.nextKeyIndex(i, len);
        }
    }

    @Override
    public void clear() {
        ++this.modCount;
        Arrays.fill(this.table, null);
        this.size = 0;
    }

    public Object clone() {
        try {
            QuantumIdentityHashMap m = (QuantumIdentityHashMap)super.clone();
            m.entrySet = null;
            m.table = (Object[])this.table.clone();
            return m;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public @NonNull Set<K> keySet() {
        return this.keySet == null ? (this.keySet = new KeySet()) : this.keySet;
    }

    @Override
    public @NonNull Collection<V> values() {
        return this.values == null ? (this.values = new Values()) : this.values;
    }

    @Override
    public @NonNull Set<Map.Entry<K, V>> entrySet() {
        return this.entrySet == null ? (this.entrySet = new EntrySet()) : this.entrySet;
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        s.defaultWriteObject();
        s.writeInt(this.size);
        Object[] tab = this.table;
        for (int i = 0; i < tab.length; i += 2) {
            Object key = tab[i];
            if (key == null) continue;
            s.writeObject(key);
            s.writeObject(tab[i + 1]);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(BiConsumer<? super K, ? super V> action) {
        Objects.requireNonNull(action);
        Object[] tab = this.table;
        int expectedModCount = this.modCount;
        for (int index = 0; index < tab.length; index += 2) {
            Object k = tab[index];
            if (k != null) {
                action.accept((K) k, (V) tab[index + 1]);
            }
            if (this.modCount == expectedModCount) continue;
            throw new ConcurrentModificationException();
        }
    }

    @Override
    public void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        Objects.requireNonNull(function);
        Object[] tab = this.table;
        int expectedModCount = this.modCount;
        for (int index = 0; index < tab.length; index += 2) {
            Object k = tab[index];
            if (k != null) {
                tab[index + 1] = function.apply((K) k, (V) tab[index + 1]);
            }
            if (this.modCount == expectedModCount) continue;
            throw new ConcurrentModificationException();
        }
    }

    private class KeySet
            extends AbstractSet<K> {
        private KeySet() {
        }

        @Override
        public @NonNull Iterator<K> iterator() {
            return new KeyIterator();
        }

        @Override
        public int size() {
            return QuantumIdentityHashMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            return QuantumIdentityHashMap.this.containsKey(o);
        }

        @Override
        public boolean remove(Object o) {
            int oldSize = QuantumIdentityHashMap.this.size;
            QuantumIdentityHashMap.this.remove(o);
            return QuantumIdentityHashMap.this.size != oldSize;
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            Iterator i = this.iterator();
            while (i.hasNext()) {
                if (!c.contains(i.next())) continue;
                i.remove();
                modified = true;
            }
            return modified;
        }

        @Override
        public void clear() {
            QuantumIdentityHashMap.this.clear();
        }

        @Override
        public int hashCode() {
            int result = 0;
            for (Object key : this) {
                result += System.identityHashCode(key);
            }
            return result;
        }

        @Override
        public Object[] toArray() {
            return this.toArray(new Object[0]);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            int expectedModCount = QuantumIdentityHashMap.this.modCount;
            int size = this.size();
            if (a.length < size) {
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            }
            Object[] tab = QuantumIdentityHashMap.this.table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                Object key = tab[si];
                if (key == null) continue;
                if (ti >= size) {
                    throw new ConcurrentModificationException();
                }
                a[ti++] = (T) key;
            }
            if (ti < size || expectedModCount != QuantumIdentityHashMap.this.modCount) {
                throw new ConcurrentModificationException();
            }
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        @Override
        public Spliterator<K> spliterator() {
            return new KeySpliterator(QuantumIdentityHashMap.this, 0, -1, 0, 0);
        }
    }

    private class Values
            extends AbstractCollection<V> {
        private Values() {
        }

        @Override
        public @NonNull Iterator<V> iterator() {
            return new ValueIterator();
        }

        @Override
        public int size() {
            return QuantumIdentityHashMap.this.size;
        }

        @Override
        public boolean contains(Object o) {
            return QuantumIdentityHashMap.this.containsValue(o);
        }

        @Override
        public boolean remove(Object o) {
            Iterator i = this.iterator();
            while (i.hasNext()) {
                if (i.next() != o) continue;
                i.remove();
                return true;
            }
            return false;
        }

        @Override
        public void clear() {
            QuantumIdentityHashMap.this.clear();
        }

        @Override
        public Object[] toArray() {
            return this.toArray(new Object[0]);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            int expectedModCount = QuantumIdentityHashMap.this.modCount;
            int size = this.size();
            if (a.length < size) {
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            }
            Object[] tab = QuantumIdentityHashMap.this.table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                if (tab[si] == null) continue;
                if (ti >= size) {
                    throw new ConcurrentModificationException();
                }
                a[ti++] = (T) tab[si + 1];
            }
            if (ti < size || expectedModCount != QuantumIdentityHashMap.this.modCount) {
                throw new ConcurrentModificationException();
            }
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        @Override
        public Spliterator<V> spliterator() {
            return new ValueSpliterator(QuantumIdentityHashMap.this, 0, -1, 0, 0);
        }
    }

    private class EntrySet
            extends AbstractSet<Map.Entry<K, V>> {
        private EntrySet() {
        }

        @Override
        public @NonNull Iterator<Map.Entry<K, V>> iterator() {
            return new EntryIterator();
        }

        @Override
        public boolean contains(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry)o;
            return QuantumIdentityHashMap.this.containsMapping(entry.getKey(), entry.getValue());
        }

        @Override
        public boolean remove(Object o) {
            if (!(o instanceof Map.Entry)) {
                return false;
            }
            Map.Entry entry = (Map.Entry)o;
            return QuantumIdentityHashMap.this.removeMapping(entry.getKey(), entry.getValue());
        }

        @Override
        public int size() {
            return QuantumIdentityHashMap.this.size;
        }

        @Override
        public void clear() {
            QuantumIdentityHashMap.this.clear();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            Objects.requireNonNull(c);
            boolean modified = false;
            Iterator i = this.iterator();
            while (i.hasNext()) {
                if (!c.contains(i.next())) continue;
                i.remove();
                modified = true;
            }
            return modified;
        }

        @Override
        public Object[] toArray() {
            return this.toArray(new Object[0]);
        }

        @Override
        public <T> T[] toArray(T[] a) {
            int expectedModCount = QuantumIdentityHashMap.this.modCount;
            int size = this.size();
            if (a.length < size) {
                a = (T[]) Array.newInstance(a.getClass().getComponentType(), size);
            }
            Object[] tab = QuantumIdentityHashMap.this.table;
            int ti = 0;
            for (int si = 0; si < tab.length; si += 2) {
                Object key = tab[si];
                if (key == null) continue;
                if (ti >= size) {
                    throw new ConcurrentModificationException();
                }
                a[ti++] = (T) new AbstractMap.SimpleEntry<Object, Object>(key, tab[si + 1]);
            }
            if (ti < size || expectedModCount != QuantumIdentityHashMap.this.modCount) {
                throw new ConcurrentModificationException();
            }
            if (ti < a.length) {
                a[ti] = null;
            }
            return a;
        }

        @Override
        public Spliterator<Map.Entry<K, V>> spliterator() {
            return new EntrySpliterator(QuantumIdentityHashMap.this, 0, -1, 0, 0);
        }
    }

    private class EntryIterator
            extends IdentityHashMapIterator<Map.Entry<K, V>> {
        private EntryIterator.Entry lastReturnedEntry;

        private EntryIterator() {
        }

        @Override
        public Map.Entry<K, V> next() {
            this.lastReturnedEntry = new Entry(this.nextIndex());
            return this.lastReturnedEntry;
        }

        @Override
        public void remove() {
            this.lastReturnedIndex = null == this.lastReturnedEntry ? -1 : ((Entry)this.lastReturnedEntry).index;
            super.remove();
            ((Entry)this.lastReturnedEntry).index = this.lastReturnedIndex;
            this.lastReturnedEntry = null;
        }

        private class Entry
                implements Map.Entry<K, V> {
            private int index;

            private Entry(int index) {
                this.index = index;
            }

            @Override
            public K getKey() {
                this.checkIndexForEntryUse();
                return (K) EntryIterator.this.traversalTable[this.index];
            }

            @Override
            public V getValue() {
                this.checkIndexForEntryUse();
                return (V) EntryIterator.this.traversalTable[this.index + 1];
            }

            @Override
            public V setValue(V value) {
                this.checkIndexForEntryUse();
                Object oldValue = EntryIterator.this.traversalTable[this.index + 1];
                EntryIterator.this.traversalTable[this.index + 1] = value;
                if (EntryIterator.this.traversalTable != QuantumIdentityHashMap.this.table) {
                    QuantumIdentityHashMap.this.put((K) EntryIterator.this.traversalTable[this.index], value);
                }
                return (V) oldValue;
            }

            @Override
            public boolean equals(Object o) {
                if (this.index < 0) {
                    return super.equals(o);
                }
                if (!(o instanceof Map.Entry)) {
                    return false;
                }
                Map.Entry e = (Map.Entry)o;
                return e.getKey() == EntryIterator.this.traversalTable[this.index] && e.getValue() == EntryIterator.this.traversalTable[this.index + 1];
            }

            @Override
            public int hashCode() {
                if (EntryIterator.this.lastReturnedIndex < 0) {
                    return super.hashCode();
                }
                return System.identityHashCode(EntryIterator.this.traversalTable[this.index]) ^ System.identityHashCode(EntryIterator.this.traversalTable[this.index + 1]);
            }

            public String toString() {
                if (this.index < 0) {
                    return super.toString();
                }
                return EntryIterator.this.traversalTable[this.index] + "=" + EntryIterator.this.traversalTable[this.index + 1];
            }

            private void checkIndexForEntryUse() {
                if (this.index < 0) {
                    throw new IllegalStateException("Entry was removed");
                }
            }
        }
    }

    private class ValueIterator
            extends IdentityHashMapIterator<V> {
        private ValueIterator() {
        }

        @Override
        public V next() {
            return (V) this.traversalTable[this.nextIndex() + 1];
        }
    }

    private class KeyIterator
            extends IdentityHashMapIterator<K> {
        private KeyIterator() {
        }

        @Override
        public K next() {
            return (K) this.traversalTable[this.nextIndex()];
        }
    }

    private abstract class IdentityHashMapIterator<T>
            implements Iterator<T> {
        int index;
        int expectedModCount;
        int lastReturnedIndex;
        boolean indexValid;
        Object[] traversalTable;

        private IdentityHashMapIterator() {
            this.index = QuantumIdentityHashMap.this.size != 0 ? 0 : QuantumIdentityHashMap.this.table.length;
            this.expectedModCount = QuantumIdentityHashMap.this.modCount;
            this.lastReturnedIndex = -1;
            this.traversalTable = QuantumIdentityHashMap.this.table;
        }

        @Override
        public boolean hasNext() {
            Object[] tab = this.traversalTable;
            for (int i = this.index; i < tab.length; i += 2) {
                Object key = tab[i];
                if (key == null) continue;
                this.index = i;
                this.indexValid = true;
                return true;
            }
            this.index = tab.length;
            return false;
        }

        protected int nextIndex() {
            if (QuantumIdentityHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (!this.indexValid && !this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.indexValid = false;
            this.lastReturnedIndex = this.index;
            this.index += 2;
            return this.lastReturnedIndex;
        }

        @Override
        public void remove() {
            Object item;
            if (this.lastReturnedIndex == -1) {
                throw new IllegalStateException();
            }
            if (QuantumIdentityHashMap.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            this.expectedModCount = ++QuantumIdentityHashMap.this.modCount;
            int deletedSlot = this.lastReturnedIndex;
            this.lastReturnedIndex = -1;
            this.index = deletedSlot;
            this.indexValid = false;
            Object[] tab = this.traversalTable;
            int len = tab.length;
            int d = deletedSlot;
            Object key = tab[d];
            tab[d] = null;
            tab[d + 1] = null;
            if (tab != QuantumIdentityHashMap.this.table) {
                QuantumIdentityHashMap.this.remove(key);
                this.expectedModCount = QuantumIdentityHashMap.this.modCount;
                return;
            }
            --QuantumIdentityHashMap.this.size;
            int i = QuantumIdentityHashMap.nextKeyIndex(d, len);
            while ((item = tab[i]) != null) {
                int r = QuantumIdentityHashMap.hash(item, len);
                if (i < r && (r <= d || d <= i) || r <= d && d <= i) {
                    if (i < deletedSlot && d >= deletedSlot && this.traversalTable == QuantumIdentityHashMap.this.table) {
                        int remaining = len - deletedSlot;
                        Object[] newTable = new Object[remaining];
                        System.arraycopy(tab, deletedSlot, newTable, 0, remaining);
                        this.traversalTable = newTable;
                        this.index = 0;
                    }
                    tab[d] = item;
                    tab[d + 1] = tab[i + 1];
                    tab[i] = null;
                    tab[i + 1] = null;
                    d = i;
                }
                i = QuantumIdentityHashMap.nextKeyIndex(i, len);
            }
        }
    }

    static final class EntrySpliterator<K, V>
            extends IdentityHashMapSpliterator<K, V>
            implements Spliterator<Map.Entry<K, V>> {
        EntrySpliterator(QuantumIdentityHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public EntrySpliterator<K, V> trySplit() {
            EntrySpliterator<K, V> entrySpliterator;
            int lo = this.index;
            int mid = lo + this.getFence() >>> 1 & 0xFFFFFFFE;
            if (lo >= mid) {
                entrySpliterator = null;
            } else {
                this.index = this.index;
                this.est = this.est;
                entrySpliterator = new EntrySpliterator<K, V>(this.map, lo, mid, this.est >>> 1, this.expectedModCount);
            }
            return entrySpliterator;
        }

        @Override
        public void forEachRemaining(Consumer<? super Map.Entry<K, V>> action) {
            Objects.requireNonNull(action);
            QuantumIdentityHashMap m = this.map;
            if (m != null) {
                int i;
                Object[] a = m.table;
                if (m.table != null && (i = this.index) >= 0) {
                    int hi;
                    this.index = hi = this.getFence();
                    if (hi <= a.length) {
                        while (i < hi) {
                            Object key = a[i];
                            if (key != null) {
                                K k = (K) key;
                                V v = (V) a[i + 1];
                                action.accept(new AbstractMap.SimpleImmutableEntry<K, V>(k, v));
                            }
                            i += 2;
                        }
                        if (m.modCount == this.expectedModCount) {
                            return;
                        }
                    }
                }
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public boolean tryAdvance(Consumer<? super Map.Entry<K, V>> action) {
            Objects.requireNonNull(action);
            Object[] a = this.map.table;
            int hi = this.getFence();
            while (this.index < hi) {
                Object key = a[this.index];
                Object v = a[this.index + 1];
                this.index += 2;
                if (key == null) continue;
                Object k = key;
                action.accept(new AbstractMap.SimpleImmutableEntry<K, V>((K) k, (V) v));
                if (this.map.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return true;
            }
            return false;
        }

        @Override
        public int characteristics() {
            return (this.fence < 0 || this.est == this.map.size ? 64 : 0) | 1;
        }
    }

    static final class ValueSpliterator<K, V>
            extends IdentityHashMapSpliterator<K, V>
            implements Spliterator<V> {
        ValueSpliterator(QuantumIdentityHashMap<K, V> m, int origin, int fence, int est, int expectedModCount) {
            super(m, origin, fence, est, expectedModCount);
        }

        public ValueSpliterator<K, V> trySplit() {
            ValueSpliterator<K, V> valueSpliterator;
            int lo = this.index;
            int hi = this.getFence();
            int mid = lo + hi >>> 1 & 0xFFFFFFFE;
            if (lo >= mid) {
                valueSpliterator = null;
            } else {
                this.index = this.index;
                this.est = this.est;
                valueSpliterator = new ValueSpliterator<K, V>(this.map, lo, mid, this.est >>> 1, this.expectedModCount);
            }
            return valueSpliterator;
        }

        @Override
        public void forEachRemaining(Consumer<? super V> action) {
            Objects.requireNonNull(action);
            QuantumIdentityHashMap<K, V> m = this.map;
            if (m != null) {
                int i;
                Object[] a = m.table;
                if (m.table != null && (i = this.index) >= 0) {
                    int hi;
                    this.index = hi = this.getFence();
                    if (hi <= a.length) {
                        while (i < hi) {
                            if (a[i] != null) {
                                Object v = a[i + 1];
                                action.accept((V) v);
                            }
                            i += 2;
                        }
                        if (m.modCount == this.expectedModCount) {
                            return;
                        }
                    }
                }
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public boolean tryAdvance(Consumer<? super V> action) {
            Objects.requireNonNull(action);
            Object[] a = this.map.table;
            int hi = this.getFence();
            while (this.index < hi) {
                Object key = a[this.index];
                Object v = a[this.index + 1];
                this.index += 2;
                if (key == null) continue;
                action.accept((V) v);
                if (this.map.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return true;
            }
            return false;
        }

        @Override
        public int characteristics() {
            return this.fence < 0 || this.est == this.map.size ? 64 : 0;
        }
    }

    static final class KeySpliterator<K, V>
            extends IdentityHashMapSpliterator<K, V>
            implements Spliterator<K> {
        KeySpliterator(QuantumIdentityHashMap<K, V> map, int origin, int fence, int est, int expectedModCount) {
            super(map, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K, V> trySplit() {
            KeySpliterator<K, V> keySpliterator;
            int lo = this.index;
            int mid = lo + this.getFence() >>> 1 & 0xFFFFFFFE;
            if (lo >= mid) {
                keySpliterator = null;
            } else {
                this.index = this.index;
                this.est = this.est;
                keySpliterator = new KeySpliterator<K, V>(this.map, lo, mid, this.est >>> 1, this.expectedModCount);
            }
            return keySpliterator;
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            Objects.requireNonNull(action);
            QuantumIdentityHashMap m = this.map;
            if (m != null) {
                int i;
                Object[] tab = m.table;
                if (m.table != null && (i = this.index) >= 0) {
                    int hi;
                    this.index = hi = this.getFence();
                    if (hi <= tab.length) {
                        while (i < hi) {
                            Object key = tab[i];
                            if (key != null) {
                                action.accept((K) key);
                            }
                            i += 2;
                        }
                        if (m.modCount == this.expectedModCount) {
                            return;
                        }
                    }
                }
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            Objects.requireNonNull(action);
            Object[] tab = this.map.table;
            int hi = this.getFence();
            while (this.index < hi) {
                Object key = tab[this.index];
                this.index += 2;
                if (key == null) continue;
                action.accept((K) key);
                if (this.map.modCount != this.expectedModCount) {
                    throw new ConcurrentModificationException();
                }
                return true;
            }
            return false;
        }

        @Override
        public int characteristics() {
            return (this.fence < 0 || this.est == this.map.size ? 64 : 0) | 1;
        }
    }

    static class IdentityHashMapSpliterator<K, V> {
        final QuantumIdentityHashMap<K, V> map;
        int index;
        int fence;
        int est;
        int expectedModCount;

        IdentityHashMapSpliterator(QuantumIdentityHashMap<K, V> map, int origin, int fence, int est, int expectedModCount) {
            this.map = map;
            this.index = origin;
            this.fence = fence;
            this.est = est;
            this.expectedModCount = expectedModCount;
        }

        final int getFence() {
            if (this.fence < 0) {
                this.est = this.map.size;
                this.expectedModCount = this.map.modCount;
                this.fence = this.map.table.length;
            }
            return this.fence;
        }

        public final long estimateSize() {
            this.getFence();
            return this.est;
        }
    }
}

