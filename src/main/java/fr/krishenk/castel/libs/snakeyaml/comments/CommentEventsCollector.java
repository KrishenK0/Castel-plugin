
package fr.krishenk.castel.libs.snakeyaml.comments;

import fr.krishenk.castel.libs.snakeyaml.events.CommentEvent;
import fr.krishenk.castel.libs.snakeyaml.events.Event;
import fr.krishenk.castel.libs.snakeyaml.parser.Parser;

import java.util.*;

public class CommentEventsCollector {
    private List<CommentLine> commentLineList = new ArrayList<CommentLine>();
    private final Queue<Event> eventSource;
    private final CommentType[] expectedCommentTypes;

    public CommentEventsCollector(final Parser parser, CommentType ... expectedCommentTypes) {
        this.eventSource = new AbstractQueue<Event>(){

            @Override
            public boolean offer(Event e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Event poll() {
                return parser.next();
            }

            @Override
            public Event peek() {
                return parser.peekEvent();
            }

            @Override
            public Iterator<Event> iterator() {
                throw new UnsupportedOperationException();
            }

            @Override
            public int size() {
                throw new UnsupportedOperationException();
            }
        };
        this.expectedCommentTypes = expectedCommentTypes;
    }

    public CommentEventsCollector(Queue<Event> eventSource, CommentType ... expectedCommentTypes) {
        this.eventSource = eventSource;
        this.expectedCommentTypes = expectedCommentTypes;
    }

    private boolean isEventExpected(Event event) {
        if (event == null || event.getEventId() != Event.ID.Comment) {
            return false;
        }
        CommentEvent commentEvent = (CommentEvent)event;
        for (CommentType type : this.expectedCommentTypes) {
            if (commentEvent.getCommentType() != type) continue;
            return true;
        }
        return false;
    }

    public CommentEventsCollector collectEvents() {
        this.collectEvents(null);
        return this;
    }

    public Event collectEvents(Event event) {
        if (event != null) {
            if (this.isEventExpected(event)) {
                this.commentLineList.add(new CommentLine((CommentEvent)event));
            } else {
                return event;
            }
        }
        while (this.isEventExpected(this.eventSource.peek())) {
            this.commentLineList.add(new CommentLine((CommentEvent)this.eventSource.poll()));
        }
        return null;
    }

    public Event collectEventsAndPoll(Event event) {
        Event nextEvent = this.collectEvents(event);
        return nextEvent != null ? nextEvent : this.eventSource.poll();
    }

    public List<CommentLine> consume() {
        try {
            List<CommentLine> list2 = this.commentLineList;
            return list2;
        }
        finally {
            this.commentLineList = new ArrayList<CommentLine>();
        }
    }

    public boolean isEmpty() {
        return this.commentLineList.isEmpty();
    }
}

