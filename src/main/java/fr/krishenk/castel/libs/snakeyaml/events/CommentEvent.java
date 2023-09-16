
package fr.krishenk.castel.libs.snakeyaml.events;

import fr.krishenk.castel.libs.snakeyaml.comments.CommentType;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Objects;

public final class CommentEvent
extends Event {
    private final CommentType type;
    private final String value;

    public CommentEvent(CommentType type, String value, Mark startMark, Mark endMark) {
        super(startMark, endMark);
        this.type = Objects.requireNonNull(type);
        this.value = Objects.requireNonNull(value);
    }

    public String getValue() {
        return this.value;
    }

    public CommentType getCommentType() {
        return this.type;
    }

    @Override
    public Event.ID getEventId() {
        return Event.ID.Comment;
    }

    public String toString() {
        return "=COM " + (Object)((Object)this.type) + ' ' + this.value;
    }
}

