
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

public final class DocumentEndEvent
extends Event {
    public DocumentEndEvent(Mark startMark, Mark endMark) {
        super(startMark, endMark);
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.DocumentEnd;
    }

    public String toString() {
        return "DocumentEndEvent";
    }
}

