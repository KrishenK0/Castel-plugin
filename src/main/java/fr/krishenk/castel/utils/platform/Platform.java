package fr.krishenk.castel.utils.platform;

public enum Platform {
    BUKKIT(CrossPlatformNotifier.isRunningBukkit()),
    SPIGOT(CrossPlatformNotifier.isRunningSpigot()),
    PAPER(CrossPlatformNotifier.isRunningPaper()),
    FORGE(CrossPlatformNotifier.isRunningForge()),
    BEDROCK(CrossPlatformNotifier.isRunningGeyser());

    private final boolean available;

    private Platform(boolean available) {
        this.available = available;
    }

    public boolean isAvailable() {
        return this.available;
    }
}
