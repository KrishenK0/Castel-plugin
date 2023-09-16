package fr.krishenk.castel.services;

public class EmptyService implements Service {
    public static final Service INSTANCE = new EmptyService();
    EmptyService() {}
}
