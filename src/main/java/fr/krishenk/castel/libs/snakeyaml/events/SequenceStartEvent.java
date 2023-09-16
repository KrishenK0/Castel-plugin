
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Optional;

public final class SequenceStartEvent
extends CollectionStartEvent {
    public SequenceStartEvent(Optional<Anchor> anchor, Optional<String> tag, FlowStyle flowStyle, Mark startMark, Mark endMark) {
        super(anchor, tag, flowStyle, startMark, endMark);
    }

    public SequenceStartEvent(Optional<Anchor> anchor, Optional<String> tag, FlowStyle flowStyle) {
        this(anchor, tag, flowStyle, null, null);
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.SequenceStart;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("SequenceStartEvent");
        if (this.getFlowStyle() == FlowStyle.FLOW) {
            builder.append(" []");
        }
        builder.append(super.toString());
        return builder.toString();
    }
}

