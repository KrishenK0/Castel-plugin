package fr.krishenk.castel.managers.land.indicator;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class LandIndicator {
    protected final Player player;
    private final BukkitTask task;

    protected LandIndicator(Player player, BukkitTask task) {
        this.player = player;
        this.task = task;
    }

    public void end() {
        if (this.task != null) this.task.cancel();
    }
}
