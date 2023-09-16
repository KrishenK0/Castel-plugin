package fr.krishenk.castel.scheduler;

import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

public class AsyncScheduledTasks {
    private static final Map<BukkitTask, Runnable> TASKS = new WeakHashMap<BukkitTask, Runnable>();

    private AsyncScheduledTasks() {
    }

    public static void addTask(BukkitTask task, Runnable runnable) {
        Objects.requireNonNull(task);
        Objects.requireNonNull(runnable);
        if (TASKS.put(task, runnable) != null) {
            throw new IllegalArgumentException("Task was already added");
        }
    }

    public static Map<BukkitTask, Runnable> getTasks() {
        return TASKS;
    }
}
