package fr.krishenk.castel.managers;

import com.google.common.base.Enums;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.weather.LightningStrikeEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;
import java.util.UUID;

public class PvPManager implements Listener {
    private static final String TRIDENT_LIGHTNING_SHOOTER = "TRIDENT_LIGHTNING_SHOOTER";
    private static final PvPType PVP_TYPE;

    public PvPManager() {
        if (ReflectionUtils.supports(13))
            Bukkit.getPluginManager().registerEvents(new v1_13(), CastelPlugin.getInstance());
    }

    public static LivingEntity getKiller(EntityDeathEvent event) {
        EntityDamageEvent entityDamageEvent = event.getEntity().getLastDamageCause();
        if (entityDamageEvent instanceof EntityDamageByEntityEvent) {
            ProjectileSource shooter;
            Entity damager = ((EntityDamageByEntityEvent) entityDamageEvent).getDamager();
            if (damager instanceof Projectile && (shooter = ((Projectile)damager).getShooter()) instanceof LivingEntity) {
                return (LivingEntity) shooter;
            }
            if (damager instanceof LivingEntity) {
                return (LivingEntity) damager;
            }
        }
        return event.getEntity().getKiller();
    }

    public static boolean isPvPType(PvPType pvpType) {
        return PVP_TYPE == pvpType;
    }

    public static PvPType getPvpType() {
        return PVP_TYPE;
    }

    public static Player getDamager(Entity entity) {
        UUID owner;
        Object shooter;
        if (entity instanceof Projectile && (shooter = ((Projectile)entity).getShooter()) instanceof Player) {
            return (Player) shooter;
        }
        if (entity instanceof LightningStrike && !((List<MetadataValue>)(shooter = entity.getMetadata(TRIDENT_LIGHTNING_SHOOTER))).isEmpty()) {
            return (Player)((MetadataValue)((List<?>) shooter).get(0)).value();
        }
        if (entity instanceof Player) return (Player) entity;
        return null;
    }

    public static boolean canFight(Player damager, Player victim) {
        //
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.LOW)
    public void onFriendlyFire(EntityDamageByEntityEvent event) {
        UUID owner;
        Player victim;
        Player damager = PvPManager.getDamager(event.getDamager());
        if (damager == null) return;
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            victim = (Player) entity;
            if (!PvPManager.canFight(damager, victim)) event.setCancelled(true);
        }
    }

    static {
        String pvp = "normal";
        PVP_TYPE = Enums.getIfPresent(PvPType.class, pvp).or(PvPType.NORMAL);
    }

    public static final class v1_13 implements Listener {
        @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
        public void onLightning(LightningStrikeEvent event) {
            if (event.getCause() != LightningStrikeEvent.Cause.TRIDENT) return;
            for (Entity entity : event.getLightning().getNearbyEntities(2, 2, 2)) {
                Trident trident;
                if (!(entity instanceof Trident) || !((trident = (Trident) entity).getShooter() instanceof Player)) continue;
                event.getLightning().setMetadata(PvPManager.TRIDENT_LIGHTNING_SHOOTER, new FixedMetadataValue(CastelPlugin.getInstance(), trident.getShooter()));
                break;
            }
        }
    }

    public enum PvPType {
        DISABLED,
        DISALLOWED,
        NORMAL,
        CLAIMED,
        UNCLAIMED,
        TERRITORY,
        RELATIONAL
    }
}
