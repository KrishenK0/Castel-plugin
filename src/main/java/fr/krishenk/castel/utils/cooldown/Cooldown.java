package fr.krishenk.castel.utils.cooldown;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class Cooldown<T> {
    private final Map<T, CooldownContainer> cooldowns;

    public Cooldown() {
        this(new HashMap());
    }

    public Cooldown(Map<T, CooldownContainer> cooldowns) {
        this.cooldowns = cooldowns;
    }

    public boolean add(T key, long time) {
        return this.add(key, time, TimeUnit.MILLISECONDS);
    }

    public boolean add(T key, Duration duration) {
        return this.add(key, duration.toMillis(), TimeUnit.MILLISECONDS);
    }

    public boolean add(T key, long time, TimeUnit timeUnit) {
        if (this.isInCooldown(key)) {
            return false;
        }
        this.cooldowns.put(key, new CooldownContainer(timeUnit.toMillis(time), System.currentTimeMillis()));
        return true;
    }

    public boolean isInCooldown(T key) {
        return this.getTimeLeft(key) != 0L;
    }

    public CooldownContainer stop(T key) {
        return this.cooldowns.remove(key);
    }

    public CooldownContainer get(T key) {
        return this.cooldowns.get(key);
    }

    public long getTimeLeft(T key) {
        CooldownContainer cooldown = this.get(key);
        if (cooldown == null) {
            return 0L;
        }
        long now = System.currentTimeMillis();
        long difference = now - cooldown.start;
        if (difference >= cooldown.time) {
            this.stop(key);
            return 0L;
        }
        return cooldown.time - difference;
    }
}


