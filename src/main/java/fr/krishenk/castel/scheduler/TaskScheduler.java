package fr.krishenk.castel.scheduler;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

public interface TaskScheduler {
    public Executor async();

    public Executor sync();

    default public void executeAsync(Runnable task) {
        this.async().execute(task);
    }

    default public void executeSync(Runnable task) {
        this.sync().execute(task);
    }

    default public <T> CompletableFuture<T> supplyFuture(Callable<T> callable) {
        Objects.requireNonNull(callable, "callable");
        return CompletableFuture.supplyAsync(() -> {
            try {
                return callable.call();
            }
            catch (Exception e) {
                if (e instanceof RuntimeException) {
                    throw (RuntimeException)e;
                }
                throw new CompletionException(e);
            }
        }, this.async());
    }

    default public CompletableFuture<Void> runFuture(Runnable runnable) {
        Objects.requireNonNull(runnable, "runnable");
        return CompletableFuture.runAsync(runnable, this.async());
    }

   public void syncLater(Runnable var1, Duration var2);

    public ScheduledTask asyncLater(Duration var1, Runnable var2);

    public ScheduledTask asyncRepeating(Duration var1, Duration var2, Runnable var3);

    public void shutdownScheduler();

    public void shutdownExecutor();
}
