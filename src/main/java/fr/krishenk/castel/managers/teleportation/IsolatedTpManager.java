package fr.krishenk.castel.managers.teleportation;

import fr.krishenk.castel.utils.internal.integer.IntHashMap;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class IsolatedTpManager {
    private final IntHashMap<TeleportTask> teleporting = new IntHashMap<>();

    public IsolatedTpManager() {}

    public void put(Player player, BukkitTask task) {
        this.put(new TeleportTask(player, task));
    }

    public void put(TeleportTask task) {
        TpManager.put(task);
        TeleportTask previous = this.teleporting.put(task.player.getEntityId(), task);
        if (previous != null) previous.task.cancel();
    }

    public boolean isTeleporting(Entity entity) {
        return this.teleporting.containsKey(entity.getEntityId());
    }

    public Iterable<TeleportTask> getTasks() {
        return this.teleporting;
    }

    public boolean end(Entity entity) {
        TpManager.end(entity);
        TeleportTask task = this.teleporting.remove(entity.getEntityId());
        if (task != null) task.task.cancel();
        return task != null;
    }
}
