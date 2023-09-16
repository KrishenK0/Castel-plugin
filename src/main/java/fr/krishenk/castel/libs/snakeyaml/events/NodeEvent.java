
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Objects;
import java.util.Optional;

public abstract class NodeEvent
extends Event {
    private final Optional<Anchor> anchor;

    public NodeEvent(Optional<Anchor> anchor, Mark startMark, Mark endMark) {
        super(startMark, endMark);
        Objects.requireNonNull(anchor);
        this.anchor = anchor;
    }

    public Optional<Anchor> getAnchor() {
        return this.anchor;
    }
}

