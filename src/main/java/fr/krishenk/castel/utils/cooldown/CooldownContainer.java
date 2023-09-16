package fr.krishenk.castel.utils.cooldown;

public class CooldownContainer {
    protected final long time;
    protected final long start;

    protected CooldownContainer(long time, long start) {
        this.time = time;
        this.start = start;
    }
}
