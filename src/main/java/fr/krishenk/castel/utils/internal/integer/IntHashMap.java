package fr.krishenk.castel.utils.internal.integer;

import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntHashMap<V> implements Iterable<V> {
    public int[] keys;
    public V[] values;
    protected int assigned;
    protected int mask;
    protected int resizeAt;
    protected boolean hasEmptyKey;
    protected double loadFactor;
    protected int iterationSeed;

    public IntHashMap() {
        this(4);
    }

    public IntHashMap(int expectedElements) {
        this(expectedElements, 0.75);
    }

    public IntHashMap(int expectedElements, double loadFactor) {
        this.loadFactor = this.verifyLoadFactor(loadFactor);
        this.iterationSeed = HashContainers.nextIterationSeed();
        this.ensureCapacity(expectedElements);
    }

    public V put(int key, V value) {
        assert this.assigned < this.mask + 1;

        int mask = this.mask;
        if (key == 0) {
            this.hasEmptyKey = true;
            V previousValue = this.values[mask + 1];
            this.values[mask + 1] = value;
            return previousValue;
        } else {
            int[] keys = this.keys;

            int slot;
            int existing;
            for(slot = this.hashKey(key) & mask; (existing = keys[slot]) != 0; slot = slot + 1 & mask) {
                if (existing == key) {
                    V previousValue = this.values[slot];
                    this.values[slot] = value;
                    return previousValue;
                }
            }

            if (this.assigned == this.resizeAt) {
                this.allocateThenInsertThenRehash(slot, key, value);
            } else {
                keys[slot] = key;
                this.values[slot] = value;
            }

            ++this.assigned;
            return null;
        }
    }

    public V remove(int key) {
        int mask = this.mask;
        if (key == 0) {
            this.hasEmptyKey = false;
            V previousValue = this.values[mask + 1];
            this.values[mask + 1] = null;
            return previousValue;
        } else {
            int[] keys = this.keys;

            int existing;
            for(int slot = this.hashKey(key) & mask; (existing = keys[slot]) != 0; slot = slot + 1 & mask) {
                if (existing == key) {
                    V previousValue = this.values[slot];
                    this.shiftConflictingKeys(slot);
                    return previousValue;
                }
            }

            return null;
        }
    }

    public V get(int key) {
        if (key == 0) {
            return this.hasEmptyKey ? this.values[this.mask + 1] : null;
        } else {
            int[] keys = this.keys;
            int mask = this.mask;

            int existing;
            for(int slot = this.hashKey(key) & mask; (existing = keys[slot]) != 0; slot = slot + 1 & mask) {
                if (existing == key) {
                    return this.values[slot];
                }
            }

            return null;
        }
    }

    public V getOrDefault(int key, V defaultValue) {
        if (key == 0) {
            return this.hasEmptyKey ? this.values[this.mask + 1] : defaultValue;
        } else {
            int[] keys = this.keys;
            int mask = this.mask;

            int existing;
            for(int slot = this.hashKey(key) & mask; (existing = keys[slot]) != 0; slot = slot + 1 & mask) {
                if (existing == key) {
                    return this.values[slot];
                }
            }

            return defaultValue;
        }
    }

    public boolean containsKey(int key) {
        if (key == 0) {
            return this.hasEmptyKey;
        } else {
            int[] keys = this.keys;
            int mask = this.mask;

            int existing;
            for(int slot = this.hashKey(key) & mask; (existing = keys[slot]) != 0; slot = slot + 1 & mask) {
                if (existing == key) {
                    return true;
                }
            }

            return false;
        }
    }

    public void clear() {
        this.assigned = 0;
        this.hasEmptyKey = false;
        Arrays.fill(this.keys, 0);
        Arrays.fill(this.values, null);
    }

    public void release() {
        this.assigned = 0;
        this.hasEmptyKey = false;
        this.keys = null;
        this.values = null;
        this.ensureCapacity(4);
    }

    public int size() {
        return this.assigned + (this.hasEmptyKey ? 1 : 0);
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    public void ensureCapacity(int expectedElements) {
        if (expectedElements > this.resizeAt || this.keys == null) {
            int[] prevKeys = this.keys;
            V[] prevValues = this.values;
            this.allocateBuffers(HashContainers.minBufferSize(expectedElements, this.loadFactor));
            if (prevKeys != null && !this.isEmpty()) {
                this.rehash(prevKeys, prevValues);
            }
        }

    }

    protected int hashKey(int key) {
        assert key != 0;

        return HashContainers.mixPhi(key);
    }

    protected double verifyLoadFactor(double loadFactor) {
        HashContainers.checkLoadFactor(loadFactor, 0.009999999776482582, 0.9900000095367432);
        return loadFactor;
    }

    protected void rehash(int[] fromKeys, V[] fromValues) {
        assert fromKeys.length == fromValues.length && HashContainers.checkPowerOfTwo(fromKeys.length - 1);

        int[] keys = this.keys;
        V[] values = this.values;
        int mask = this.mask;
        int from = fromKeys.length - 1;
        keys[keys.length - 1] = fromKeys[from];
        values[values.length - 1] = fromValues[from];

        while(true) {
            int existing;
            do {
                --from;
                if (from < 0) {
                    return;
                }
            } while((existing = fromKeys[from]) == 0);

            int slot;
            for(slot = this.hashKey(existing) & mask; keys[slot] != 0; slot = slot + 1 & mask) {
            }

            keys[slot] = existing;
            values[slot] = fromValues[from];
        }
    }

    protected void allocateBuffers(int arraySize) {
        assert Integer.bitCount(arraySize) == 1;

        int[] prevKeys = this.keys;
        V[] prevValues = this.values;

        try {
            int emptyElementSlot = 1;
            this.keys = new int[arraySize + emptyElementSlot];
            this.values = (V[]) new Object[arraySize + emptyElementSlot];
        } catch (OutOfMemoryError var5) {
            this.keys = prevKeys;
            this.values = prevValues;
            throw new IllegalStateException("Not enough memory to allocate buffers for rehashing");
        }

        this.resizeAt = HashContainers.expandAtCount(arraySize, this.loadFactor);
        this.mask = arraySize - 1;
    }

    protected void allocateThenInsertThenRehash(int slot, int pendingKey, V pendingValue) {
        assert this.assigned == this.resizeAt && this.keys[slot] == 0 && pendingKey != 0;

        int[] prevKeys = this.keys;
        V[] prevValues = this.values;
        this.allocateBuffers(HashContainers.nextBufferSize(this.mask + 1, this.size(), this.loadFactor));

        assert this.keys.length > prevKeys.length;

        prevKeys[slot] = pendingKey;
        prevValues[slot] = pendingValue;
        this.rehash(prevKeys, prevValues);
    }

    protected void shiftConflictingKeys(int gapSlot) {
        int[] keys = this.keys;
        V[] values = this.values;
        int mask = this.mask;
        int distance = 0;

        while(true) {
            ++distance;
            int slot = gapSlot + distance & mask;
            int existing = keys[slot];
            if (existing == 0) {
                keys[gapSlot] = 0;
                values[gapSlot] = null;
                --this.assigned;
                return;
            }

            int idealSlot = this.hashKey(existing);
            int shift = slot - idealSlot & mask;
            if (shift >= distance) {
                keys[gapSlot] = existing;
                values[gapSlot] = values[slot];
                gapSlot = slot;
                distance = 0;
            }
        }
    }

    public @NonNull Iterator<V> iterator() {
        return new IntHashMap.Iterate();
    }

    private final class Iterate implements Iterator<V> {
        int cursor;
        V next;

        private Iterate() {
            this.cursor = 0;
        }

        public boolean hasNext() {
            while(this.cursor < IntHashMap.this.values.length && (this.next = IntHashMap.this.values[this.cursor++]) == null) {
            }

            return this.next != null;
        }

        public V next() {
            if (this.next == null) {
                throw new NoSuchElementException();
            } else {
                return this.next;
            }
        }
    }
}
