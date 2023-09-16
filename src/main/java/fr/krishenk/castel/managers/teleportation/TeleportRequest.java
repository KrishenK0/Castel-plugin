package fr.krishenk.castel.managers.teleportation;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class TeleportRequest {
    private final Player teleporter;
    private final Player target;
    private final long sentTime = System.currentTimeMillis();
    private final BukkitTask task;

    public TeleportRequest(Player teleporter, Player target, BukkitTask task) {
        this.teleporter = teleporter;
        this.target = target;
        this.task = task;
    }

    public BukkitTask getTask() {
        return task;
    }

    public void cancel() {
        if (this.task != null) {
            this.task.cancel();
        }
    }

    public Player getTarget() {
        return target;
    }

    public long getSenTime() {
        return sentTime;
    }

    public Player getTeleporter() {
        return teleporter;
    }
}
