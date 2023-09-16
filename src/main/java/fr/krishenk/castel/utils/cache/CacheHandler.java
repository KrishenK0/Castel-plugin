package fr.krishenk.castel.utils.cache;

import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ScheduledExecutorService;

public class CacheHandler {
    private static final ForkJoinPool POOL = new ForkJoinPool();

    public static Caffeine<Object, Object> newBuilder() {
        return Caffeine.newBuilder().executor(POOL);
    }

    public static ForkJoinPool getPool() {
        return POOL;
    }

    public static ScheduledExecutorService newSheduler() {
        return Executors.newSingleThreadScheduledExecutor(Executors.defaultThreadFactory());
    }
}
