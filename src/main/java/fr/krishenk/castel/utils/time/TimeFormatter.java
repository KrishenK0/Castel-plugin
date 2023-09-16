package fr.krishenk.castel.utils.time;

import com.google.common.base.Strings;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.builders.LanguageEntryWithContext;
import fr.krishenk.castel.locale.compiler.builders.RawLanguageEntryObjectBuilder;
import fr.krishenk.castel.locale.provider.MessageBuilder;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class TimeFormatter {
    public final long millis;
    public final long absoluteSeconds;
    public final long absoluteMinutes;
    public final long absoluteHours;
    public final long absoluteDays;
    public final long absoluteWeeks;
    public final long absoluteMonths;
    public final long years;
    public final long seconds;
    public final long minutes;
    public final long hours;
    public final long days;
    public final long weeks;
    public final long months;

    public TimeFormatter(long time, TimeUnit unit) {
        this.millis = unit.toMillis(Math.abs(time));
        this.absoluteSeconds = Math.round((double)this.millis / 1000.0);
        this.absoluteMinutes = this.absoluteSeconds / 60L;
        this.absoluteHours = this.absoluteMinutes / 60L;
        this.absoluteDays = this.absoluteHours / 24L;
        this.absoluteWeeks = this.absoluteDays / 7L;
        this.absoluteMonths = this.absoluteDays / 30L;
        this.years = this.absoluteDays / 365L;
        this.seconds = this.absoluteSeconds % 60L;
        this.minutes = this.absoluteMinutes % 60L;
        this.hours = this.absoluteHours % 24L;
        this.days = this.absoluteDays % 7L;
        this.weeks = this.absoluteWeeks % 4L;
        this.months = this.absoluteMonths % 12L;
    }

    private static String replaceVariables(String str, Object ... edits) {
        for (int i = edits.length - 1; i > 0; --i) {
            String replacement = String.valueOf(edits[i]);
            String variable = String.valueOf(edits[--i]);
            str = MessageHandler.replace(str, variable, replacement);
        }
        return str;
    }

    private static String format(long number) {
        return number >= 10L ? Long.toString(number) : '0' + Long.toString(number);
    }

    public static MessageObjectBuilder of(long time) {
        return new TimeFormatter(time, TimeUnit.MILLISECONDS).getFormat();
    }

    public static String ofRaw(long time) {
        return new TimeFormatter(time, TimeUnit.MILLISECONDS).toString();
    }

    public String toString() {
        if (this.absoluteMinutes == 0L) {
            return "00:00:" + TimeFormatter.format(this.absoluteSeconds);
        }
        if (this.absoluteHours == 0L) {
            return "00:" + TimeFormatter.format(this.absoluteMinutes) + ':' + TimeFormatter.format(this.seconds);
        }
        if (this.absoluteDays == 0L) {
            return TimeFormatter.format(this.absoluteHours) + ':' + TimeFormatter.format(this.minutes) + ':' + TimeFormatter.format(this.seconds);
        }
        if (this.absoluteWeeks == 0L) {
            return this.absoluteDays + " day(s), " + TimeFormatter.format(this.hours) + ':' + TimeFormatter.format(this.minutes) + ':' + TimeFormatter.format(this.seconds);
        }
        if (this.absoluteMonths == 0L) {
            return this.format("wwwwa week(s), dd day(s), hh:mm:ss");
        }
        return this.format("yyyy years, MMA month(s), wwww week(s), dd day(s), hh:mm:ss");
    }

    private Object[] getEdits() {
        return new Object[]{"yyyy", this.years, "MM", this.months, "MMA", this.absoluteMonths, "wwww", this.weeks, "wwwwa", this.absoluteWeeks, "dd", this.days, "dda", this.absoluteDays, "hh", this.hours, "hha", this.absoluteHours, "hhf", TimeFormatter.format(this.hours), "hhaf", TimeFormatter.format(this.absoluteHours), "mm", this.minutes, "mmf", TimeFormatter.format(this.minutes), "mma", this.absoluteMinutes, "mmaf", TimeFormatter.format(this.absoluteMinutes), "ss", this.seconds, "ssa", this.absoluteSeconds, "ssf", TimeFormatter.format(this.seconds), "ssaf", TimeFormatter.format(this.absoluteSeconds)};
    }

    private String format(String format) {
        if (Strings.isNullOrEmpty(format)) {
            return format;
        }
        return TimeFormatter.replaceVariables(format, this.getEdits());
    }

    public static MessageObjectBuilder dateOf(long time) {
        return new RawLanguageEntryObjectBuilder(CastelLang.DATE_FORMATTER, str -> {
            try {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern(str).withZone(TimeUtils.TIME_ZONE);
                return TimeUtils.formatTime(formatter, Instant.ofEpochMilli(time), 20).toString();
            }
            catch (IllegalArgumentException ex) {
                CLogger.error("Illegal character '" + str + "' inside 'date-formatter' inside language file: " + ex.getMessage());
                if (CLogger.isDebugging()) {
                    ex.printStackTrace();
                }
                return TimeUtils.getDate().toString();
            }
        });
    }

    public MessageObjectBuilder getFormat() {
        String path = this.absoluteMinutes == 0L ? "SECONDS" : (this.absoluteHours == 0L ? "MINUTES" : (this.absoluteDays == 0L ? "HOURS" : (this.absoluteWeeks == 0L ? "DAYS" : (this.absoluteMonths == 0L ? "WEEKS" : "MONTHS"))));
        MessageBuilder settings = new MessageBuilder().raws(this.getEdits());
        return new LanguageEntryWithContext(CastelLang.valueOf("TIME_FORMATTER_" + path), settings);
    }
}


