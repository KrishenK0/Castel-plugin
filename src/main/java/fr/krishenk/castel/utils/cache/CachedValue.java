package fr.krishenk.castel.utils.cache;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.Objects;
import java.util.function.Supplier;

public class CachedValue<T> implements Supplier<T> {
    @NotNull
    private final Supplier<T> getter;
    @NotNull
    private final Duration cacheTime;
    private long lastChecked;
    @Nullable
    private T cached;

    public CachedValue(@NotNull Supplier<T> getter, @NotNull Duration cacheTime) {
        this.getter = getter;
        this.cacheTime = cacheTime;
        this.lastChecked = System.currentTimeMillis();
        if (this.cacheTime.getSeconds() <= 5L) {
            throw new IllegalArgumentException("Any cache time under 5 seconds is not likely to help with performance: " + this.cacheTime.toMillis() + "ms");
        }
    }

    public final boolean contains(T obj) {
        return Objects.equals(this.get(), obj);
    }

    @Override
    public T get() {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - this.lastChecked;
        if (this.cached == null || this.cacheTime.minusMillis(diff).isNegative()) {
            this.cached = this.getter.get();
            this.lastChecked = currentTime;
        }
        return this.cached;
    }
}

