
package fr.krishenk.castel.libs.snakeyaml.nodes;

import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Collection;
import java.util.Objects;

public abstract class CollectionNode<T>
extends Node {
    private FlowStyle flowStyle;

    public CollectionNode(Tag tag, FlowStyle flowStyle, Mark startMark, Mark endMark) {
        super(tag, startMark, endMark);
        this.setFlowStyle(flowStyle);
    }

    public abstract Collection<T> getValue();

    public FlowStyle getFlowStyle() {
        return this.flowStyle;
    }

    public void setFlowStyle(FlowStyle flowStyle) {
        this.flowStyle = Objects.requireNonNull(flowStyle, "Flow style must be provided.");
    }

    public void setEndMark(Mark endMark) {
        this.endMark = endMark;
    }
}

