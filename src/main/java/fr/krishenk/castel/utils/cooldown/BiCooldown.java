package fr.krishenk.castel.utils.cooldown;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class BiCooldown<T, S> {
    private final Map<T, Map<S, CooldownContainer>> cooldowns;
    private final Supplier<Map<S, CooldownContainer>> biConstructor;

    public BiCooldown() {
        this(null);
    }

    public BiCooldown(Supplier<Map<S, CooldownContainer>> biConstructor) {
        this(new HashMap<>(), biConstructor);
    }

    public BiCooldown(Map<T, Map<S, CooldownContainer>> cooldowns, Supplier<Map<S, CooldownContainer>> biConstructor) {
        this.cooldowns = cooldowns;
        this.biConstructor = biConstructor;
    }

    public boolean add(T key, S secondKey, long time) {
        return this.add(key, secondKey, time, TimeUnit.MILLISECONDS);
    }

    public boolean add(T key, S secondKey, long time, TimeUnit unit) {
        if (this.isInCooldown(key, secondKey)) return false;
        this.cooldowns.compute(key, (k, v) -> {
            if (v == null) v = this.biConstructor == null ? new HashMap<>() : this.biConstructor.get();
            v.put(secondKey, new CooldownContainer(unit.toMillis(time), System.currentTimeMillis()));
            return v;
        });
        return true;
    }

    public boolean isInCooldown(T key, S secondKey) {
        return this.getTimeLeft(key, secondKey) != 0L;
    }

    public CooldownContainer stop(T key, S secondKey) {
        Map<S, CooldownContainer> innerMap = this.cooldowns.get(key);
        if (innerMap == null) return null;
        CooldownContainer map = innerMap.remove(secondKey);
        if (map != null && innerMap.isEmpty()) this.cooldowns.remove(key);
        return map;
    }

    public CooldownContainer get(T key, S secondKey) {
        Map<S, CooldownContainer> first = this.cooldowns.get(key);
        return first == null ? null : first.get(secondKey);
    }

    public long getTimeLeft(T key, S secondKey) {
        CooldownContainer cooldown = this.get(key, secondKey);
        if (cooldown == null) return 0L;
        long now = System.currentTimeMillis();
        long difference = now - cooldown.start;
        if (difference >= cooldown.time) {
            this.stop(key, secondKey);
            return 0L;
        }
        return cooldown.time - difference;
    }


}
