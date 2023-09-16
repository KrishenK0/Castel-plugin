package fr.krishenk.castel.managers.daily;

import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.utils.time.TimeUtils;

import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

public class TimeZoneHandler {
    private static final DateTimeFormatter PARSER_FORMAT = DateTimeFormatter.ofPattern("H:m[:s]");
    public static final ZoneId SERVER_TIME_ZONE;
    public static final LocalTime DAILY_CHECKS;

    public TimeZoneHandler() {}

    public static LocalTime parseLocalTime(String time) {
        return LocalTime.parse(time, PARSER_FORMAT);
    }

    public static long convert(ChronoUnit fromUnit, TimeUnit toUnit, long amount) {
        Objects.requireNonNull(fromUnit);
        Objects.requireNonNull(toUnit);
        switch (fromUnit) {
            case DAYS: return toUnit.toDays(amount);
            case HOURS: return toUnit.toHours(amount);
            case MINUTES: return toUnit.toMinutes(amount);
            case SECONDS: return toUnit.toSeconds(amount);
            case MICROS: return toUnit.toMicros(amount);
            case MILLIS: return toUnit.toMillis(amount);
            case NANOS: return toUnit.toNanos(amount);
            default: throw new UnsupportedOperationException("Cannot convert ChronoUnit to TimeUnit: " + fromUnit);
        }
    }

    static {
        String timezone = Config.DAILY_CHECKS_TIMEZONE.getString();
        if (timezone == null) {
            MessageHandler.sendConsolePluginMessage("&4Server timezone is not specified in the config. Using the default local timezone");
            timezone = "local";
        }

        SERVER_TIME_ZONE = timezone.equalsIgnoreCase("local") ? ZoneId.systemDefault() : ZoneId.of(timezone);
        TimeUtils.TIME_ZONE = SERVER_TIME_ZONE;
        String time = Config.DAILY_CHECKS_TIME.getString();
        if (time == null) {
            MessageHandler.sendConsolePluginMessage("&4Server daily checks time is not specified in the config. Using the default 12:00 timme.");
            time = "12:00";
        }

        DAILY_CHECKS = LocalTime.parse(time, PARSER_FORMAT);
    }
}
