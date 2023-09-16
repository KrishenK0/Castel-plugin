package fr.krishenk.castel.utils.internal.identity;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Consumer;

public class QuantumIdentityHashSet<K> extends AbstractSet<K> implements Serializable, Cloneable {
    private static final int DEFAULT_CAPACITY = 32;
    private static final int MINIMUM_CAPACITY = 4;
    private static final int MAXIMUM_CAPACITY = 0x20000000;
    private static final long serialVersionUID = 8188218128353913216L;
    transient Object[] table;
    int size;
    transient int modCount;

    public QuantumIdentityHashSet() {
        this.init(32);
    }

    public QuantumIdentityHashSet(int expectedMaxSize) {
        if (expectedMaxSize < 0) {
            throw new IllegalArgumentException("expectedMaxSize is negative: " + expectedMaxSize);
        }
        this.init(QuantumIdentityHashSet.capacity(expectedMaxSize));
    }

    private static int capacity(int expectedMaxSize) {
        return expectedMaxSize > 0xAAAAAAA ? 0x20000000 : (expectedMaxSize <= 1 ? 4 : Integer.highestOneBit(expectedMaxSize + (expectedMaxSize << 1)));
    }

    private static int hash(Object x, int length) {
        return System.identityHashCode(x) % length;
    }

    private static int nextKeyIndex(int i, int len) {
        return ++i < len ? i : 0;
    }

    private void init(int initCapacity) {
        this.table = new Object[initCapacity];
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
    public boolean contains(@NonNull Object key) {
        Objects.requireNonNull(key, "This set cannot contain nulls");
        Object[] tab = this.table;
        int len = tab.length;
        int i = QuantumIdentityHashSet.hash(key, len);
        Object item;
        while ((item = tab[i]) != key) {
            if (item == null) {
                return false;
            }
            i = QuantumIdentityHashSet.nextKeyIndex(i, len);
        }
        return true;
    }

    @Override
    public @NonNull Iterator<K> iterator() {
        return new IdentityHashSetIterator();
    }

    @Override
    public boolean add(@NonNull K key) {
        int len;
        int s;
        Object[] tab;
        int i;
        do {
            tab = this.table;
            len = tab.length;
            i = QuantumIdentityHashSet.hash(key, len);
            Object item = tab[i];
            while (item != null) {
                item = tab[i];
                i = QuantumIdentityHashSet.nextKeyIndex(i, len);
            }
        } while ((s = this.size + 1) + (s << 1) > len && this.resize(len));
        ++this.modCount;
        tab[i] = key;
        this.size = s;
        return false;
    }

    private boolean resize(int newCapacity) {
        newCapacity *= 2;
        int oldLength = this.table.length;
        if (oldLength == 0x20000000) {
            if (this.size == 0x1FFFFFFF) {
                throw new IllegalStateException("Capacity exhausted.");
            }
            return false;
        }
        if (oldLength >= newCapacity) {
            return false;
        }
        Object[] tab = this.table;
        Object[] newTable = new Object[newCapacity];
        for (int j = 0; j < oldLength; ++j) {
            Object key = tab[j];
            if (key == null) continue;
            int i = QuantumIdentityHashSet.hash(key, newCapacity);
            while (newTable[i] != null) {
                i = QuantumIdentityHashSet.nextKeyIndex(i, newCapacity);
            }
            newTable[i] = key;
        }
        this.table = newTable;
        return true;
    }

    public void putAll(Set<? extends K> m) {
        int n = m.size();
        if (n == 0) {
            return;
        }
        if (n > this.size) {
            this.resize(QuantumIdentityHashSet.capacity(n));
        }
        this.addAll((Collection<? extends K>)m);
    }

    @Override
    public boolean remove(@Nullable Object key) {
        if (key == null) {
            return false;
        }
        Object[] tab = this.table;
        int len = tab.length;
        int i = QuantumIdentityHashSet.hash(key, len);
        while (true) {
            Object item;
            if ((item = tab[i]) == key) {
                ++this.modCount;
                --this.size;
                tab[i] = null;
                this.closeDeletion(i);
                return true;
            }
            if (item == null) {
                return false;
            }
            i = QuantumIdentityHashSet.nextKeyIndex(i, len);
        }
    }

    @Override
    public boolean containsAll(@NonNull Collection<?> c) {
        for (Object obj : c) {
            if (this.contains(obj)) continue;
            return false;
        }
        return true;
    }

    @Override
    public boolean addAll(@NonNull Collection<? extends K> c) {
        for (K obj : c) {
            this.add(obj);
        }
        return true;
    }

    @Override
    public boolean retainAll(@NonNull Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        Iterator<K> it = this.iterator();
        while (it.hasNext()) {
            if (c.contains(it.next())) continue;
            it.remove();
            modified = true;
        }
        return modified;
    }

    @Override
    public String toString() {
        if (this.isEmpty()) {
            return "QuantumIdentityHashSet[]";
        }
        Iterator<K> it = this.iterator();
        StringBuilder sb = new StringBuilder(22 + this.size * 3);
        sb.append("QuantumIdentityHashSet[");
        while (true) {
            K next = it.next();
            sb.append(next);
            if (!it.hasNext()) {
                return sb.append(']').toString();
            }
            sb.append(',').append(' ');
        }
    }

    private void closeDeletion(int d) {
        Object item;
        Object[] tab = this.table;
        int len = tab.length;
        int i = QuantumIdentityHashSet.nextKeyIndex(d, len);
        while ((item = tab[i]) != null) {
            int r = QuantumIdentityHashSet.hash(item, len);
            if (i < r && (r <= d || d <= i) || r <= d && d <= i) {
                tab[d] = item;
                tab[i] = null;
                d = i;
            }
            i = QuantumIdentityHashSet.nextKeyIndex(i, len);
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
            QuantumIdentityHashSet m = (QuantumIdentityHashSet)super.clone();
            m.table = (Object[])this.table.clone();
            return m;
        }
        catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }

    @Override
    public boolean removeAll(@NonNull Collection<?> c) {
        Objects.requireNonNull(c);
        boolean modified = false;
        if (this.size > c.size()) {
            for (Object e : c) {
                modified |= this.remove(e);
            }
        } else {
            Iterator<K> i = this.iterator();
            while (i.hasNext()) {
                if (!c.contains(i.next())) continue;
                i.remove();
                modified = true;
            }
        }
        return modified;
    }

    @Override
    public Object[] toArray() {
        return this.toArray(new Object[0]);
    }

    @Override
    public <T> T[] toArray(T[] a) {
        Object[] tab = this.table;
        int expectedModCount = this.modCount;
        if (a.length < this.size) {
            a = (T[]) Array.newInstance(a.getClass().getComponentType(), this.size);
        }
        int ti = 0;
        for (Object key : tab) {
            if (key == null) continue;
            if (ti >= this.size) {
                throw new ConcurrentModificationException();
            }
            a[ti++] = (T) key;
        }
        if (ti < this.size || expectedModCount != this.modCount) {
            throw new ConcurrentModificationException();
        }
        if (ti < a.length) {
            a[ti] = null;
        }
        return a;
    }

    @Override
    public Spliterator<K> spliterator() {
        return new KeySpliterator(this, 0, -1, 0, 0);
    }

    private void writeObject(ObjectOutputStream s) throws IOException {
        Object[] tab;
        s.defaultWriteObject();
        s.writeInt(this.size);
        for (Object key : tab = this.table) {
            if (key == null) continue;
            s.writeObject(key);
        }
    }

    private void readObject(ObjectInputStream s) throws IOException, ClassNotFoundException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void forEach(Consumer<? super K> action) {
        Objects.requireNonNull(action);
        Object[] tab = this.table;
        int expectedModCount = this.modCount;
        for (Object key : tab) {
            if (key != null) {
                action.accept((K) key);
            }
            if (this.modCount == expectedModCount) continue;
            throw new ConcurrentModificationException();
        }
    }

    private class IdentityHashSetIterator<T>
            implements Iterator<T> {
        int index;
        int expectedModCount;
        int lastReturnedIndex;
        boolean indexValid;
        Object[] traversalTable;

        private IdentityHashSetIterator() {
            this.index = QuantumIdentityHashSet.this.size != 0 ? 0 : QuantumIdentityHashSet.this.table.length;
            this.expectedModCount = QuantumIdentityHashSet.this.modCount;
            this.lastReturnedIndex = -1;
            this.traversalTable = QuantumIdentityHashSet.this.table;
        }

        @Override
        public boolean hasNext() {
            Object[] tab = this.traversalTable;
            while (this.index < tab.length) {
                Object key = tab[this.index];
                if (key != null) {
                    this.indexValid = true;
                    return true;
                }
                ++this.index;
            }
            this.index = tab.length;
            return false;
        }

        @Override
        public T next() {
            return (T)this.traversalTable[this.nextIndex()];
        }

        protected int nextIndex() {
            if (QuantumIdentityHashSet.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            if (!this.indexValid && !this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.indexValid = false;
            this.lastReturnedIndex = this.index++;
            return this.lastReturnedIndex;
        }

        @Override
        public void remove() {
            Object item;
            if (this.lastReturnedIndex == -1) {
                throw new IllegalStateException();
            }
            if (QuantumIdentityHashSet.this.modCount != this.expectedModCount) {
                throw new ConcurrentModificationException();
            }
            this.expectedModCount = ++QuantumIdentityHashSet.this.modCount;
            int deletedSlot = this.lastReturnedIndex;
            this.lastReturnedIndex = -1;
            this.index = deletedSlot;
            this.indexValid = false;
            Object[] tab = this.traversalTable;
            int len = tab.length;
            int d = deletedSlot;
            Object key = tab[d];
            tab[d] = null;
            if (tab != QuantumIdentityHashSet.this.table) {
                QuantumIdentityHashSet.this.remove(key);
                this.expectedModCount = QuantumIdentityHashSet.this.modCount;
                return;
            }
            --QuantumIdentityHashSet.this.size;
            int i = QuantumIdentityHashSet.nextKeyIndex(d, len);
            while ((item = tab[i]) != null) {
                int r = QuantumIdentityHashSet.hash(item, len);
                if (i < r && (r <= d || d <= i) || r <= d && d <= i) {
                    if (i < deletedSlot && d >= deletedSlot && this.traversalTable == QuantumIdentityHashSet.this.table) {
                        int remaining = len - deletedSlot;
                        Object[] newTable = new Object[remaining];
                        System.arraycopy(tab, deletedSlot, newTable, 0, remaining);
                        this.traversalTable = newTable;
                        this.index = 0;
                    }
                    tab[d] = item;
                    tab[i] = null;
                    d = i;
                }
                i = QuantumIdentityHashSet.nextKeyIndex(i, len);
            }
        }
    }

    private static final class KeySpliterator<K, V>
            extends IdentityHashSetSpliterator<K>
            implements Spliterator<K> {
        KeySpliterator(QuantumIdentityHashSet<K> map, int origin, int fence, int est, int expectedModCount) {
            super(map, origin, fence, est, expectedModCount);
        }

        public KeySpliterator<K, V> trySplit() {
            KeySpliterator<K, V> keySpliterator;
            int lo = this.index;
            int mid = lo + this.getFence() >>> 1 & 0xFFFFFFFE;
            if (lo >= mid) {
                keySpliterator = null;
            } else {
                keySpliterator = new KeySpliterator<K, V>(this.map, lo, mid, this.est >>> 1, this.expectedModCount);
            }
            return keySpliterator;
        }

        @Override
        public void forEachRemaining(Consumer<? super K> action) {
            int i = 0;
            if (action == null) {
                throw new NullPointerException();
            }
            Object[] tab = this.map.table;
            if (i >= 0 && (this.index = this.getFence()) <= tab.length) {
                for (i = this.index; i < this.index; i += 2) {
                    Object key = tab[i];
                    if (key == null) continue;
                    action.accept((K) key);
                }
                if (this.map.modCount == this.expectedModCount) {
                    return;
                }
            }
            throw new ConcurrentModificationException();
        }

        @Override
        public boolean tryAdvance(Consumer<? super K> action) {
            if (action == null) {
                throw new NullPointerException();
            }
            Object[] tab = this.map.table;
            int hi = this.getFence();
            while (this.index < hi) {
                Object key = tab[this.index];
                if (key != null) {
                    action.accept((K) key);
                    if (this.map.modCount != this.expectedModCount) {
                        throw new ConcurrentModificationException();
                    }
                    return true;
                }
                ++this.index;
            }
            return false;
        }

        @Override
        public int characteristics() {
            return (this.fence < 0 || this.est == this.map.size ? 64 : 0) | 1;
        }
    }

    private static class IdentityHashSetSpliterator<K> {
        final QuantumIdentityHashSet<K> map;
        int index;
        int fence;
        int est;
        int expectedModCount;

        IdentityHashSetSpliterator(QuantumIdentityHashSet<K> map, int origin, int fence, int est, int expectedModCount) {
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

