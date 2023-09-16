package fr.krishenk.castel.utils.cooldown;

import fr.krishenk.castel.CastelPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import java.util.List;
import java.util.concurrent.TimeUnit;

public class EntityCooldown {
    public static boolean add(Entity entity, String name, long time) {
        return add(entity, name, time, TimeUnit.SECONDS);
    }

    public static boolean add(Entity entity, String name, long time, TimeUnit timeUnit) {
        if (isInCooldown(entity, name)) return false;
        entity.setMetadata(name, new FixedMetadataValue(CastelPlugin.getInstance(), new CooldownContainer(timeUnit.toMillis(time), System.currentTimeMillis())));
        return true;
    }

    public static boolean isInCooldown(Entity entity, String name) {
        return getTimeLeft(entity, name) != 0L;
    }

    public static long getTimeLeft(Entity entity, String name) {
        CooldownContainer cooldown = get(entity, name);
        if (cooldown == null) return 0L;
        long now = System.currentTimeMillis();
        long difference = now - cooldown.start;
        if (difference >= cooldown.time) {
            stop(entity, name);
            return 0L;
        }
        return cooldown.time - difference;
    }

    public static CooldownContainer get(Entity entity, String name) {
        List<MetadataValue> meta = entity.getMetadata(name);
        return meta.isEmpty() ? null : (CooldownContainer) meta.get(0).value();
    }

    public static void stop(Entity entity, String name) {
        entity.removeMetadata(name, CastelPlugin.getInstance());
    }
}
