package fr.krishenk.castel.managers.teleportation;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.internal.integer.IntHashMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

public class TpManager implements Listener {
    private static final IntHashMap<TeleportTask> TELEPORTING = new IntHashMap<>();

    public TpManager() {}

    public static void put(Player player, BukkitTask task) {
        put(new TeleportTask(player, task));
    }

    public static void put(TeleportTask task) {
        TeleportTask previous = TELEPORTING.put(task.player.getEntityId(), task);
        if (previous != null) previous.task.cancel();
    }

    public static boolean end(Entity entity) {
        TeleportTask task = TELEPORTING.remove(entity.getEntityId());
        if (task != null) task.task.cancel();
        return task != null;
    }

    public static void cancelAll(Collection<Entity> entities) {
        for (Entity entity : entities) {
            end(entity);
        }
    }

    public static boolean isTeleporting(Entity entity) {
        return TELEPORTING.containsKey(entity.getEntityId());
    }

    public static boolean alreadyTping(Player player) {
        if (isTeleporting(player)) {
            Lang.TELEPORTS_ALREADY_TELEPORTING.sendError(player);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        TELEPORTING.remove(event.getPlayer().getEntityId());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            TeleportTask task = TELEPORTING.remove(event.getEntity().getEntityId());
            if (task != null && task.onDamage != null) {
                task.onDamage.apply(event);
                task.task.cancel();
            }
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMove(PlayerMoveEvent event) {
        Bukkit.getScheduler().runTaskAsynchronously(CastelPlugin.getInstance(), () -> {
            if (LocationUtils.hasMoved(event.getFrom(), event.getTo())) {
                TeleportTask task = TELEPORTING.remove(event.getPlayer().getEntityId());
                if (task != null && task.onMove != null) {
                    task.onMove.apply(event);
                    task.task.cancel();
                }
            }
        });
    }
}
