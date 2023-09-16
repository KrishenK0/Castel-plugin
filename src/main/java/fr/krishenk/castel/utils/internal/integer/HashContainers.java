package fr.krishenk.castel.utils.internal.integer;

import java.util.concurrent.atomic.AtomicInteger;

public class HashContainers {
    public static final int MAX_HASH_ARRAY_LENGTH = 0x40000000;
    public static final int MIN_HASH_ARRAY_LENGTH = 4;
    public static final float DEFAULT_LOAD_FACTOR = 0.75f;
    public static final float MIN_LOAD_FACTOR = 0.01f;
    public static final float MAX_LOAD_FACTOR = 0.99f;
    public static final int DEFAULT_EXPECTED_ELEMENTS = 4;
    private static final AtomicInteger ITERATION_SEED = new AtomicInteger();
    private static final long PHI_C64 = -7046029254386353131L;

    HashContainers() {
    }

    static int minBufferSize(int elements, double loadFactor) {
        if (elements < 0) {
            throw new IllegalArgumentException("Number of elements must be >= 0: " + elements);
        }
        long length = (long)Math.ceil((double)elements / loadFactor);
        if (length == (long)elements) {
            ++length;
        }
        if ((length = Math.max(4L, HashContainers.nextHighestPowerOfTwo(length))) > 0x40000000L) {
            throw new IllegalArgumentException("Maximum array size exceeded for this load factor");
        }
        return (int)length;
    }

    static int nextBufferSize(int arraySize, int elements, double loadFactor) {
        assert (HashContainers.checkPowerOfTwo(arraySize));
        if (arraySize == 0x40000000) {
            throw new IllegalArgumentException("Maximum array size exceeded for this load factor");
        }
        return arraySize << 1;
    }

    public static int mixPhi(double k) {
        long h = Double.doubleToLongBits(k) * -7046029254386353131L;
        return (int)(h ^ h >>> 32);
    }

    public static int nextHighestPowerOfTwo(int v) {
        --v;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        return ++v;
    }

    public static long nextHighestPowerOfTwo(long v) {
        --v;
        v |= v >> 1;
        v |= v >> 2;
        v |= v >> 4;
        v |= v >> 8;
        v |= v >> 16;
        v |= v >> 32;
        return ++v;
    }

    static int expandAtCount(int arraySize, double loadFactor) {
        assert (HashContainers.checkPowerOfTwo(arraySize));
        return Math.min(arraySize - 1, (int)Math.ceil((double)arraySize * loadFactor));
    }

    static void checkLoadFactor(double loadFactor, double minAllowedInclusive, double maxAllowedInclusive) {
        if (loadFactor < minAllowedInclusive || loadFactor > maxAllowedInclusive) {
            throw new IllegalArgumentException("The load factor should be in range");
        }
    }

    static boolean checkPowerOfTwo(int arraySize) {
        assert (arraySize > 1);
        assert (HashContainers.nextHighestPowerOfTwo(arraySize) == arraySize);
        return true;
    }

    static int nextIterationSeed() {
        return ITERATION_SEED.incrementAndGet();
    }

    static int iterationIncrement(int seed) {
        return 29 + ((seed & 7) << 1);
    }

    public static long mix64(long z) {
        z = (z ^ z >>> 32) * 5536775847593249645L;
        z = (z ^ z >>> 29) * -282946459933713943L;
        return z ^ z >>> 32;
    }
}


