package fr.krishenk.castel.utils.internal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import fr.krishenk.castel.utils.cache.CacheHandler;
import org.apache.commons.lang.Validate;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class ExpirableSet<K> {
    private final Cache<K, Long> map;
    private final long duration;

    public ExpirableSet(long duration, TimeUnit unit, boolean expireAfterAccess) {
        Validate.isTrue(duration > 0L, "Expiration duration must be greater than 0 got : " + duration);
        Objects.requireNonNull(unit, "Expiration time unit cannot be null");
        Caffeine<Object, Object> builder = CacheHandler.newBuilder();
        this.map = (expireAfterAccess ? builder.expireAfterAccess(duration, unit) : builder.expireAfterWrite(duration, unit)).build();
        this.duration = unit.toMillis(duration);
    }

    public boolean add(K key) {
        if (this.contains(key)) {
            return false;
        }
        this.map.put(key, System.currentTimeMillis());
        return true;
    }

    public long getTimeLeft(K key) {
        Long added = this.map.getIfPresent(key);
        if (added == null) {
            return 0L;
        }
        long passed = System.currentTimeMillis() - added;
        long left = this.duration - passed;
        return left <= 0L ? 0L : left;
    }

    public void clear() {
        this.map.invalidateAll();
    }

    public boolean contains(K key) {
        return this.map.getIfPresent(key) != null;
    }

    public void remove(K key) {
        this.map.invalidate(key);
    }
}
