package fr.krishenk.castel.scheduler;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.dependencies.classpath.BootstrapProvider;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.time.Duration;
import java.util.concurrent.Executor;

public class BukkitSchedulerAdapter extends AbstractJavaScheduler {
    private final Executor sync;
    private final BukkitScheduler bukkitScheduler;
    private final Plugin plugin;

    public BukkitSchedulerAdapter(CastelPlugin plugin, BootstrapProvider bootstrapProvider) {
        super(bootstrapProvider);
        this.plugin = plugin;
        this.bukkitScheduler = plugin.getServer().getScheduler();
        this.sync = task -> {
            if (CastelPlugin.getInstance().isDisabling()) {
                return;
            }
            plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin.getLoader(), task);
        };
    }

    @Override
    public Executor sync() {
        return this.sync;
    }

    @Override
    public void syncLater(Runnable task, Duration startIn) {
        this.bukkitScheduler.scheduleSyncDelayedTask(this.plugin, task, TimeUtils.millisToTicks(startIn.toMillis()));
    }
}
