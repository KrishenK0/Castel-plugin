package fr.krishenk.castel.managers.abstraction;

import com.google.common.base.Strings;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.config.managers.NewConfigManager;
import fr.krishenk.castel.data.StartupCache;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.managers.daily.DailyChecksManager;
import fr.krishenk.castel.managers.daily.ElectionsManager;
import fr.krishenk.castel.managers.daily.TimeZoneHandler;
import fr.krishenk.castel.managers.logger.CastelLogger;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.time.TimeFormatter;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public abstract class ProlongedTask {
    private static final long ONE_DAY_TICKS = TimeUnit.DAYS.toSeconds(1L) * 20L;
    private final Duration interval;
    private final LocalTime fixedPerformTime;
    private final String taskName;
    private final String[] lastPerformConfigPath;
    private final long[] countdowns;
    private boolean skip;
    private BukkitTask task;
    private BukkitTask reminder;

    protected ProlongedTask(long interval, String fixedPerformTime, String taskName, String[] lastPerformConfigPath, List<String> countdown) {
        Objects.requireNonNull(interval, taskName + " interval cannot be null");
        this.fixedPerformTime = Strings.isNullOrEmpty(fixedPerformTime) ? null : TimeZoneHandler.parseLocalTime(fixedPerformTime);
        if (fixedPerformTime != null) {
            if (interval % Duration.ofDays(1L).toMillis() != 0L) {
                CLogger.error("The interval of " + taskName + " must be in days: " + interval + " -> " + Duration.ofMillis(interval).toDays());
                this.interval = Duration.ofDays(1L);
            } else this.interval = Duration.ofMillis(interval);
        } else this.interval = Duration.ofMillis(interval);

        this.taskName = taskName;
        this.lastPerformConfigPath = lastPerformConfigPath;
        this.countdowns = countdown == null ? null : parseCountdowns(countdown);
    }

    public ProlongedTask(Duration interval, LocalTime fixedPerformTime, String taskName, String[] lastPerformConfigPath, List<String> countdown) {
        this.interval = interval;
        this.fixedPerformTime = fixedPerformTime;
        this.taskName = taskName;
        this.lastPerformConfigPath = lastPerformConfigPath;
        this.countdowns = countdown == null ? null : parseCountdowns(countdown);
    }

    public static void init() {
        StartupCache.whenLoaded((x) -> {
            DailyChecksManager.getInstance().load();
            if (Config.DAILY_CHECKS_ELECTIONS_ENABLED.getBoolean())
                ElectionsManager.getInstance().load();
        });
    }

    public void runAndRenew() {
        CastelLogger.getMain().log("Force running " + this.taskName + " interval checks...");
        this.task.cancel();
        if (this.reminder != null) this.reminder.cancel();
        this.runAndSet();
        this.load();
        CastelLogger.getMain().log("Done");
    }

    private static long[] parseCountdowns(List<String> countdown) {
        return countdown.stream().map(TimeUtils::parseTime).filter(Objects::nonNull).mapToLong((x) -> x).toArray();
    }

    public abstract void run();

    public abstract void remind(String str);

    public boolean isSkipping() {
        return this.skip;
    }

    public void setSkipped(boolean skip) {
        this.skip = skip;
    }

    public long untilNextChecks(ChronoUnit unit) {
        LocalDateTime now = LocalDateTime.now(TimeZoneHandler.SERVER_TIME_ZONE);
        LocalDateTime lastPerform = this.lastPerform();
        if (this.fixedPerformTime == null) {
            if (lastPerform == null) return now.until(now.plus(this.interval), unit);
            return lastPerform.plus(this.interval).isBefore(now) ? 0L : now.until(lastPerform.plus(this.interval), unit);
        } else if ( lastPerform == null) {
            return now.until(LocalDateTime.of(now.toLocalDate(), this.fixedPerformTime), unit);
        }
        LocalDateTime runAt = LocalDateTime.of(lastPerform.plus(this.interval).toLocalDate(), this.fixedPerformTime);
        return now.isBefore(runAt) ? now.until(runAt, unit) : TimeZoneHandler.convert(unit, TimeUnit.DAYS, 1L) - this.fixedPerformTime.until(now, unit);
    }

    public LocalDateTime lastPerform() {
        long millis = NewConfigManager.getGlobals().getLong(StringUtils.join(this.lastPerformConfigPath, "."));
        return millis <= 0L ? null : LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), TimeZoneHandler.SERVER_TIME_ZONE);
    }

    public long getIntervalTicks() {
        return this.fixedPerformTime == null ? this.interval.toMillis() : ONE_DAY_TICKS;
    }

    public void runAndSet() {
        NewConfigManager.getGlobals().set(StringUtils.join(this.lastPerformConfigPath, "."), System.currentTimeMillis());
        try {
            NewConfigManager.getGlobalsAdapter().save("global.yml");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.run();
    }

    public void load() {
        long next = this.untilNextChecks(ChronoUnit.MILLIS);
        this.task = (new BukkitRunnable() {
            @Override
            public void run() {
                if (!ProlongedTask.this.isSkipping()) ProlongedTask.this.runAndSet();
                else ProlongedTask.this.skip = false;
            }
        }).runTaskTimerAsynchronously(CastelPlugin.getInstance(), TimeUtils.millisToTicks(next), this.getIntervalTicks());
        this.remind(CastelPlugin.getInstance());
    }

    private void remind(CastelPlugin plugin) {
        if (this.countdowns != null && !this.skip) {
            long nextCheck = this.untilNextChecks(ChronoUnit.MILLIS);
            Long millisUntilNextCooldown = null;
            for (int i = 0; i < this.countdowns.length; i++) {
                if (this.countdowns[i] < nextCheck) {
                    millisUntilNextCooldown = nextCheck - this.countdowns[i];
                    break;
                }
            }

            if (millisUntilNextCooldown != null) {
                CastelLogger.getMain().log("Triggered reminder. Next: " + TimeFormatter.ofRaw(millisUntilNextCooldown));
                this.reminder = (new BukkitRunnable() {
                    @Override
                    public void run() {
                        ProlongedTask.this.remind(plugin);
                        ProlongedTask.this.remind(TimeFormatter.ofRaw(ProlongedTask.this.untilNextChecks(ChronoUnit.MILLIS)));
                    }
                }).runTaskLaterAsynchronously(plugin, TimeUtils.toTicks(Duration.ofMillis(millisUntilNextCooldown).plusMillis(100L)));
            }
        }
    }
}
