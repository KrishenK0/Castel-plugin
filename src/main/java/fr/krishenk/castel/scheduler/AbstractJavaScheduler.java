package fr.krishenk.castel.scheduler;

import fr.krishenk.castel.dependencies.classpath.BootstrapProvider;

import java.time.Duration;
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

public abstract class AbstractJavaScheduler implements TaskScheduler {
    private static final String PREFIX = "castel";
    private static final int PARALLELISM = 16;
    private final BootstrapProvider bootstrap;
    private final ScheduledThreadPoolExecutor scheduler;
    private final ForkJoinPool worker;

    public AbstractJavaScheduler(BootstrapProvider bootstrap) {
        this.bootstrap = bootstrap;
        this.scheduler = new ScheduledThreadPoolExecutor(1, r -> {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName("castel-scheduler");
            return thread;
        });
        this.scheduler.setRemoveOnCancelPolicy(true);
        this.scheduler.setExecuteExistingDelayedTasksAfterShutdownPolicy(false);
        this.worker = new ForkJoinPool(16, new WorkerThreadFactory(), new ExceptionHandler(), false);
    }

    @Override
    public Executor async() {
        return this.worker;
    }

    @Override
    public ScheduledTask asyncLater(Duration delay, Runnable task) {
        ScheduledFuture<?> future = this.scheduler.schedule(() -> this.worker.execute(task), delay.toMillis(), TimeUnit.MILLISECONDS);
        return () -> future.cancel(false);
    }

    @Override
    public ScheduledTask asyncRepeating(Duration initialDelay, Duration interval, Runnable task) {
        ScheduledFuture<?> future = this.scheduler.scheduleAtFixedRate(() -> this.worker.execute(task), initialDelay.toMillis(), interval.toMillis(), TimeUnit.MILLISECONDS);
        return () -> future.cancel(false);
    }

    @Override
    public void shutdownScheduler() {
        this.scheduler.shutdown();
        try {
            if (!this.scheduler.awaitTermination(1L, TimeUnit.MINUTES)) {
                this.bootstrap.getLogger().severe("Timed out waiting for the scheduler to terminate");
                this.reportRunningTasks(thread -> thread.getName().equals("castel-scheduler"));
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void shutdownExecutor() {
        this.worker.shutdown();
        try {
            if (!this.worker.awaitTermination(1L, TimeUnit.MINUTES)) {
                this.bootstrap.getLogger().severe("Timed out waiting for the worker thread pool to terminate");
                this.reportRunningTasks(thread -> thread.getName().startsWith("castel-worker-"));
            }
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void reportRunningTasks(Predicate<Thread> predicate) {
        Thread.getAllStackTraces().forEach((thread, stack) -> {
            if (predicate.test((Thread)thread)) {
                this.bootstrap.getLogger().log(Level.WARNING, "Thread " + thread.getName() + " is blocked, and may be the reason for the slow shutdown!\n" + Arrays.stream(stack).map(el -> "  " + el).collect(Collectors.joining("\n")));
            }
        });
    }

    private static final class WorkerThreadFactory
            implements ForkJoinPool.ForkJoinWorkerThreadFactory {
        private static final AtomicInteger COUNT = new AtomicInteger(0);

        private WorkerThreadFactory() {
        }

        @Override
        public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
            ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
            thread.setDaemon(true);
            thread.setName("castel-worker-" + COUNT.getAndIncrement());
            return thread;
        }
    }

    private final class ExceptionHandler
            implements Thread.UncaughtExceptionHandler {
        private ExceptionHandler() {
        }

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            AbstractJavaScheduler.this.bootstrap.getLogger().log(Level.WARNING, "Thread " + t.getName() + " threw an uncaught exception", e);
        }
    }
}
