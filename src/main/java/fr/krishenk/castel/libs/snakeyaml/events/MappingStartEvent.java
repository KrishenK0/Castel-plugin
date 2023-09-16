
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Optional;

public final class MappingStartEvent
extends CollectionStartEvent {
    public MappingStartEvent(Optional<Anchor> anchor, Optional<String> tag, FlowStyle flowStyle, Mark startMark, Mark endMark) {
        super(anchor, tag, flowStyle, startMark, endMark);
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.MappingStart;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("MappingStartEvent");
        if (this.getFlowStyle() == FlowStyle.FLOW) {
            builder.append(" {}");
        }
        builder.append(super.toString());
        return builder.toString();
    }
}

