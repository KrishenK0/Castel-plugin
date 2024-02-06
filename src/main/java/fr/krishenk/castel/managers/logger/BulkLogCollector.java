package fr.krishenk.castel.managers.logger;

import fr.krishenk.castel.CastelPlugin;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public abstract class BulkLogCollector {
    private final long started = System.currentTimeMillis();
    private BukkitTask task;

    public long getStarted() {
        return started;
    }

    public void setTask(long delay, Runnable runnable) {
        if (this.task != null) this.task.cancel();
        this.task = Bukkit.getScheduler().runTaskLater(CastelPlugin.getInstance(), runnable, delay);
    }
}
