
package fr.krishenk.castel.libs.snakeyaml.comments;

import fr.krishenk.castel.libs.snakeyaml.events.CommentEvent;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Objects;

public class CommentLine {
    private final Mark startMark;
    private final Mark endMark;
    private final String value;
    private final CommentType commentType;

    public CommentLine(CommentEvent event) {
        this(event.getStartMark(), event.getEndMark(), event.getValue(), event.getCommentType());
    }

    public CommentLine(String value, CommentType commentType) {
        this(null, null, value, commentType);
    }

    public CommentLine(Mark startMark, Mark endMark, String value, CommentType commentType) {
        this.startMark = startMark;
        this.endMark = endMark;
        this.value = Objects.requireNonNull(value);
        this.commentType = Objects.requireNonNull(commentType);
    }

    public Mark getEndMark() {
        return this.endMark;
    }

    public Mark getStartMark() {
        return this.startMark;
    }

    public CommentType getCommentType() {
        return this.commentType;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return '<' + this.getClass().getName() + " (type=" + (Object)((Object)this.commentType) + ", value='" + this.value + "')>";
    }
}

