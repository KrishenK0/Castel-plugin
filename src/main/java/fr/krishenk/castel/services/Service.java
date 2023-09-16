package fr.krishenk.castel.services;

public interface Service {
    default public boolean isAvailable() { return true; }

    default public void enable() {}

    default public void disable() {}
}
