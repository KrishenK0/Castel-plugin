
package fr.krishenk.castel.libs.snakeyaml.emitter;

import fr.krishenk.castel.libs.snakeyaml.api.DumpSettings;
import fr.krishenk.castel.libs.snakeyaml.api.StreamDataWriter;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentEventsCollector;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentLine;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentType;
import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.common.ArrayStack;
import fr.krishenk.castel.libs.snakeyaml.common.CharConstants;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.events.*;
import fr.krishenk.castel.libs.snakeyaml.exceptions.EmitterException;
import fr.krishenk.castel.libs.snakeyaml.exceptions.YamlEngineException;
import fr.krishenk.castel.libs.snakeyaml.scanner.StreamReader;

import java.util.*;

public final class Emitter
implements Emitable {
    static final Map<Character, String> ESCAPE_REPLACEMENTS = new HashMap<Character, String>(15);
    final StreamDataWriter stream;
    final ArrayStack<EmitterState> states = new ArrayStack(100);
    EmitterState state = new ExpectDocumentStart();
    final Queue<Event> events = new ArrayDeque<Event>(100);
    Event event;
    final ArrayStack<Integer> indents = new ArrayStack(10);
    Integer indent;
    boolean mappingContext;
    boolean simpleKeyContext;
    int flowLevel;
    int column;
    boolean whitespace = true;
    boolean indention = true;
    Optional<Anchor> preparedAnchor;
    ScalarAnalysis analysis;
    final DumpSettings settings;
    static final boolean splitLines = true;
    final CommentEventsCollector blockCommentsCollector;
    final CommentEventsCollector inlineCommentsCollector;

    public Emitter(DumpSettings opts, StreamDataWriter stream) {
        this.stream = stream;
        this.settings = opts;
        this.preparedAnchor = Optional.empty();
        this.analysis = null;
        this.blockCommentsCollector = new CommentEventsCollector(this.events, CommentType.BLANK_LINE, CommentType.BLOCK);
        this.inlineCommentsCollector = new CommentEventsCollector(this.events, CommentType.IN_LINE);
    }

    @Override
    public void emit(Event event) {
        this.events.add(event);
        while (!this.needMoreEvents()) {
            this.event = this.events.poll();
            this.state.expect();
            this.event = null;
        }
    }

    boolean needMoreEvents() {
        if (this.events.isEmpty()) {
            return true;
        }
        Iterator<Event> iter = this.events.iterator();
        Event event = (Event)iter.next();
        while (event instanceof CommentEvent) {
            if (!iter.hasNext()) {
                return true;
            }
            event = (Event)iter.next();
        }
        if (event instanceof SequenceStartEvent) {
            return Emitter.needEvents(iter, 2);
        }
        if (event instanceof MappingStartEvent) {
            return Emitter.needEvents(iter, 3);
        }
        return Emitter.needEvents(iter, 1);
    }

    static boolean needEvents(Iterator<Event> iter, int count) {
        int level = 0;
        int actualCount = 0;
        while (iter.hasNext()) {
            Event event = iter.next();
            if (event instanceof CommentEvent) continue;
            ++actualCount;
            if (event instanceof CollectionStartEvent) {
                ++level;
            } else if (event instanceof CollectionEndEvent) {
                --level;
            }
            if (level >= 0) continue;
            return false;
        }
        return actualCount < count;
    }

    void increaseIndent(boolean isFlow, boolean indentless) {
        this.indents.push(this.indent);
        if (this.indent == null) {
            this.indent = isFlow ? Integer.valueOf(this.settings.getIndent()) : Integer.valueOf(0);
        } else if (!indentless) {
            this.indent = this.indent + this.settings.getIndent();
        }
    }

    void expectNode(boolean mapping, boolean simpleKey) {
        this.mappingContext = mapping;
        this.simpleKeyContext = simpleKey;
        if (this.event.getEventId() == Event.ID.Alias) {
            this.expectAlias();
        } else if (this.event.getEventId() == Event.ID.Scalar || this.event.getEventId() == Event.ID.SequenceStart || this.event.getEventId() == Event.ID.MappingStart) {
            this.processAnchor("&");
            this.handleNodeEvent(this.event.getEventId());
        } else {
            throw new EmitterException("expected NodeEvent, but got " + (Object)((Object)this.event.getEventId()));
        }
    }

    void handleNodeEvent(Event.ID id) {
        switch (id) {
            case Scalar: {
                this.expectScalar();
                break;
            }
            case SequenceStart: {
                if (this.flowLevel != 0 || ((SequenceStartEvent)this.event).isFlow() || this.checkEmptySequence()) {
                    this.expectFlowSequence();
                    break;
                }
                this.expectBlockSequence();
                break;
            }
            case MappingStart: {
                if (this.checkEmptyMapping()) {
                    this.expectBlockMapping();
                    break;
                }
                if (this.flowLevel != 0 || ((MappingStartEvent)this.event).isFlow()) {
                    this.expectFlowMapping();
                    break;
                }
                this.expectBlockMapping();
                break;
            }
            default: {
                throw new EmitterException("Unexpected node to handle: " + id.name());
            }
        }
    }

    void expectAlias() {
        if (!(this.event instanceof AliasEvent)) {
            throw new EmitterException("Expecting Alias.");
        }
        this.processAnchor("*");
        this.state = this.states.pop();
    }

    void expectScalar() {
        this.increaseIndent(true, false);
        this.processScalar();
        this.indent = this.indents.pop();
        this.state = this.states.pop();
    }

    void expectFlowSequence() {
        this.writeIndicator("[", true, true, false);
        ++this.flowLevel;
        this.increaseIndent(true, false);
        this.state = new ExpectFirstFlowSequenceItem();
    }

    void expectFlowMapping() {
        this.writeIndicator("{", true, true, false);
        ++this.flowLevel;
        this.increaseIndent(true, false);
        this.state = new ExpectFirstFlowMappingKey();
    }

    void expectBlockSequence() {
        boolean indentless = this.mappingContext && !this.indention;
        this.increaseIndent(false, indentless);
        this.state = new ExpectFirstBlockSequenceItem();
    }

    void expectBlockMapping() {
        this.increaseIndent(false, false);
        this.state = new ExpectFirstBlockMappingKey();
    }

    static boolean isFoldedOrLiteral(Event event) {
        if (event.getEventId() != Event.ID.Scalar) {
            return false;
        }
        ScalarEvent scalarEvent = (ScalarEvent)event;
        ScalarStyle style = scalarEvent.getScalarStyle();
        return style == ScalarStyle.FOLDED || style == ScalarStyle.LITERAL;
    }

    boolean checkEmptySequence() {
        return this.event.getEventId() == Event.ID.SequenceStart && !this.events.isEmpty() && this.events.peek().getEventId() == Event.ID.SequenceEnd;
    }

    boolean checkEmptyMapping() {
        return this.event.getEventId() == Event.ID.MappingStart && !this.events.isEmpty() && this.events.peek().getEventId() == Event.ID.MappingEnd;
    }

    void processAnchor(String indicator) {
        NodeEvent ev = (NodeEvent)this.event;
        Optional<Anchor> anchorOption = ev.getAnchor();
        if (anchorOption.isPresent()) {
            Anchor anchor = anchorOption.get();
            if (!this.preparedAnchor.isPresent()) {
                this.preparedAnchor = anchorOption;
            }
            this.writeIndicator(indicator + anchor, true, false, false);
        }
        this.preparedAnchor = Optional.empty();
    }

    ScalarStyle chooseScalarStyle(ScalarEvent ev) {
        if (this.analysis.isEmpty()) {
            return ScalarStyle.SINGLE_QUOTED;
        }
        if (this.analysis.isMultiline()) {
            return ScalarStyle.LITERAL;
        }
        if (this.flowLevel != 0 ? this.analysis.isAllowFlowPlain() : this.analysis.isAllowBlockPlain()) {
            return ScalarStyle.PLAIN;
        }
        if (this.analysis.isAllowSingleQuoted()) {
            return ScalarStyle.SINGLE_QUOTED;
        }
        return ScalarStyle.DOUBLE_QUOTED;
    }

    void processScalar() {
        ScalarStyle scalarStyle;
        ScalarEvent ev = (ScalarEvent)this.event;
        String value = ev.getValue();
        if (this.analysis == null) {
            this.analysis = this.analyzeScalar(value);
        }
        if ((scalarStyle = ev.getScalarStyle()) == ScalarStyle.AUTO) {
            scalarStyle = this.chooseScalarStyle(ev);
        }
        boolean split = !this.simpleKeyContext;
        switch (scalarStyle) {
            case DOUBLE_QUOTED: {
                this.writeDoubleQuoted(value, split);
                break;
            }
            case SINGLE_QUOTED: {
                this.writeSingleQuoted(value, split);
                break;
            }
            case FOLDED: {
                this.writeFolded(value, split);
                break;
            }
            case LITERAL: {
                this.writeLiteral(value);
                break;
            }
            case PLAIN: {
                this.writePlain(value, split);
                break;
            }
            default: {
                throw new YamlEngineException("Unknown scalar style: " + (Object)((Object)scalarStyle));
            }
        }
        this.analysis = null;
    }

    ScalarAnalysis analyzeScalar(String scalar) {
        if (scalar.isEmpty()) {
            return new ScalarAnalysis(true, false, false, true, true, false);
        }
        boolean blockIndicators = false;
        boolean flowIndicators = false;
        boolean lineBreaks = false;
        boolean specialCharacters = false;
        boolean leadingSpace = false;
        boolean leadingBreak = false;
        boolean trailingSpace = false;
        boolean trailingBreak = false;
        boolean breakSpace = false;
        boolean spaceBreak = false;
        boolean preceededByWhitespace = true;
        boolean followedByWhitespace = scalar.length() == 1 || CharConstants.NULL_BL_T_LINEBR.has(scalar.codePointAt(1));
        boolean previousSpace = false;
        boolean previousBreak = false;
        int index = 0;
        while (index < scalar.length()) {
            int nextIndex;
            boolean isLineBreak;
            int c = scalar.codePointAt(index);
            if (index == 0) {
                if ("#,[]{}&*!|>'\"%@`".indexOf(c) != -1) {
                    flowIndicators = true;
                    blockIndicators = true;
                }
                if (c == 58) {
                    flowIndicators = true;
                    if (followedByWhitespace) {
                        blockIndicators = true;
                    }
                }
                if (c == 45 && followedByWhitespace) {
                    flowIndicators = true;
                    blockIndicators = true;
                }
            } else {
                if (",[]{}".indexOf(c) != -1) {
                    flowIndicators = true;
                }
                if (c == 58) {
                    flowIndicators = true;
                    if (followedByWhitespace) {
                        blockIndicators = true;
                    }
                }
                if (c == 35 && preceededByWhitespace) {
                    flowIndicators = true;
                    blockIndicators = true;
                }
            }
            if (isLineBreak = CharConstants.LINEBR.has(c)) {
                lineBreaks = true;
            }
            if (c != 10 && (32 > c || c > 126)) {
                if (c == 133 || c >= 160 && c <= 55295 || c >= 57344 && c <= 65533 || c >= 65536 && c <= 0x10FFFF) {
                    if (!this.settings.isUseUnicodeEncoding()) {
                        specialCharacters = true;
                    }
                } else {
                    specialCharacters = true;
                }
            }
            if (c == 32) {
                if (index == 0) {
                    leadingSpace = true;
                }
                if (index == scalar.length() - 1) {
                    trailingSpace = true;
                }
                if (previousBreak) {
                    breakSpace = true;
                }
                previousSpace = true;
                previousBreak = false;
            } else if (isLineBreak) {
                if (index == 0) {
                    leadingBreak = true;
                }
                if (index == scalar.length() - 1) {
                    trailingBreak = true;
                }
                if (previousSpace) {
                    spaceBreak = true;
                }
                previousSpace = false;
                previousBreak = true;
            } else {
                previousSpace = false;
                previousBreak = false;
            }
            preceededByWhitespace = CharConstants.NULL_BL_T.has(c) || isLineBreak;
            followedByWhitespace = true;
            if ((index += Character.charCount(c)) + 1 >= scalar.length() || (nextIndex = index + Character.charCount(scalar.codePointAt(index))) >= scalar.length()) continue;
            followedByWhitespace = CharConstants.NULL_BL_T.has(scalar.codePointAt(nextIndex)) || isLineBreak;
        }
        boolean allowFlowPlain = true;
        boolean allowBlockPlain = true;
        boolean allowSingleQuoted = true;
        boolean allowBlock = true;
        if (leadingSpace || leadingBreak || trailingSpace || trailingBreak) {
            allowBlockPlain = false;
            allowFlowPlain = false;
        }
        if (trailingSpace) {
            allowBlock = false;
        }
        if (breakSpace) {
            allowSingleQuoted = false;
            allowBlockPlain = false;
            allowFlowPlain = false;
        }
        if (spaceBreak || specialCharacters) {
            allowBlock = false;
            allowSingleQuoted = false;
            allowBlockPlain = false;
            allowFlowPlain = false;
        }
        if (lineBreaks) {
            allowFlowPlain = false;
        }
        if (flowIndicators) {
            allowFlowPlain = false;
        }
        if (blockIndicators) {
            allowBlockPlain = false;
        }
        return new ScalarAnalysis(false, lineBreaks, allowFlowPlain, allowBlockPlain, allowSingleQuoted, allowBlock);
    }

    void writeStreamStart() {
    }

    void writeStreamEnd() {
        this.stream.closeWriter();
    }

    void writeIndicator(String indicator, boolean needWhitespace, boolean whitespace, boolean indentation) {
        if (!this.whitespace && needWhitespace) {
            this.write(" ");
        }
        this.whitespace = whitespace;
        this.indention = this.indention && indentation;
        this.write(indicator);
    }

    void writeIndent() {
        int indentToWrite = this.indent != null ? this.indent : 0;
        if (!this.indention || this.column > indentToWrite || this.column == indentToWrite && !this.whitespace) {
            this.writeLineBreak(null);
        }
        this.writeWhitespace(indentToWrite - this.column);
    }

    void writeWhitespace(int length) {
        if (length <= 0) {
            return;
        }
        this.whitespace = true;
        for (int i = 0; i < length; ++i) {
            this.stream.write(" ");
        }
        this.column += length;
    }

    void writeLineBreak(String data) {
        this.whitespace = true;
        this.indention = true;
        this.column = 0;
        if (data == null) {
            this.stream.write("\n");
        } else {
            this.stream.write(data);
        }
    }

    void writeSingleQuoted(String text, boolean split) {
        this.writeIndicator("'", true, false, false);
        boolean spaces = false;
        boolean breaks = false;
        int start = 0;
        for (int end = 0; end <= text.length(); ++end) {
            int len;
            char ch = '\u0000';
            if (end < text.length()) {
                ch = text.charAt(end);
            }
            if (spaces) {
                if (ch == '\u0000' || ch != ' ') {
                    if (start + 1 == end && this.column > this.settings.getWidth() && split && start != 0 && end != text.length()) {
                        this.writeIndent();
                    } else {
                        len = end - start;
                        this.write(text, start, len);
                    }
                    start = end;
                }
            } else if (breaks) {
                if (ch == '\u0000' || CharConstants.LINEBR.hasNo(ch)) {
                    if (text.charAt(start) == '\n') {
                        this.writeLineBreak(null);
                    }
                    String data = text.substring(start, end);
                    for (char br : data.toCharArray()) {
                        if (br == '\n') {
                            this.writeLineBreak(null);
                            continue;
                        }
                        this.writeLineBreak(String.valueOf(br));
                    }
                    this.writeIndent();
                    start = end;
                }
            } else if (CharConstants.LINEBR.has(ch, "\u0000 '") && start < end) {
                len = end - start;
                this.write(text, start, len);
                start = end;
            }
            if (ch == '\'') {
                this.write("''");
                start = end + 1;
            }
            if (ch == '\u0000') continue;
            spaces = ch == ' ';
            breaks = CharConstants.LINEBR.has(ch);
        }
        this.writeIndicator("'", false, false, false);
    }

    void writeDoubleQuoted(String text, boolean split) {
        this.writeIndicator("\"", true, false, false);
        int start = 0;
        for (int end = 0; end <= text.length(); ++end) {
            Character ch = null;
            if (end < text.length()) {
                ch = Character.valueOf(text.charAt(end));
            }
            if (ch == null || "\"\\\u0085\u2028\u2029\ufeff".indexOf(ch.charValue()) != -1 || ' ' > ch.charValue() || ch.charValue() > '~') {
                if (start < end) {
                    int len = end - start;
                    this.write(text, start, len);
                    start = end;
                }
                if (ch != null) {
                    String data;
                    if (ESCAPE_REPLACEMENTS.containsKey(ch)) {
                        data = '\\' + ESCAPE_REPLACEMENTS.get(ch);
                    } else if (!this.settings.isUseUnicodeEncoding() || !StreamReader.isPrintable(ch.charValue())) {
                        String s;
                        if (ch.charValue() <= '\u00ff') {
                            s = '0' + Integer.toString(ch.charValue(), 16);
                            data = "\\x" + s.substring(s.length() - 2);
                        } else if (ch.charValue() >= '\ud800' && ch.charValue() <= '\udbff') {
                            if (end + 1 < text.length()) {
                                char ch2 = text.charAt(++end);
                                String s2 = "000" + Long.toHexString(Character.toCodePoint(ch.charValue(), ch2));
                                data = "\\U" + s2.substring(s2.length() - 8);
                            } else {
                                s = "000" + Integer.toString(ch.charValue(), 16);
                                data = "\\u" + s.substring(s.length() - 4);
                            }
                        } else {
                            s = "000" + Integer.toString(ch.charValue(), 16);
                            data = "\\u" + s.substring(s.length() - 4);
                        }
                    } else {
                        data = String.valueOf(ch);
                    }
                    this.write(data);
                    start = end + 1;
                }
            }
            if (0 >= end || end >= text.length() - 1 || ch.charValue() != ' ' && start < end || this.column + (end - start) <= this.settings.getWidth() || !split) continue;
            String data = start >= end ? "\\" : text.substring(start, end) + '\\';
            if (start < end) {
                start = end;
            }
            this.write(data);
            this.writeIndent();
            this.whitespace = false;
            this.indention = false;
            if (text.charAt(start) != ' ') continue;
            data = "\\";
            this.write(data);
        }
        this.writeIndicator("\"", false, false, false);
    }

    void write(String str, int off, int len) {
        this.column += len;
        this.stream.write(str, off, len);
    }

    void write(String data) {
        this.column += data.length();
        this.stream.write(data);
    }

    boolean writeCommentLines(List<CommentLine> commentLines) {
        boolean wroteComment = false;
        boolean firstComment = true;
        int indentColumns = 0;
        for (CommentLine commentLine : commentLines) {
            if (commentLine.getCommentType() != CommentType.BLANK_LINE) {
                this.writeWhitespace(indentColumns);
                if (firstComment) {
                    firstComment = false;
                    this.writeIndicator("#", commentLine.getCommentType() == CommentType.IN_LINE, false, false);
                    indentColumns = this.column > 0 ? this.column - 1 : 0;
                } else {
                    this.writeIndicator("#", false, false, false);
                }
                this.stream.write(commentLine.getValue());
                this.writeLineBreak(null);
            } else {
                if (firstComment && indentColumns == 0) {
                    indentColumns = this.column;
                }
                this.writeLineBreak(null);
            }
            wroteComment = true;
        }
        return wroteComment;
    }

    void writeBlockComment() {
        if (!this.blockCommentsCollector.isEmpty()) {
            this.writeIndent();
            this.writeCommentLines(this.blockCommentsCollector.consume());
        }
    }

    boolean writeInlineComments() {
        return this.writeCommentLines(this.inlineCommentsCollector.consume());
    }

    void writeFolded(String text, boolean split) {
        this.writeIndicator(">", true, false, false);
        if (!this.writeInlineComments()) {
            this.writeLineBreak(null);
        }
        boolean leadingSpace = true;
        boolean spaces = false;
        boolean breaks = true;
        int start = 0;
        for (int end = 0; end <= text.length(); ++end) {
            char ch = '\u0000';
            if (end < text.length()) {
                ch = text.charAt(end);
            }
            if (breaks) {
                if (ch == '\u0000' || CharConstants.LINEBR.hasNo(ch)) {
                    if (!leadingSpace && ch != '\u0000' && ch != ' ' && text.charAt(start) == '\n') {
                        this.writeLineBreak(null);
                    }
                    leadingSpace = ch == ' ';
                    String data = text.substring(start, end);
                    for (char br : data.toCharArray()) {
                        if (br == '\n') {
                            this.writeLineBreak(null);
                            continue;
                        }
                        this.writeLineBreak(String.valueOf(br));
                    }
                    if (ch != '\u0000') {
                        this.writeIndent();
                    }
                    start = end;
                }
            } else if (spaces) {
                if (ch != ' ') {
                    if (start + 1 == end && this.column > this.settings.getWidth() && split) {
                        this.writeIndent();
                    } else {
                        int len = end - start;
                        this.write(text, start, len);
                    }
                    start = end;
                }
            } else if (CharConstants.LINEBR.has(ch, "\u0000 ")) {
                int len = end - start;
                this.write(text, start, len);
                if (ch == '\u0000') {
                    this.writeLineBreak(null);
                }
                start = end;
            }
            if (ch == '\u0000') continue;
            breaks = CharConstants.LINEBR.has(ch);
            spaces = ch == ' ';
        }
    }

    void writeLiteral(String text) {
        this.writeIndicator("|", true, false, false);
        if (!this.writeInlineComments()) {
            this.writeLineBreak(null);
        }
        boolean breaks = true;
        int start = 0;
        for (int end = 0; end <= text.length(); ++end) {
            char ch;
            char c = ch = end < text.length() ? text.charAt(end) : (char)'\u0000';
            if (breaks) {
                if (ch == '\u0000' || CharConstants.LINEBR.hasNo(ch)) {
                    String data = text.substring(start, end);
                    for (char br : data.toCharArray()) {
                        if (br == '\n') {
                            this.writeLineBreak(null);
                            continue;
                        }
                        this.writeLineBreak(String.valueOf(br));
                    }
                    if (ch != '\u0000') {
                        this.writeIndent();
                    }
                    start = end;
                }
            } else if (ch == '\u0000' || CharConstants.LINEBR.has(ch)) {
                this.write(text, start, end - start);
                if (ch == '\u0000') {
                    this.writeLineBreak(null);
                }
                start = end;
            }
            if (ch == '\u0000') continue;
            breaks = CharConstants.LINEBR.has(ch);
        }
    }

    void writePlain(String text, boolean split) {
        if (text.isEmpty()) {
            return;
        }
        if (!this.whitespace) {
            this.write(" ");
        }
        this.whitespace = false;
        this.indention = false;
        boolean spaces = false;
        boolean breaks = false;
        int start = 0;
        for (int end = 0; end <= text.length(); ++end) {
            int len;
            char ch = '\u0000';
            if (end < text.length()) {
                ch = text.charAt(end);
            }
            if (spaces) {
                if (ch != ' ') {
                    if (start + 1 == end && this.column > this.settings.getWidth() && split) {
                        this.writeIndent();
                        this.whitespace = false;
                        this.indention = false;
                    } else {
                        len = end - start;
                        this.write(text, start, len);
                    }
                    start = end;
                }
            } else if (breaks) {
                if (CharConstants.LINEBR.hasNo(ch)) {
                    if (text.charAt(start) == '\n') {
                        this.writeLineBreak(null);
                    }
                    String data = text.substring(start, end);
                    for (char br : data.toCharArray()) {
                        if (br == '\n') {
                            this.writeLineBreak(null);
                            continue;
                        }
                        this.writeLineBreak(String.valueOf(br));
                    }
                    this.writeIndent();
                    this.whitespace = false;
                    this.indention = false;
                    start = end;
                }
            } else if (CharConstants.LINEBR.has(ch, "\u0000 ")) {
                len = end - start;
                this.write(text, start, len);
                start = end;
            }
            if (ch == '\u0000') continue;
            spaces = ch == ' ';
            breaks = CharConstants.LINEBR.has(ch);
        }
    }

    static {
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\u0000'), "0");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\u0007'), "a");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\b'), "b");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\t'), "t");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\n'), "n");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\u000b'), "v");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\f'), "f");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\r'), "r");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\u001b'), "e");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\"'), "\"");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\\'), "\\");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\u0085'), "N");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\u00a0'), "_");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\u2028'), "L");
        ESCAPE_REPLACEMENTS.put(Character.valueOf('\u2029'), "P");
    }

    class ExpectDocumentStart
    implements EmitterState {
        ExpectDocumentStart() {
        }

        @Override
        public void expect() {
            if (Emitter.this.event.getEventId() == Event.ID.DocumentEnd) {
                Emitter.this.writeStreamEnd();
                Emitter.this.state = new ExpectNothing();
            } else if (Emitter.this.event instanceof CommentEvent) {
                Emitter.this.blockCommentsCollector.collectEvents(Emitter.this.event);
                Emitter.this.writeBlockComment();
            } else {
                Emitter.this.expectNode(false, false);
            }
        }
    }

    class ExpectFirstFlowSequenceItem
    implements EmitterState {
        ExpectFirstFlowSequenceItem() {
        }

        @Override
        public void expect() {
            if (Emitter.this.event.getEventId() == Event.ID.SequenceEnd) {
                Emitter.this.indent = Emitter.this.indents.pop();
                --Emitter.this.flowLevel;
                Emitter.this.writeIndicator("]", false, false, false);
                Emitter.this.inlineCommentsCollector.collectEvents();
                Emitter.this.writeInlineComments();
                Emitter.this.state = Emitter.this.states.pop();
            } else if (Emitter.this.event instanceof CommentEvent) {
                Emitter.this.blockCommentsCollector.collectEvents(Emitter.this.event);
                Emitter.this.writeBlockComment();
            } else {
                if (Emitter.this.column > Emitter.this.settings.getWidth()) {
                    Emitter.this.writeIndent();
                }
                Emitter.this.states.push(new ExpectFlowSequenceItem());
                Emitter.this.expectNode(false, false);
                Emitter.this.event = Emitter.this.inlineCommentsCollector.collectEvents(Emitter.this.event);
                Emitter.this.writeInlineComments();
            }
        }
    }

    class ExpectFirstFlowMappingKey
    implements EmitterState {
        ExpectFirstFlowMappingKey() {
        }

        @Override
        public void expect() {
            Emitter.this.event = Emitter.this.blockCommentsCollector.collectEventsAndPoll(Emitter.this.event);
            Emitter.this.writeBlockComment();
            if (Emitter.this.event.getEventId() == Event.ID.MappingEnd) {
                Emitter.this.indent = Emitter.this.indents.pop();
                --Emitter.this.flowLevel;
                Emitter.this.writeIndicator("}", false, false, false);
                Emitter.this.inlineCommentsCollector.collectEvents();
                Emitter.this.writeInlineComments();
                Emitter.this.state = Emitter.this.states.pop();
            } else {
                if (Emitter.this.column > Emitter.this.settings.getWidth()) {
                    Emitter.this.writeIndent();
                }
                Emitter.this.states.push(new ExpectFlowMappingSimpleValue());
                Emitter.this.expectNode(true, true);
            }
        }
    }

    class ExpectFirstBlockSequenceItem
    implements EmitterState {
        ExpectFirstBlockSequenceItem() {
        }

        @Override
        public void expect() {
            new ExpectBlockSequenceItem(true).expect();
        }
    }

    final class ExpectFirstBlockMappingKey
    implements EmitterState {
        ExpectFirstBlockMappingKey() {
        }

        @Override
        public void expect() {
            new ExpectBlockMappingKey(true).expect();
        }
    }

    class ExpectBlockMappingSimpleValue
    implements EmitterState {
        ExpectBlockMappingSimpleValue() {
        }

        @Override
        public void expect() {
            Emitter.this.writeIndicator(":", false, false, false);
            Emitter.this.event = Emitter.this.inlineCommentsCollector.collectEventsAndPoll(Emitter.this.event);
            if (!Emitter.isFoldedOrLiteral(Emitter.this.event) && Emitter.this.writeInlineComments()) {
                Emitter.this.increaseIndent(true, false);
                Emitter.this.writeIndent();
                Emitter.this.indent = Emitter.this.indents.pop();
            }
            Emitter.this.event = Emitter.this.blockCommentsCollector.collectEventsAndPoll(Emitter.this.event);
            if (!Emitter.this.blockCommentsCollector.isEmpty()) {
                Emitter.this.increaseIndent(true, false);
                Emitter.this.writeBlockComment();
                Emitter.this.writeIndent();
                Emitter.this.indent = Emitter.this.indents.pop();
            }
            Emitter.this.states.push(new ExpectBlockMappingKey(false));
            Emitter.this.expectNode(true, false);
            Emitter.this.inlineCommentsCollector.collectEvents();
            Emitter.this.writeInlineComments();
        }
    }

    final class ExpectBlockMappingKey
    implements EmitterState {
        final boolean first;

        public ExpectBlockMappingKey(boolean first) {
            this.first = first;
        }

        @Override
        public void expect() {
            Emitter.this.event = Emitter.this.blockCommentsCollector.collectEventsAndPoll(Emitter.this.event);
            Emitter.this.writeBlockComment();
            if (this.first && Emitter.this.event.getEventId() == Event.ID.MappingEnd) {
                Emitter.this.indent = Emitter.this.indents.pop();
                Emitter.this.state = Emitter.this.states.pop();
            } else if (!this.first && Emitter.this.event.getEventId() == Event.ID.MappingEnd) {
                Emitter.this.indent = Emitter.this.indents.pop();
                Emitter.this.state = Emitter.this.states.pop();
            } else {
                Emitter.this.writeIndent();
                Emitter.this.states.push(new ExpectBlockMappingSimpleValue());
                Emitter.this.expectNode(true, true);
            }
        }
    }

    class ExpectBlockSequenceItem
    implements EmitterState {
        final boolean first;

        public ExpectBlockSequenceItem(boolean first) {
            this.first = first;
        }

        @Override
        public void expect() {
            if (!this.first && Emitter.this.event.getEventId() == Event.ID.SequenceEnd) {
                Emitter.this.indent = Emitter.this.indents.pop();
                Emitter.this.state = Emitter.this.states.pop();
            } else if (Emitter.this.event instanceof CommentEvent) {
                Emitter.this.blockCommentsCollector.collectEvents(Emitter.this.event);
            } else {
                if (this.first) {
                    Emitter.this.indent = Emitter.this.indent + Emitter.this.settings.getIndicatorIndent();
                }
                Emitter.this.writeIndent();
                Emitter.this.writeIndicator("-", true, false, true);
                if (!Emitter.this.blockCommentsCollector.isEmpty()) {
                    Emitter.this.increaseIndent(false, false);
                    Emitter.this.writeBlockComment();
                    if (Emitter.this.event instanceof ScalarEvent) {
                        Emitter.this.analysis = Emitter.this.analyzeScalar(((ScalarEvent)Emitter.this.event).getValue());
                        if (!Emitter.this.analysis.isEmpty()) {
                            Emitter.this.writeIndent();
                        }
                    }
                    Emitter.this.indent = Emitter.this.indents.pop();
                }
                Emitter.this.states.push(new ExpectBlockSequenceItem(false));
                Emitter.this.expectNode(false, false);
                Emitter.this.inlineCommentsCollector.collectEvents();
                Emitter.this.writeInlineComments();
            }
        }
    }

    class ExpectFlowMappingSimpleValue
    implements EmitterState {
        ExpectFlowMappingSimpleValue() {
        }

        @Override
        public void expect() {
            Emitter.this.writeIndicator(":", false, false, false);
            Emitter.this.event = Emitter.this.inlineCommentsCollector.collectEventsAndPoll(Emitter.this.event);
            Emitter.this.writeInlineComments();
            Emitter.this.states.push(new ExpectFlowMappingKey());
            Emitter.this.expectNode(true, false);
            Emitter.this.inlineCommentsCollector.collectEvents(Emitter.this.event);
            Emitter.this.writeInlineComments();
        }
    }

    class ExpectFlowMappingKey
    implements EmitterState {
        ExpectFlowMappingKey() {
        }

        @Override
        public void expect() {
            if (Emitter.this.event.getEventId() == Event.ID.MappingEnd) {
                Emitter.this.indent = Emitter.this.indents.pop();
                --Emitter.this.flowLevel;
                Emitter.this.writeIndicator("}", false, false, false);
                Emitter.this.inlineCommentsCollector.collectEvents();
                Emitter.this.writeInlineComments();
                Emitter.this.state = Emitter.this.states.pop();
            } else {
                Emitter.this.writeIndicator(",", false, false, false);
                Emitter.this.event = Emitter.this.blockCommentsCollector.collectEventsAndPoll(Emitter.this.event);
                Emitter.this.writeBlockComment();
                if (Emitter.this.column > Emitter.this.settings.getWidth()) {
                    Emitter.this.writeIndent();
                }
                Emitter.this.states.push(new ExpectFlowMappingSimpleValue());
                Emitter.this.expectNode(true, true);
            }
        }
    }

    class ExpectFlowSequenceItem
    implements EmitterState {
        ExpectFlowSequenceItem() {
        }

        @Override
        public void expect() {
            if (Emitter.this.event.getEventId() == Event.ID.SequenceEnd) {
                Emitter.this.indent = Emitter.this.indents.pop();
                --Emitter.this.flowLevel;
                Emitter.this.writeIndicator("]", false, false, false);
                Emitter.this.inlineCommentsCollector.collectEvents();
                Emitter.this.writeInlineComments();
                Emitter.this.state = Emitter.this.states.pop();
            } else if (Emitter.this.event instanceof CommentEvent) {
                Emitter.this.event = Emitter.this.blockCommentsCollector.collectEvents(Emitter.this.event);
            } else {
                Emitter.this.writeIndicator(",", false, false, false);
                Emitter.this.writeBlockComment();
                if (Emitter.this.column > Emitter.this.settings.getWidth()) {
                    Emitter.this.writeIndent();
                }
                Emitter.this.states.push(new ExpectFlowSequenceItem());
                Emitter.this.expectNode(false, false);
                Emitter.this.event = Emitter.this.inlineCommentsCollector.collectEvents(Emitter.this.event);
                Emitter.this.writeInlineComments();
            }
        }
    }

    class ExpectNothing
    implements EmitterState {
        ExpectNothing() {
        }

        @Override
        public void expect() {
            throw new EmitterException("expecting nothing, but got " + Emitter.this.event);
        }
    }
}

