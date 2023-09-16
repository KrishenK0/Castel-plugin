
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Objects;
import java.util.Optional;

public abstract class CollectionStartEvent
extends NodeEvent {
    private final Optional<String> tag;
    private final FlowStyle flowStyle;

    public CollectionStartEvent(Optional<Anchor> anchor, Optional<String> tag, FlowStyle flowStyle, Mark startMark, Mark endMark) {
        super(anchor, startMark, endMark);
        Objects.requireNonNull(tag);
        this.tag = tag;
        Objects.requireNonNull(flowStyle);
        this.flowStyle = flowStyle;
    }

    public Optional<String> getTag() {
        return this.tag;
    }

    public FlowStyle getFlowStyle() {
        return this.flowStyle;
    }

    public boolean isFlow() {
        return FlowStyle.FLOW == this.flowStyle;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        this.getAnchor().ifPresent(a -> builder.append(" &" + a));
        return builder.toString();
    }
}

