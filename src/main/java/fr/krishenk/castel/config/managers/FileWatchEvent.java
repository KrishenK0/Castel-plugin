package fr.krishenk.castel.config.managers;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public final class FileWatchEvent {
    private final Path path;
    private final WatchEvent.Kind<?> kind;

    public FileWatchEvent(Path path, WatchEvent.Kind<?> kind) {
        this.path = path;
        this.kind = kind;
    }

    public Path getPath() {
        return this.path;
    }

    public WatchEvent.Kind<?> getKind() {
        return this.kind;
    }
}