package fr.krishenk.castel.utils.time;

import com.google.common.base.Strings;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.string.StringUtils;

import javax.annotation.Nonnull;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class TimeUtils {
    public static final Map<String, TimeUnit> TIME_UNITS = new HashMap<String, TimeUnit>(18);
    private static final long PROGRAM_FIRST_RELEASE = 1588316400L;
    public static ZoneId TIME_ZONE = ZoneId.systemDefault();
    public static final DateTimeFormatter DATE_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd hh:mm:ss").withZone(TIME_ZONE);
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM/dd").withZone(TIME_ZONE);
    public static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("hh:mm:ss").withZone(TIME_ZONE);

    public static void validateUnixTime(long time) {
        if (time < 1588316400L) {
            throw new IllegalArgumentException("Possible invalid timestamp: " + time);
        }
    }

    @Nonnull
    public static StringBuilder getDateAndTime(TemporalAccessor accessor) {
        return TimeUtils.formatTime(DATE_TIME_FORMAT, accessor, 19);
    }

    @Nonnull
    public static StringBuilder formatTime(DateTimeFormatter formatter, TemporalAccessor accessor, int buffer) {
        StringBuilder builder = new StringBuilder(buffer);
        formatter.formatTo(accessor, builder);
        return builder;
    }

    @Nonnull
    public static StringBuilder getDateAndTime(long epoch) {
        return TimeUtils.getDateAndTime(Instant.ofEpochMilli(epoch));
    }

    @Nonnull
    public static StringBuilder getDateAndTime() {
        return TimeUtils.getDateAndTime(LocalDateTime.now());
    }

    @Nonnull
    public static StringBuilder getTime() {
        return TimeUtils.getTime(LocalTime.now());
    }

    @Nonnull
    public static StringBuilder getTime(TemporalAccessor accessor) {
        return TimeUtils.formatTime(TIME_FORMAT, accessor, 8);
    }

    @Nonnull
    public static StringBuilder getTime(long epoch) {
        return TimeUtils.getTime(Instant.ofEpochMilli(epoch));
    }

    public static long afterNow(Duration duration) {
        return duration.plusMillis(System.currentTimeMillis()).toMillis();
    }

    @Nonnull
    public static StringBuilder getDate(long epoch) {
        return TimeUtils.getDate(Instant.ofEpochMilli(epoch));
    }

    @Nonnull
    public static StringBuilder getDate(TemporalAccessor accessor) {
        return TimeUtils.formatTime(DATE_FORMAT, accessor, 10);
    }

    @Nonnull
    public static StringBuilder getDate() {
        return TimeUtils.getDate(LocalDateTime.now());
    }

    public static long getTimeUntil(CharSequence time, String timeZone, ChronoUnit timeUnit) {
        LocalTime localTime;
        LocalDateTime until;
        ZoneId zoneId = ZoneId.of(timeZone);
        LocalDateTime now = LocalDateTime.now(zoneId);
        long difference = timeUnit.between(now, until = (localTime = LocalTime.parse(time)).atDate(now.toLocalDate()));
        if (difference <= 0L) {
            until = until.plus(1L, ChronoUnit.DAYS);
            difference = timeUnit.between(now, until);
        }
        return difference * 20L;
    }

    public static Duration getTimeUntilTomrrow(ZoneId timezone) {
        ZonedDateTime now = ZonedDateTime.now(timezone);
        LocalDate tomorrow = now.toLocalDate().plusDays(1L);
        ZonedDateTime tomorrowStart = tomorrow.atStartOfDay(timezone);
        return Duration.between(now, tomorrowStart);
    }

    public static Long parseTime(String time) {
        return TimeUtils.parseTime(time, TimeUnit.SECONDS);
    }

    public static Long parseTime(String time, TimeUnit timeUnit) {
        if (Strings.isNullOrEmpty((String)time)) {
            return null;
        }
        int len = time.length();
        StringBuilder number = new StringBuilder(10);
        StringBuilder unit = new StringBuilder(7);
        boolean countNumbers = true;
        for (int i = 0; i < len; ++i) {
            char ch = time.charAt(i);
            if (ch == ' ') continue;
            if (countNumbers) {
                if (StringUtils.isEnglishDigit(ch)) {
                    number.append(ch);
                    continue;
                }
                if (number.length() == 0) {
                    return null;
                }
                countNumbers = false;
                unit.append((char)(ch | 0x20));
                continue;
            }
            unit.append((char)(ch | 0x20));
        }
        Integer num = MathUtils.parseIntUnchecked(number, false);
        if (num == null || num <= 0) {
            return 0L;
        }
        if (unit.length() > 0 && (timeUnit = TIME_UNITS.get(unit.toString())) == null) {
            return null;
        }
        return timeUnit.toMillis(num.intValue());
    }

    public static long millisToTicks(long millis) {
        return millis / 50L;
    }

    public static long toTicks(Duration duration) {
        return TimeUtils.millisToTicks(duration.toMillis());
    }

    static {
        Arrays.asList("d", "day", "days").forEach(x -> TIME_UNITS.put((String)x, TimeUnit.DAYS));
        Arrays.asList("h", "hr", "hrs", "hour", "hours").forEach(x -> TIME_UNITS.put((String)x, TimeUnit.HOURS));
        Arrays.asList("m", "min", "mins", "minute", "minutes").forEach(x -> TIME_UNITS.put((String)x, TimeUnit.MINUTES));
        Arrays.asList("s", "sec", "secs", "second", "seconds").forEach(x -> TIME_UNITS.put((String)x, TimeUnit.SECONDS));
    }
}
