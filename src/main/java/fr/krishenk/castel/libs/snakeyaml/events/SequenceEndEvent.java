
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

public final class SequenceEndEvent
extends CollectionEndEvent {
    public SequenceEndEvent(Mark startMark, Mark endMark) {
        super(startMark, endMark);
    }

    public SequenceEndEvent() {
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.SequenceEnd;
    }

    public String toString() {
        return "SequenceEndEvent";
    }
}

