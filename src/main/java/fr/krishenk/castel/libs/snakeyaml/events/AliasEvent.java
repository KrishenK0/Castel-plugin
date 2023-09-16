
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Optional;

public class AliasEvent
extends NodeEvent {
    private final Anchor alias;

    public AliasEvent(Optional<Anchor> anchor, Mark startMark, Mark endMark) {
        super(anchor, startMark, endMark);
        this.alias = anchor.orElseThrow(() -> new NullPointerException("Anchor is required in AliasEvent"));
    }

    public AliasEvent(Optional<Anchor> anchor) {
        this(anchor, null, null);
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.Alias;
    }

    public String toString() {
        return "=ALI *" + this.alias;
    }

    public Anchor getAlias() {
        return this.alias;
    }
}

