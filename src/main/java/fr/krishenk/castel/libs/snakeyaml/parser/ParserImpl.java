
package fr.krishenk.castel.libs.snakeyaml.parser;

import fr.krishenk.castel.libs.snakeyaml.api.LoadSettings;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentType;
import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.common.ArrayStack;
import fr.krishenk.castel.libs.snakeyaml.common.FlowStyle;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.events.*;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ParserException;
import fr.krishenk.castel.libs.snakeyaml.exceptions.YamlEngineException;
import fr.krishenk.castel.libs.snakeyaml.scanner.Scanner;
import fr.krishenk.castel.libs.snakeyaml.scanner.ScannerImpl;
import fr.krishenk.castel.libs.snakeyaml.scanner.StreamReader;
import fr.krishenk.castel.libs.snakeyaml.tokens.*;

import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ParserImpl
implements Parser {
    protected final Scanner scanner;
    private final LoadSettings settings;
    private Optional<Event> currentEvent;
    private final ArrayStack<Production> states;
    private final ArrayStack<Mark> marksStack;
    private Production state;

    public ParserImpl(LoadSettings settings, StreamReader reader) {
        this(settings, new ScannerImpl(settings, reader));
    }

    public ParserImpl(LoadSettings settings, Scanner scanner) {
        this.scanner = scanner;
        this.settings = settings;
        this.currentEvent = Optional.empty();
        this.states = new ArrayStack(100);
        this.marksStack = new ArrayStack(10);
        this.state = new ParseDocumentContent();
        scanner.next();
    }

    @Override
    public boolean checkEvent(Event.ID id) {
        this.peekEvent();
        return this.currentEvent.isPresent() && this.currentEvent.get().getEventId() == id;
    }

    @Override
    public Event peekEvent() {
        this.produce();
        return this.currentEvent.orElseThrow(() -> new NoSuchElementException("No more Events found."));
    }

    @Override
    public Event next() {
        Event value = this.peekEvent();
        this.currentEvent = Optional.empty();
        return value;
    }

    @Override
    public boolean hasNext() {
        this.produce();
        return this.currentEvent.isPresent();
    }

    private void produce() {
        if (!this.currentEvent.isPresent() && this.state != null) {
            this.currentEvent = Optional.of(this.state.produce());
        }
    }

    static CommentEvent produceCommentEvent(CommentToken token) {
        String value = token.getValue();
        CommentType type = token.getCommentType();
        return new CommentEvent(type, value, token.getStartMark(), token.getEndMark());
    }

    Event parseBlockNode() {
        return this.parseNode(true, false);
    }

    Event parseFlowNode() {
        return this.parseNode(false, false);
    }

    Event parseBlockNodeOrIndentlessSequence() {
        return this.parseNode(true, true);
    }

    /*
     * WARNING - void declaration
     * Enabled aggressive block sorting
     */
    Event parseNode(boolean block, boolean indentlessSequence) {
        String string;
        Token token;
        if (this.scanner.checkToken(Token.ID.Alias)) {
            String string2;
            AliasToken token2 = (AliasToken)this.scanner.next();
            AliasEvent aliasEvent = new AliasEvent(Optional.of(token2.getValue()), token2.getStartMark(), token2.getEndMark());
            if (token2.getValue().getIdentifier().startsWith("fn-")) {
                String string3;
                Mark startMark = this.scanner.peekToken().getStartMark();
                if (this.scanner.checkToken(Token.ID.FlowSequenceStart)) {
                    this.state = new ParseFlowSequenceStart();
                    return aliasEvent;
                }
                Token next = this.scanner.peekToken();
                StringBuilder stringBuilder = new StringBuilder().append("while parsing a ");
                if (block) {
                    string3 = "block";
                    throw new ParserException(stringBuilder.append(string3).append(" node").toString(), startMark, "expected alias function parameters, but found '" + next.getTokenId() + "'", next.getStartMark());
                }
                string3 = "flow";
                throw new ParserException(stringBuilder.append(string3).append(" node").toString(), startMark, "expected alias function parameters, but found '" + next.getTokenId() + "'", next.getStartMark());
            }
            if (!this.scanner.checkToken(Token.ID.FlowSequenceStart)) {
                this.state = this.states.pop();
                return aliasEvent;
            }
            Mark startMark = this.scanner.peekToken().getStartMark();
            Token next = this.scanner.peekToken();
            StringBuilder stringBuilder = new StringBuilder().append("while parsing a ");
            if (block) {
                string2 = "block";
                throw new ParserException(stringBuilder.append(string2).append(" node").toString(), startMark, "didn't expect alias function parameters '" + next.getTokenId() + "'", next.getStartMark());
            }
            string2 = "flow";
            throw new ParserException(stringBuilder.append(string2).append(" node").toString(), startMark, "didn't expect alias function parameters '" + next.getTokenId() + "'", next.getStartMark());
        }
        Mark startMark = null;
        Mark endMark = null;
        Optional<Anchor> anchor = Optional.empty();
        if (this.scanner.checkToken(Token.ID.Anchor)) {
            token = this.scanner.next();
            startMark = token.getStartMark();
            endMark = token.getEndMark();
            anchor = Optional.of(((AnchorToken)token).getValue());
        }
        if (startMark == null) {
            endMark = startMark = this.scanner.peekToken().getStartMark();
        }
        if (indentlessSequence) {
            if (this.scanner.checkToken(Token.ID.BlockEntry)) {
                endMark = this.scanner.peekToken().getEndMark();
                SequenceStartEvent sequenceStartEvent = new SequenceStartEvent(anchor, Optional.empty(), FlowStyle.BLOCK, startMark, endMark);
                this.state = new ParseIndentlessSequenceEntryKey();
                return sequenceStartEvent;
            }
        }
        if (this.scanner.checkToken(Token.ID.Scalar)) {
            token = this.scanner.next();
            endMark = token.getEndMark();
            ScalarEvent scalarEvent = new ScalarEvent(anchor, ((ScalarToken)token).getValue(), ((ScalarToken)token).getStyle(), startMark, endMark);
            this.state = this.states.pop();
            return scalarEvent;
        }
        if (this.scanner.checkToken(Token.ID.FlowSequenceStart)) {
            endMark = this.scanner.peekToken().getEndMark();
            SequenceStartEvent sequenceStartEvent = new SequenceStartEvent(anchor, Optional.empty(), FlowStyle.FLOW, startMark, endMark);
            this.state = new ParseFlowSequenceFirstEntry();
            return sequenceStartEvent;
        }
        if (this.scanner.checkToken(Token.ID.FlowMappingStart)) {
            endMark = this.scanner.peekToken().getEndMark();
            MappingStartEvent mappingStartEvent = new MappingStartEvent(anchor, Optional.empty(), FlowStyle.FLOW, startMark, endMark);
            this.state = new ParseFlowMappingFirstKey();
            return mappingStartEvent;
        }
        if (block) {
            if (this.scanner.checkToken(Token.ID.BlockSequenceStart)) {
                endMark = this.scanner.peekToken().getStartMark();
                SequenceStartEvent sequenceStartEvent = new SequenceStartEvent(anchor, Optional.empty(), FlowStyle.BLOCK, startMark, endMark);
                this.state = new ParseBlockSequenceFirstEntry();
                return sequenceStartEvent;
            }
        }
        if (block) {
            if (this.scanner.checkToken(Token.ID.BlockMappingStart)) {

                endMark = this.scanner.peekToken().getStartMark();
                MappingStartEvent mappingStartEvent = new MappingStartEvent(anchor, Optional.empty(), FlowStyle.BLOCK, startMark, endMark);
                this.state = new ParseBlockMappingFirstKey();
                return mappingStartEvent;
            }
        }
        if (anchor.isPresent()) {
            ScalarEvent scalarEvent = new ScalarEvent(anchor, "", ScalarStyle.PLAIN, startMark, endMark);
            this.state = this.states.pop();
            return scalarEvent;
        }
        token = this.scanner.peekToken();
        StringBuilder stringBuilder = new StringBuilder().append("while parsing a ");
        if (block) {
            string = "block";
            throw new ParserException(stringBuilder.append(string).append(" node").toString(), startMark, "expected the node content, but found '" + token.getTokenId() + "'", token.getStartMark());
        }
        string = "flow";
        throw new ParserException(stringBuilder.append(string).append(" node").toString(), startMark, "expected the node content, but found '" + token.getTokenId() + "'", token.getStartMark());
    }


    private static Event processEmptyScalar(Mark mark) {
        return new ScalarEvent(Optional.empty(), "", ScalarStyle.PLAIN, mark, mark);
    }

    private Mark markPop() {
        return this.marksStack.pop();
    }

    private void markPush(Mark mark) {
        this.marksStack.push(mark);
    }

    final class ParseDocumentContent implements Production {
        ParseDocumentContent() {
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = new ParseDocumentContent();
                return ParserImpl.produceCommentEvent((CommentToken)ParserImpl.this.scanner.next());
            }
            if (ParserImpl.this.scanner.checkToken(Token.ID.DocumentEnd)) {
                DocumentEndToken token = (DocumentEndToken)ParserImpl.this.scanner.next();
                DocumentEndEvent event = new DocumentEndEvent(token.getStartMark(), token.getEndMark());
                if (!ParserImpl.this.states.isEmpty()) {
                    throw new YamlEngineException("Unexpected end of stream. States left: " + ParserImpl.this.states);
                }
                if (!ParserImpl.this.marksStack.isEmpty()) {
                    throw new YamlEngineException("Unexpected end of stream. Marks left: " + ParserImpl.this.marksStack);
                }
                ParserImpl.this.state = null;
                return event;
            }
            ParserImpl.this.states.push(new ParseDocumentEnd());
            return ParserImpl.this.parseBlockNode();
        }
    }

    final class ParseFlowSequenceStart
    implements Production {
        ParseFlowSequenceStart() {
        }

        @Override
        public Event produce() {
            Mark startMark;
            Mark endMark = startMark = ParserImpl.this.scanner.peekToken().getStartMark();
            ParserImpl.this.state = new ParseFlowSequenceFirstEntry();
            return new SequenceStartEvent(Optional.empty(), Optional.empty(), FlowStyle.FLOW, startMark, endMark);
        }
    }

    final class ParseIndentlessSequenceEntryKey
    implements Production {
        ParseIndentlessSequenceEntryKey() {
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = new ParseIndentlessSequenceEntryKey();
                return ParserImpl.produceCommentEvent((CommentToken)ParserImpl.this.scanner.next());
            }
            if (ParserImpl.this.scanner.checkToken(Token.ID.BlockEntry)) {
                BlockEntryToken token = (BlockEntryToken)ParserImpl.this.scanner.next();
                return new ParseIndentlessSequenceEntryValue(token).produce();
            }
            Token token = ParserImpl.this.scanner.peekToken();
            SequenceEndEvent event = new SequenceEndEvent(token.getStartMark(), token.getEndMark());
            ParserImpl.this.state = ParserImpl.this.states.pop();
            return event;
        }
    }

    final class ParseFlowSequenceFirstEntry
    implements Production {
        ParseFlowSequenceFirstEntry() {
        }

        @Override
        public Event produce() {
            Token token = ParserImpl.this.scanner.next();
            ParserImpl.this.markPush(token.getStartMark());
            return new ParseFlowSequenceEntry(true).produce();
        }
    }

    private class ParseFlowMappingFirstKey
    implements Production {
        private ParseFlowMappingFirstKey() {
        }

        @Override
        public Event produce() {
            Token token = ParserImpl.this.scanner.next();
            ParserImpl.this.markPush(token.getStartMark());
            return new ParseFlowMappingKey(true).produce();
        }
    }

    final class ParseBlockSequenceFirstEntry
    implements Production {
        ParseBlockSequenceFirstEntry() {
        }

        @Override
        public Event produce() {
            Token token = ParserImpl.this.scanner.next();
            ParserImpl.this.markPush(token.getStartMark());
            return new ParseBlockSequenceEntryKey().produce();
        }
    }

    final class ParseBlockMappingFirstKey
    implements Production {
        ParseBlockMappingFirstKey() {
        }

        @Override
        public Event produce() {
            Token token = ParserImpl.this.scanner.next();
            ParserImpl.this.markPush(token.getStartMark());
            return new ParseBlockMappingKey().produce();
        }
    }

    private class ParseFlowMappingEmptyValue
    implements Production {
        private ParseFlowMappingEmptyValue() {
        }

        @Override
        public Event produce() {
            ParserImpl.this.state = new ParseFlowMappingKey(false);
            return ParserImpl.processEmptyScalar(ParserImpl.this.scanner.peekToken().getStartMark());
        }
    }

    final class ParseFlowMappingValue
    implements Production {
        ParseFlowMappingValue() {
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Value)) {
                Token token = ParserImpl.this.scanner.next();
                if (!ParserImpl.this.scanner.checkToken(Token.ID.FlowEntry, Token.ID.FlowMappingEnd)) {
                    ParserImpl.this.states.push(new ParseFlowMappingKey(false));
                    return ParserImpl.this.parseFlowNode();
                }
                ParserImpl.this.state = new ParseFlowMappingKey(false);
                return ParserImpl.processEmptyScalar(token.getEndMark());
            }
            ParserImpl.this.state = new ParseFlowMappingKey(false);
            Token token = ParserImpl.this.scanner.peekToken();
            return ParserImpl.processEmptyScalar(token.getStartMark());
        }
    }

    private class ParseFlowMappingKey
    implements Production {
        private final boolean first;

        public ParseFlowMappingKey(boolean first) {
            this.first = first;
        }

        @Override
        public Event produce() {
            if (!ParserImpl.this.scanner.checkToken(Token.ID.FlowMappingEnd)) {
                if (!this.first) {
                    if (ParserImpl.this.scanner.checkToken(Token.ID.FlowEntry)) {
                        ParserImpl.this.scanner.next();
                    } else {
                        Token token = ParserImpl.this.scanner.peekToken();
                        throw new ParserException("while parsing a flow mapping", ParserImpl.this.markPop(), "expected ',' or '}', but got " + token.getTokenId(), token.getStartMark());
                    }
                }
                if (ParserImpl.this.scanner.checkToken(Token.ID.Key)) {
                    Token token = ParserImpl.this.scanner.next();
                    if (!ParserImpl.this.scanner.checkToken(Token.ID.Value, Token.ID.FlowEntry, Token.ID.FlowMappingEnd)) {
                        ParserImpl.this.states.push(new ParseFlowMappingValue());
                        return ParserImpl.this.parseFlowNode();
                    }
                    ParserImpl.this.state = new ParseFlowMappingValue();
                    return ParserImpl.processEmptyScalar(token.getEndMark());
                }
                if (!ParserImpl.this.scanner.checkToken(Token.ID.FlowMappingEnd)) {
                    ParserImpl.this.states.push(new ParseFlowMappingEmptyValue());
                    return ParserImpl.this.parseFlowNode();
                }
            }
            Token token = ParserImpl.this.scanner.next();
            MappingEndEvent event = new MappingEndEvent(token.getStartMark(), token.getEndMark());
            ParserImpl.this.markPop();
            if (!ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = ParserImpl.this.states.pop();
            } else {
                ParserImpl.this.state = new ParseFlowEndComment();
            }
            return event;
        }
    }

    final class ParseFlowSequenceEntryMappingEnd
    implements Production {
        ParseFlowSequenceEntryMappingEnd() {
        }

        @Override
        public Event produce() {
            ParserImpl.this.state = new ParseFlowSequenceEntry(false);
            Token token = ParserImpl.this.scanner.peekToken();
            return new MappingEndEvent(token.getStartMark(), token.getEndMark());
        }
    }

    private class ParseFlowSequenceEntryMappingValue
    implements Production {
        private ParseFlowSequenceEntryMappingValue() {
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Value)) {
                Token token = ParserImpl.this.scanner.next();
                if (!ParserImpl.this.scanner.checkToken(Token.ID.FlowEntry, Token.ID.FlowSequenceEnd)) {
                    ParserImpl.this.states.push(new ParseFlowSequenceEntryMappingEnd());
                    return ParserImpl.this.parseFlowNode();
                }
                ParserImpl.this.state = new ParseFlowSequenceEntryMappingEnd();
                return ParserImpl.processEmptyScalar(token.getEndMark());
            }
            ParserImpl.this.state = new ParseFlowSequenceEntryMappingEnd();
            Token token = ParserImpl.this.scanner.peekToken();
            return ParserImpl.processEmptyScalar(token.getStartMark());
        }
    }

    final class ParseFlowSequenceEntryMappingKey
    implements Production {
        ParseFlowSequenceEntryMappingKey() {
        }

        @Override
        public Event produce() {
            Token token = ParserImpl.this.scanner.next();
            if (!ParserImpl.this.scanner.checkToken(Token.ID.Value, Token.ID.FlowEntry, Token.ID.FlowSequenceEnd)) {
                ParserImpl.this.states.push(new ParseFlowSequenceEntryMappingValue());
                return ParserImpl.this.parseFlowNode();
            }
            ParserImpl.this.state = new ParseFlowSequenceEntryMappingValue();
            return ParserImpl.processEmptyScalar(token.getEndMark());
        }
    }

    final class ParseFlowEndComment
    implements Production {
        ParseFlowEndComment() {
        }

        @Override
        public Event produce() {
            CommentEvent event = ParserImpl.produceCommentEvent((CommentToken)ParserImpl.this.scanner.next());
            if (!ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = ParserImpl.this.states.pop();
            }
            return event;
        }
    }

    final class ParseFlowSequenceEntry
    implements Production {
        private final boolean first;

        public ParseFlowSequenceEntry(boolean first) {
            this.first = first;
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = new ParseFlowSequenceEntry(this.first);
                return ParserImpl.produceCommentEvent((CommentToken)ParserImpl.this.scanner.next());
            }
            if (!ParserImpl.this.scanner.checkToken(Token.ID.FlowSequenceEnd)) {
                if (!this.first) {
                    if (ParserImpl.this.scanner.checkToken(Token.ID.FlowEntry)) {
                        ParserImpl.this.scanner.next();
                        if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                            ParserImpl.this.state = new ParseFlowSequenceEntry(true);
                            return ParserImpl.produceCommentEvent((CommentToken)ParserImpl.this.scanner.next());
                        }
                    } else {
                        Token token = ParserImpl.this.scanner.peekToken();
                        throw new ParserException("while parsing a flow sequence", ParserImpl.this.markPop(), "expected ',' or ']', but got " + token.getTokenId(), token.getStartMark());
                    }
                }
                if (ParserImpl.this.scanner.checkToken(Token.ID.Key)) {
                    Token token = ParserImpl.this.scanner.peekToken();
                    MappingStartEvent event = new MappingStartEvent(Optional.empty(), Optional.empty(), FlowStyle.FLOW, token.getStartMark(), token.getEndMark());
                    ParserImpl.this.state = new ParseFlowSequenceEntryMappingKey();
                    return event;
                }
                if (!ParserImpl.this.scanner.checkToken(Token.ID.FlowSequenceEnd)) {
                    ParserImpl.this.states.push(new ParseFlowSequenceEntry(false));
                    return ParserImpl.this.parseFlowNode();
                }
            }
            Token token = ParserImpl.this.scanner.next();
            SequenceEndEvent event = new SequenceEndEvent(token.getStartMark(), token.getEndMark());
            if (!ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = ParserImpl.this.states.pop();
            } else {
                ParserImpl.this.state = new ParseFlowEndComment();
            }
            ParserImpl.this.markPop();
            return event;
        }
    }

    final class ParseBlockMappingValueCommentList
    implements Production {
        final List<CommentToken> tokens;

        public ParseBlockMappingValueCommentList(List<CommentToken> tokens) {
            this.tokens = tokens;
        }

        @Override
        public Event produce() {
            if (!this.tokens.isEmpty()) {
                return ParserImpl.produceCommentEvent(this.tokens.remove(0));
            }
            return new ParseBlockMappingKey().produce();
        }
    }

    final class ParseBlockMappingValueComment
    implements Production {
        final List<CommentToken> tokens = new LinkedList<CommentToken>();

        ParseBlockMappingValueComment() {
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                this.tokens.add((CommentToken)ParserImpl.this.scanner.next());
                return this.produce();
            }
            if (!ParserImpl.this.scanner.checkToken(Token.ID.Key, Token.ID.Value, Token.ID.BlockEnd)) {
                if (!this.tokens.isEmpty()) {
                    return ParserImpl.produceCommentEvent(this.tokens.remove(0));
                }
                ParserImpl.this.states.push(new ParseBlockMappingKey());
                return ParserImpl.this.parseBlockNodeOrIndentlessSequence();
            }
            ParserImpl.this.state = new ParseBlockMappingValueCommentList(this.tokens);
            return ParserImpl.processEmptyScalar(ParserImpl.this.scanner.peekToken().getStartMark());
        }
    }

    final class ParseBlockMappingValue
    implements Production {
        ParseBlockMappingValue() {
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Value)) {
                Token token = ParserImpl.this.scanner.next();
                if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                    ParseBlockMappingValueComment p = new ParseBlockMappingValueComment();
                    ParserImpl.this.state = p;
                    return p.produce();
                }
                if (!ParserImpl.this.scanner.checkToken(Token.ID.Key, Token.ID.Value, Token.ID.BlockEnd)) {
                    ParserImpl.this.states.push(new ParseBlockMappingKey());
                    return ParserImpl.this.parseBlockNodeOrIndentlessSequence();
                }
                ParserImpl.this.state = new ParseBlockMappingKey();
                return ParserImpl.processEmptyScalar(token.getEndMark());
            }
            if (ParserImpl.this.scanner.checkToken(Token.ID.Scalar)) {
                ParserImpl.this.states.push(new ParseBlockMappingKey());
                return ParserImpl.this.parseBlockNodeOrIndentlessSequence();
            }
            ParserImpl.this.state = new ParseBlockMappingKey();
            Token token = ParserImpl.this.scanner.peekToken();
            return ParserImpl.processEmptyScalar(token.getStartMark());
        }
    }

    final class ParseBlockMappingKey
    implements Production {
        ParseBlockMappingKey() {
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = new ParseBlockMappingKey();
                return ParserImpl.produceCommentEvent((CommentToken)ParserImpl.this.scanner.next());
            }
            if (ParserImpl.this.scanner.checkToken(Token.ID.Key)) {
                Token token = ParserImpl.this.scanner.next();
                if (!ParserImpl.this.scanner.checkToken(Token.ID.Key, Token.ID.Value, Token.ID.BlockEnd)) {
                    ParserImpl.this.states.push(new ParseBlockMappingValue());
                    return ParserImpl.this.parseBlockNodeOrIndentlessSequence();
                }
                ParserImpl.this.state = new ParseBlockMappingValue();
                return ParserImpl.processEmptyScalar(token.getEndMark());
            }
            if (!ParserImpl.this.scanner.checkToken(Token.ID.BlockEnd)) {
                Token token = ParserImpl.this.scanner.peekToken();
                throw new ParserException("while parsing a block mapping", ParserImpl.this.markPop(), "expected <block end>, but found '" + token.getTokenId() + '\'', token.getStartMark());
            }
            Token token = ParserImpl.this.scanner.next();
            MappingEndEvent event = new MappingEndEvent(token.getStartMark(), token.getEndMark());
            ParserImpl.this.state = ParserImpl.this.states.pop();
            ParserImpl.this.markPop();
            return event;
        }
    }

    final class ParseIndentlessSequenceEntryValue
    implements Production {
        final BlockEntryToken token;

        public ParseIndentlessSequenceEntryValue(BlockEntryToken token) {
            this.token = token;
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = new ParseIndentlessSequenceEntryValue(this.token);
                return ParserImpl.produceCommentEvent((CommentToken)ParserImpl.this.scanner.next());
            }
            if (!ParserImpl.this.scanner.checkToken(Token.ID.BlockEntry, Token.ID.Value, Token.ID.BlockEnd)) {
                ParserImpl.this.states.push(new ParseIndentlessSequenceEntryKey());
                return ParserImpl.this.parseBlockNode();
            }
            ParserImpl.this.state = new ParseIndentlessSequenceEntryKey();
            return ParserImpl.processEmptyScalar(this.token.getEndMark());
        }
    }

    final class ParseBlockSequenceEntryValue
    implements Production {
        final BlockEntryToken token;

        public ParseBlockSequenceEntryValue(BlockEntryToken token) {
            this.token = token;
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = new ParseBlockSequenceEntryValue(this.token);
                return ParserImpl.produceCommentEvent((CommentToken)ParserImpl.this.scanner.next());
            }
            if (!ParserImpl.this.scanner.checkToken(Token.ID.BlockEntry, Token.ID.BlockEnd)) {
                ParserImpl.this.states.push(new ParseBlockSequenceEntryKey());
                return ParserImpl.this.parseBlockNode();
            }
            ParserImpl.this.state = new ParseBlockSequenceEntryKey();
            return ParserImpl.processEmptyScalar(this.token.getEndMark());
        }
    }

    final class ParseBlockSequenceEntryKey
    implements Production {
        ParseBlockSequenceEntryKey() {
        }

        @Override
        public Event produce() {
            if (ParserImpl.this.scanner.checkToken(Token.ID.Comment)) {
                ParserImpl.this.state = new ParseBlockSequenceEntryKey();
                return ParserImpl.produceCommentEvent((CommentToken)ParserImpl.this.scanner.next());
            }
            if (ParserImpl.this.scanner.checkToken(Token.ID.BlockEntry)) {
                BlockEntryToken token = (BlockEntryToken)ParserImpl.this.scanner.next();
                return new ParseBlockSequenceEntryValue(token).produce();
            }
            if (!ParserImpl.this.scanner.checkToken(Token.ID.BlockEnd)) {
                Token token = ParserImpl.this.scanner.peekToken();
                throw new ParserException("while parsing a block collection", ParserImpl.this.markPop(), "expected <block end>, but found '" + token.getTokenId() + '\'', token.getStartMark());
            }
            Token token = ParserImpl.this.scanner.next();
            SequenceEndEvent event = new SequenceEndEvent(token.getStartMark(), token.getEndMark());
            ParserImpl.this.state = ParserImpl.this.states.pop();
            ParserImpl.this.markPop();
            return event;
        }
    }

    final class ParseDocumentEnd
    implements Production {
        ParseDocumentEnd() {
        }

        @Override
        public Event produce() {
            Token token = ParserImpl.this.scanner.peekToken();
            Mark startMark = token.getStartMark();
            return new DocumentEndEvent(startMark, startMark);
        }
    }
}

