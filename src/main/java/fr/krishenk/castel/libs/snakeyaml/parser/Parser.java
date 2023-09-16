
package fr.krishenk.castel.libs.snakeyaml.parser;

import fr.krishenk.castel.libs.snakeyaml.events.Event;

import java.util.Iterator;

public interface Parser
extends Iterator<Event> {
    public boolean checkEvent(Event.ID var1);

    public Event peekEvent();

    @Override
    public Event next();
}

