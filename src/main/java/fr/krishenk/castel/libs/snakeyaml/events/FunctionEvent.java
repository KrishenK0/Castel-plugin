
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;
import fr.krishenk.castel.libs.snakeyaml.nodes.SequenceNode;

import java.util.Optional;

public final class FunctionEvent
extends AliasEvent {
    private final SequenceNode parameters;

    public FunctionEvent(Optional<Anchor> anchor, SequenceNode parameters, Mark endMark, Mark startMark) {
        super(anchor, startMark, endMark);
        this.parameters = parameters;
    }

    public FunctionEvent(Optional<Anchor> anchor, SequenceNode parameters) {
        this(anchor, parameters, null, null);
    }

    public SequenceNode getParameters() {
        return this.parameters;
    }
}

