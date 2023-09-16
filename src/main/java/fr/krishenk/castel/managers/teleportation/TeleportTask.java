package fr.krishenk.castel.managers.teleportation;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.abstraction.MoveSensitiveAction;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.function.Consumer;

public class TeleportTask extends MoveSensitiveAction {
    public final Player player;
    public final BukkitTask task;

    public TeleportTask(Player player, BukkitTask task) {
        this.player = player;
        this.task = task;
    }

    public TeleportTask assignBasicMoveChecks() {
        this.onAnyMove((p) -> {
            Lang.TELEPORTS_MOVED.sendError(player);
            return true;
        });
        return this;
    }

    public static TeleportTask timer(Player player, int timer, Consumer<BukkitRunnable> onCountdown, Runnable onFinish) {
        BukkitTask task = (new BukkitRunnable() {
            int timed = timer;

            @Override
            public void run() {
                if (this.timed <= 0) {
                    onFinish.run();
                    TpManager.end(player);
                    this.cancel();
                } else {
                    onCountdown.accept(this);
                    --this.timed;
                }
            }
        }).runTaskTimer(CastelPlugin.getInstance(), 0L, 20L);
        return new TeleportTask(player, task);
    }
}
