package fr.krishenk.castel.utils.internal.integer;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class IntHashSet implements Iterable<IntHashSet.IntCursor> {
    public int[] keys;
    protected int assigned;
    protected int mask;
    protected int resizeAt;
    protected boolean hasEmptyKey;
    protected double loadFactor;
    protected int iterationSeed;

    public IntHashSet() {
        this(4, 0.75);
    }

    public IntHashSet(int expectedElements) {
        this(expectedElements, 0.75);
    }

    public IntHashSet(int expectedElements, double loadFactor) {
        this.loadFactor = this.verifyLoadFactor(loadFactor);
        this.iterationSeed = HashContainers.nextIterationSeed();
        this.ensureCapacity(expectedElements);
    }

    public String toString() {
        return Arrays.toString(this.toArray());
    }

    public boolean add(int key) {
        int existing;
        if (key == 0) {
            assert (this.keys[this.mask + 1] == 0);
            boolean added = !this.hasEmptyKey;
            this.hasEmptyKey = true;
            return added;
        }
        int[] keys = this.keys;
        int mask = this.mask;
        int slot = this.hashKey(key) & mask;
        while ((existing = keys[slot]) != 0) {
            if (existing == key) {
                return false;
            }
            slot = slot + 1 & mask;
        }
        if (this.assigned == this.resizeAt) {
            this.allocateThenInsertThenRehash(slot, key);
        } else {
            keys[slot] = key;
        }
        ++this.assigned;
        return true;
    }

    public int[] toArray() {
        int[] cloned = new int[this.size()];
        int j = 0;
        if (this.hasEmptyKey) {
            cloned[j++] = 0;
        }
        int[] keys = this.keys;
        int seed = this.nextIterationSeed();
        int inc = HashContainers.iterationIncrement(seed);
        int mask = this.mask;
        int slot = seed & mask;
        for (int i = 0; i <= mask; ++i) {
            int existing = keys[slot];
            if (existing != 0) {
                cloned[j++] = existing;
            }
            slot = slot + inc & mask;
        }
        return cloned;
    }

    public boolean remove(int key) {
        int existing;
        if (key == 0) {
            boolean hadEmptyKey = this.hasEmptyKey;
            this.hasEmptyKey = false;
            return hadEmptyKey;
        }
        int[] keys = this.keys;
        int mask = this.mask;
        int slot = this.hashKey(key) & mask;
        while ((existing = keys[slot]) != 0) {
            if (existing == key) {
                this.shiftConflictingKeys(slot);
                return true;
            }
            slot = slot + 1 & mask;
        }
        return false;
    }

    public boolean contains(int key) {
        int existing;
        if (key == 0) {
            return this.hasEmptyKey;
        }
        if (this.size() == 0) {
            return false;
        }
        int[] keys = this.keys;
        int mask = this.mask;
        int slot = this.hashKey(key) & mask;
        while ((existing = keys[slot]) != 0) {
            if (existing == key) {
                return true;
            }
            slot = slot + 1 & mask;
        }
        return false;
    }

    public void clear() {
        this.assigned = 0;
        this.hasEmptyKey = false;
        Arrays.fill(this.keys, 0);
    }

    public void release() {
        this.assigned = 0;
        this.hasEmptyKey = false;
        this.keys = null;
        this.ensureCapacity(4);
    }

    public boolean isEmpty() {
        return this.size() == 0;
    }

    public void ensureCapacity(int expectedElements) {
        if (expectedElements > this.resizeAt || this.keys == null) {
            int[] prevKeys = this.keys;
            this.allocateBuffers(HashContainers.minBufferSize(expectedElements, this.loadFactor));
            if (prevKeys != null && !this.isEmpty()) {
                this.rehash(prevKeys);
            }
        }
    }

    public int size() {
        return this.assigned + (this.hasEmptyKey ? 1 : 0);
    }

    public int hashCode() {
        throw new UnsupportedOperationException();
    }

    public boolean equals(Object obj) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<IntCursor> iterator() {
        return new EntryIterator();
    }

    protected int nextIterationSeed() {
        this.iterationSeed = HashContainers.mixPhi(this.iterationSeed);
        return this.iterationSeed;
    }

    protected int hashKey(int key) {
        assert (key != 0);
        return HashContainers.mixPhi(key);
    }

    public int indexOf(int key) {
        int existing;
        int mask = this.mask;
        if (key == 0) {
            return this.hasEmptyKey ? mask + 1 : ~(mask + 1);
        }
        int[] keys = this.keys;
        int slot = this.hashKey(key) & mask;
        while ((existing = keys[slot]) != 0) {
            if (existing == key) {
                return slot;
            }
            slot = slot + 1 & mask;
        }
        return ~slot;
    }

    protected double verifyLoadFactor(double loadFactor) {
        HashContainers.checkLoadFactor(loadFactor, 0.01f, 0.99f);
        return loadFactor;
    }

    protected void rehash(int[] fromKeys) {
        assert (HashContainers.checkPowerOfTwo(fromKeys.length - 1));
        int[] keys = this.keys;
        int mask = this.mask;
        int i = fromKeys.length - 1;
        while (--i >= 0) {
            int existing = fromKeys[i];
            if (existing == 0) continue;
            int slot = this.hashKey(existing) & mask;
            while (keys[slot] != 0) {
                slot = slot + 1 & mask;
            }
            keys[slot] = existing;
        }
    }

    protected void allocateBuffers(int arraySize) {
        assert (Integer.bitCount(arraySize) == 1);
        int[] prevKeys = this.keys;
        try {
            int emptyElementSlot = 1;
            this.keys = new int[arraySize + emptyElementSlot];
        }
        catch (OutOfMemoryError e) {
            this.keys = prevKeys;
            throw new IllegalStateException("Not enough memory to allocate buffers for rehashing");
        }
        this.resizeAt = HashContainers.expandAtCount(arraySize, this.loadFactor);
        this.mask = arraySize - 1;
    }

    protected void allocateThenInsertThenRehash(int slot, int pendingKey) {
        assert (this.assigned == this.resizeAt && this.keys[slot] == 0 && pendingKey != 0);
        int[] prevKeys = this.keys;
        this.allocateBuffers(HashContainers.nextBufferSize(this.mask + 1, this.size(), this.loadFactor));
        assert (this.keys.length > prevKeys.length);
        prevKeys[slot] = pendingKey;
        this.rehash(prevKeys);
    }

    protected void shiftConflictingKeys(int gapSlot) {
        int slot;
        int existing;
        int[] keys = this.keys;
        int mask = this.mask;
        int distance = 0;
        while ((existing = keys[slot = gapSlot + ++distance & mask]) != 0) {
            int idealSlot = this.hashKey(existing);
            int shift = slot - idealSlot & mask;
            if (shift < distance) continue;
            keys[gapSlot] = existing;
            gapSlot = slot;
            distance = 0;
        }
        keys[gapSlot] = 0;
        --this.assigned;
    }

    protected final class EntryIterator
            implements Iterator<IntCursor> {
        private static final int NOT_CACHED = 0;
        private static final int CACHED = 1;
        private static final int AT_END = 2;
        private final IntCursor cursor = new IntCursor();
        private final int increment;
        private int index;
        private int slot;
        private int state = 0;
        private IntCursor nextElement;

        public EntryIterator() {
            int seed = IntHashSet.this.nextIterationSeed();
            this.increment = HashContainers.iterationIncrement(seed);
            this.slot = seed & IntHashSet.this.mask;
        }

        @Override
        public boolean hasNext() {
            if (this.state == 0) {
                this.state = 1;
                this.nextElement = this.fetch();
            }
            return this.state == 1;
        }

        @Override
        public IntCursor next() {
            if (!this.hasNext()) {
                throw new NoSuchElementException();
            }
            this.state = 0;
            return this.nextElement;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        protected final IntCursor done() {
            this.state = 2;
            return null;
        }

        protected IntCursor fetch() {
            int mask = IntHashSet.this.mask;
            while (this.index <= mask) {
                ++this.index;
                this.slot = this.slot + this.increment & mask;
                int existing = IntHashSet.this.keys[this.slot];
                if (existing == 0) continue;
                this.cursor.index = this.slot;
                this.cursor.value = existing;
                return this.cursor;
            }
            if (this.index == mask + 1 && IntHashSet.this.hasEmptyKey) {
                this.cursor.index = this.index++;
                this.cursor.value = 0;
                return this.cursor;
            }
            return this.done();
        }
    }

    public static final class IntCursor {
        public int index;
        public int value;

        public String toString() {
            return "[cursor, index: " + this.index + ", value: " + this.value + "]";
        }
    }
}

