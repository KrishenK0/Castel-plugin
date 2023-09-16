
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

public final class MappingEndEvent
extends CollectionEndEvent {
    public MappingEndEvent(Mark startMark, Mark endMark) {
        super(startMark, endMark);
    }

    public MappingEndEvent() {
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.MappingEnd;
    }

    public String toString() {
        return "MappingEndEvent";
    }
}

