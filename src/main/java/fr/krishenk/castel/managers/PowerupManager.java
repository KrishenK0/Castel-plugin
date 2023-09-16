package fr.krishenk.castel.managers;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.upgradable.Powerup;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.projectiles.ProjectileSource;

import java.util.List;

public class PowerupManager implements Listener {
    private static final String PROJECTILE_SOURCE_META = "PROJECTILE_SOURCE";

    private static SimpleChunkLocation getProjectileLocation(Powerup powerup, Player damager, Entity projectile) {
        if (powerup == Powerup.ARROW_BOOST) {
            List<MetadataValue> test = projectile.getMetadata(PROJECTILE_SOURCE_META);
            return test.isEmpty() ? SimpleChunkLocation.of(damager.getLocation()) : SimpleChunkLocation.of((Location) test.get(0).value());
        }
        return SimpleChunkLocation.of(damager.getLocation());
    }

    public static void onPlayerDeathPowerLoss(Player player, CastelPlayer cp) {
        if (Config.Powers.POWER_ENABLED.getManager().getBoolean()) {
            double amount = Config.Powers.POWER_PLAYER_LOSS_DEATH.getManager().getDouble();
            if (!(amount <= 0.0)) {
                double previous = cp.getPower();
                cp.addPower(-amount);
                Lang.POWER_DEATH.sendError(player, "lost", amount, "previous", previous);
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerShootEvent(ProjectileLaunchEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile.getShooter() instanceof Player) {
            Player shooter = (Player) projectile.getShooter();
            projectile.setMetadata(PROJECTILE_SOURCE_META, new FixedMetadataValue(CastelPlugin.getInstance(), shooter.getLocation()));
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onDamage(EntityDamageByEntityEvent event) {
        double damage = event.getDamage();
        Player damager;
        CastelPlayer cp;
        Guild guild;
        if (event.getEntity() instanceof Player && Powerup.DAMAGE_REDUCTION.isEnabled()) {
            damager = (Player) event.getEntity();
            cp = CastelPlayer.getCastelPlayer(damager);
            guild = cp.getGuild();
            if (guild != null) {
                int lvl = guild.getUpgradeLevel(Powerup.DAMAGE_REDUCTION);
                if (lvl > 0 && (!Powerup.DAMAGE_REDUCTION.isOwnLandOnly() || guild.isClaimed(SimpleChunkLocation.of(damager.getLocation())))) {
                    damage -= Powerup.DAMAGE_REDUCTION.getScaling(new PlaceholderContextBuilder().withContext(guild).raw("lvl", lvl).raw("damage", damage));
                }
            }
        }

        damager = null;
        if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            ProjectileSource shooter = projectile.getShooter();
            if (shooter instanceof Player) {
                damager = (Player) shooter;
            }
        } else if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        }

        if (damager != null) {
            cp = CastelPlayer.getCastelPlayer(damager);
            guild = cp.getGuild();
            if (guild != null) {
                Powerup powerup = event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE ? Powerup.ARROW_BOOST : Powerup.DAMAGE_BOOST;
                if (powerup.isEnabled()) {
                    int lvl = guild.getUpgradeLevel(powerup);
                    if (lvl > 0 && (!powerup.isOwnLandOnly() || guild.isClaimed(getProjectileLocation(powerup, damager, event.getDamager())))) {
                        damage += powerup.getScaling(new PlaceholderContextBuilder().withContext(guild).raw("lvl", lvl).raw("damage", event.getDamage()));
                    }
                }
            }
        }

        event.setDamage(Math.max(0, damage));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onRegen(EntityRegainHealthEvent event) {
        if (event.getRegainReason() == EntityRegainHealthEvent.RegainReason.EATING) {
            if (event.getEntity() instanceof Player && Powerup.REGENERATION_BOOST.isEnabled()) {
                Player damager = (Player) event.getEntity();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(damager);
                Guild guild = cp.getGuild();
                if (guild != null) {
                    int lvl = guild.getUpgradeLevel(Powerup.REGENERATION_BOOST);
                    if (lvl > 0 && (!Powerup.REGENERATION_BOOST.isOwnLandOnly() || guild.isClaimed(SimpleChunkLocation.of(damager.getLocation())))) {
                        double amount = Powerup.REGENERATION_BOOST.getScaling(new PlaceholderContextBuilder().withContext(guild).raw("lvl", lvl).raw("amount", event.getAmount()));
                        event.setAmount(event.getAmount() + amount);
                    }
                }
            }
        }
    }
}
