package fr.krishenk.castel.managers.logger;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.managers.daily.TimeZoneHandler;
import fr.krishenk.castel.utils.cache.CacheHandler;
import fr.krishenk.castel.utils.time.TimeUtils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

public class CastelLogger {
    private static final DateTimeFormatter DATE_PATTERN = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final Path PARENT = CastelPlugin.getInstance().getDataFolder().toPath().resolve("logs");
    private static final String EXTENSION = ".log";
    private static final CastelLogger MAIN = new CastelLogger();
    private BufferedWriter writer;

    public CastelLogger(BufferedWriter writer) {
        this.writer = writer;
    }

    public CastelLogger(Path path) {
        this.setupWriter(path);
    }

    public CastelLogger() {
        this.setupWriter(PARENT.resolve(this.buildFilePath()));
    }

    public static CastelLogger getMain() {
        return MAIN;
    }

    private String buildFilePath() {
        return ZonedDateTime.now(TimeZoneHandler.SERVER_TIME_ZONE).format(DATE_PATTERN) + ".log";
    }

    private void setupWriter(Path path) {
        BufferedWriter writer;
        try {
            setupDir(path);
            writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
            writer = null;
        }

        this.writer = writer;
    }

    private static Path setupDir(Path path) {
        try {
            Files.createDirectories(path.getParent());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return path;
    }

    private static String getTime() {
        return '[' + TimeUtils.TIME_FORMAT.format(ZonedDateTime.now(TimeZoneHandler.SERVER_TIME_ZONE)) + ']';
    }

    public void flush() {
        try {
            MAIN.writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public CastelLogger log(String str) {
        try {
            this.writer.write(getTime() + ' ' + str);
            this.writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public CastelLogger newLine() {
        try {
            this.writer.newLine();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return this;
    }

    public void close() {
        try {
            this.writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static {
        Duration time = TimeUtils.getTimeUntilTomrrow(TimeZoneHandler.SERVER_TIME_ZONE);
        CacheHandler.newSheduler().scheduleAtFixedRate(() -> {
            MAIN.close();
            Path path = setupDir(PARENT.resolve(MAIN.buildFilePath()));
            MAIN.setupWriter(path);
        }, time.toMillis(), TimeUnit.DAYS.toMillis(1L), TimeUnit.MILLISECONDS);
    }
}
