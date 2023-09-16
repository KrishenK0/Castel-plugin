
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.common.CharConstants;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class ScalarEvent
extends NodeEvent {
    private final ScalarStyle style;
    private final String value;

    public ScalarEvent(Optional<Anchor> anchor, String value, ScalarStyle style, Mark startMark, Mark endMark) {
        super(anchor, startMark, endMark);
        this.value = Objects.requireNonNull(value);
        this.style = Objects.requireNonNull(style);
    }

    public ScalarEvent(Optional<Anchor> anchor, String value, ScalarStyle style) {
        this(anchor, value, style, null, null);
    }

    public ScalarStyle getScalarStyle() {
        return this.style;
    }

    public String getValue() {
        return this.value;
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.Scalar;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder("ScalarEvent{");
        this.getAnchor().ifPresent(a -> builder.append(" &").append(a));
        builder.append(' ');
        builder.append(this.style.toString());
        builder.append(this.escapedValue());
        return builder.append('}').toString();
    }

    public String escapedValue() {
        return this.value.codePoints().filter(i -> i < 65535).mapToObj(ch -> CharConstants.escapeChar(String.valueOf(Character.toChars(ch)))).collect(Collectors.joining(""));
    }
}

