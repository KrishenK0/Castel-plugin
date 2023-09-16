
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

public final class DocumentStartEvent
extends Event {
    public DocumentStartEvent(Mark startMark, Mark endMark) {
        super(startMark, endMark);
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.DocumentStart;
    }

    public String toString() {
        return "DocumentStartEvent";
    }
}

