package fr.krishenk.castel.managers.abstraction;

import java.time.Duration;
import java.time.LocalTime;
import java.util.List;

public abstract class ProlongedDurationTask extends ProlongedTask {
    protected long started;

    public long getStarted() {
        return started;
    }

    public ProlongedDurationTask(long interval, String fixedPerformTime, String taskName, String[] lastPerformConfigPath, List<String> countdown) {
        super(interval, fixedPerformTime, taskName, lastPerformConfigPath, countdown);
    }

    public ProlongedDurationTask(Duration interval, LocalTime fixedPerformTime, String taskName, String[] lastPerformConfigPath, List<String> countdown) {
        super(interval, fixedPerformTime, taskName, lastPerformConfigPath, countdown);
    }

    public abstract void stop();

    public boolean isRunning() {
        return this.started != 0L;
    }

    public abstract Duration getDuration();

    public Duration getTimeLeftUntilStop() {
        if (this.started == 0L) {
            throw new IllegalStateException("Task hasn't started yet");
        } else {
            long diff = System.currentTimeMillis() - this.started;
            Duration left = this.getDuration().minusMillis(diff);

            return left.isNegative() ? Duration.ZERO : left;
        }
    }
}
