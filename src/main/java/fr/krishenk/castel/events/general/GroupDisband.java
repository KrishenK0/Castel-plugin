package fr.krishenk.castel.events.general;

import fr.krishenk.castel.abstraction.GroupOperator;

public interface GroupDisband extends GroupOperator {
    public Reason getReason();

    public static enum Reason {
        SELF,
        ADMIN,
        TAXES,
        INACTIVITY,
        CUSTOM;
    }
}
