
package fr.krishenk.castel.libs.snakeyaml.scanner;

import fr.krishenk.castel.libs.snakeyaml.api.LoadSettings;
import fr.krishenk.castel.libs.snakeyaml.comments.CommentType;
import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.common.ArrayStack;
import fr.krishenk.castel.libs.snakeyaml.common.CharConstants;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;
import fr.krishenk.castel.libs.snakeyaml.exceptions.ScannerException;
import fr.krishenk.castel.libs.snakeyaml.exceptions.YamlEngineException;
import fr.krishenk.castel.libs.snakeyaml.tokens.*;

import java.util.*;
import java.util.regex.Pattern;

public final class ScannerImpl
implements Scanner {
    private static final String SCANNING_SCALAR = "while scanning a block scalar";
    private static final Pattern NOT_HEXA = Pattern.compile("[^0-9A-Fa-f]");
    private final StreamReader reader;
    private boolean done;
    private int flowLevel = 0;
    private final List<Token> tokens;
    private Token lastToken;
    private int tokensTaken = 0;
    private int indent = -1;
    private final ArrayStack<Integer> indents;
    private boolean allowSimpleKey = true;
    private final Map<Integer, SimpleKey> possibleSimpleKeys;
    private final LoadSettings settings;

    public ScannerImpl(LoadSettings settings, StreamReader reader) {
        this.reader = reader;
        this.settings = settings;
        this.tokens = new ArrayList<>(100);
        this.indents = new ArrayStack<>(10);
        this.possibleSimpleKeys = new LinkedHashMap<Integer, SimpleKey>();
        this.fetchStreamStart();
    }

    @Override
    public boolean checkToken(Token.ID ... choices) {
        while (this.needMoreTokens()) {
            this.fetchMoreTokens();
        }
        if (!this.tokens.isEmpty()) {
            if (choices.length == 0) {
                return true;
            }
            Token firstToken = this.tokens.get(0);
            Token.ID first = firstToken.getTokenId();
            for (Token.ID choice : choices) {
                if (first != choice) continue;
                return true;
            }
        }
        return false;
    }

    @Override
    public Token peekToken() {
        while (this.needMoreTokens()) {
            this.fetchMoreTokens();
        }
        return this.tokens.get(0);
    }

    @Override
    public boolean hasNext() {
        return this.checkToken(new Token.ID[0]);
    }

    @Override
    public Token next() {
        ++this.tokensTaken;
        if (this.tokens.isEmpty()) {
            throw new NoSuchElementException("No more tokens");
        }
        return this.tokens.remove(0);
    }

    private void addToken(Token token) {
        this.lastToken = token;
        this.tokens.add(token);
    }

    private void addToken(int index, Token token) {
        if (index == this.tokens.size()) {
            this.lastToken = token;
        }
        this.tokens.add(index, token);
    }

    private void addAllTokens(List<Token> tokens) {
        this.lastToken = tokens.get(tokens.size() - 1);
        this.tokens.addAll(tokens);
    }

    private boolean isBlockContext() {
        return this.flowLevel == 0;
    }

    private boolean isFlowContext() {
        return !this.isBlockContext();
    }

    private boolean needMoreTokens() {
        if (this.done) {
            return false;
        }
        if (this.tokens.isEmpty()) {
            return true;
        }
        this.stalePossibleSimpleKeys();
        return this.nextPossibleSimpleKey() == this.tokensTaken;
    }

    private void fetchMoreTokens() {
        this.scanToNextToken();
        this.stalePossibleSimpleKeys();
        this.unwindIndent(this.reader.getColumn());
        int c = this.reader.peek();
        switch (c) {
            case 0: {
                this.fetchStreamEnd();
                return;
            }
            case 37: {
                throw new UnsupportedOperationException("Directives are not supported" + this.reader.getMark());
            }
            case 45: {
                if (!this.checkBlockEntry()) break;
                this.fetchBlockEntry();
                return;
            }
            case 46: {
                throw new UnsupportedOperationException("Explicit document ends are not supported" + this.reader.getMark());
            }
            case 91: {
                this.fetchFlowSequenceStart();
                return;
            }
            case 123: {
                this.fetchFlowMappingStart();
                return;
            }
            case 93: {
                this.fetchFlowSequenceEnd();
                return;
            }
            case 125: {
                this.fetchFlowMappingEnd();
                return;
            }
            case 44: {
                this.fetchFlowEntry();
                return;
            }
            case 63: {
                throw new UnsupportedOperationException("Key indicators are not supported" + this.reader.getMark());
            }
            case 58: {
                if (!this.checkValue()) break;
                this.fetchValue();
                return;
            }
            case 42: {
                this.fetchAlias();
                return;
            }
            case 38: {
                this.fetchAnchor();
                return;
            }
            case 33: {
                throw new UnsupportedOperationException("Tags are not supported" + this.reader.getMark());
            }
            case 124: {
                if (!this.isBlockContext()) break;
                this.fetchBlockScalar(ScalarStyle.LITERAL);
                return;
            }
            case 62: {
                if (!this.isBlockContext()) break;
                this.fetchBlockScalar(ScalarStyle.FOLDED);
                return;
            }
            case 39: {
                this.fetchFlowScalar(ScalarStyle.SINGLE_QUOTED);
                return;
            }
            case 34: {
                this.fetchFlowScalar(ScalarStyle.DOUBLE_QUOTED);
                return;
            }
        }
        if (this.checkPlain()) {
            this.fetchPlain();
            return;
        }
        String chRepresentation = CharConstants.escapeChar(String.valueOf(Character.toChars(c)));
        if (c == 9) {
            chRepresentation = chRepresentation + "(TAB)";
        }
        String text = String.format("found character '%s' that cannot start any token. (Do not use %s for indentation)", chRepresentation, chRepresentation);
        throw new ScannerException("while scanning for the next token", null, text, this.reader.getMark());
    }

    private int nextPossibleSimpleKey() {
        if (!this.possibleSimpleKeys.isEmpty()) {
            return this.possibleSimpleKeys.values().iterator().next().getTokenNumber();
        }
        return -1;
    }

    private void stalePossibleSimpleKeys() {
        if (!this.possibleSimpleKeys.isEmpty()) {
            Iterator<SimpleKey> iterator = this.possibleSimpleKeys.values().iterator();
            while (iterator.hasNext()) {
                SimpleKey key = iterator.next();
                if (key.getLine() == this.reader.getLine() && this.reader.getIndex() - key.getIndex() <= 1024) continue;
                if (key.isRequired()) {
                    throw new ScannerException("while scanning a simple key", key.getMark(), "could not find expected ':'", this.reader.getMark());
                }
                iterator.remove();
            }
        }
    }

    private void savePossibleSimpleKey() {
        boolean required = this.isBlockContext() && this.indent == this.reader.getColumn();
        if (!this.allowSimpleKey && required) {
            throw new YamlEngineException("A simple key is required only if it is the first token in the current line");
        }
        if (this.allowSimpleKey) {
            this.removePossibleSimpleKey();
            int tokenNumber = this.tokensTaken + this.tokens.size();
            SimpleKey key = new SimpleKey(tokenNumber, required, this.reader.getIndex(), this.reader.getLine(), this.reader.getColumn(), this.reader.getMark());
            this.possibleSimpleKeys.put(this.flowLevel, key);
        }
    }

    private void removePossibleSimpleKey() {
        SimpleKey key = this.possibleSimpleKeys.remove(this.flowLevel);
        if (key != null && key.isRequired()) {
            throw new ScannerException("while scanning a simple key", key.getMark(), "could not find expected ':'", this.reader.getMark());
        }
    }

    private void unwindIndent(int col) {
        if (this.isFlowContext()) {
            return;
        }
        while (this.indent > col) {
            Mark mark = this.reader.getMark();
            this.indent = this.indents.pop();
            this.addToken(new BlockEndToken(mark, mark));
        }
    }

    private boolean addIndent(int column) {
        if (this.indent < column) {
            this.indents.push(this.indent);
            this.indent = column;
            return true;
        }
        return false;
    }

    private void fetchStreamStart() {
        Mark mark = this.reader.getMark();
        DocumentStartToken token = new DocumentStartToken(mark, mark);
        this.addToken(token);
    }

    private void fetchStreamEnd() {
        this.unwindIndent(-1);
        this.removePossibleSimpleKey();
        this.allowSimpleKey = false;
        this.possibleSimpleKeys.clear();
        Mark mark = this.reader.getMark();
        DocumentEndToken token = new DocumentEndToken(mark, mark);
        this.addToken(token);
        this.done = true;
    }

    private void fetchFlowSequenceStart() {
        this.fetchFlowCollectionStart(false);
    }

    private void fetchFlowMappingStart() {
        this.fetchFlowCollectionStart(true);
    }

    private void fetchFlowCollectionStart(boolean isMappingStart) {
        this.savePossibleSimpleKey();
        ++this.flowLevel;
        this.allowSimpleKey = true;
        Mark startMark = this.reader.getMark();
        this.reader.forward(1);
        Mark endMark = this.reader.getMark();
        Token token = isMappingStart ? new FlowMappingStartToken(startMark, endMark) : new FlowSequenceStartToken(startMark, endMark);
        this.addToken(token);
    }

    private void fetchFlowSequenceEnd() {
        this.fetchFlowCollectionEnd(false);
    }

    private void fetchFlowMappingEnd() {
        this.fetchFlowCollectionEnd(true);
    }

    private void fetchFlowCollectionEnd(boolean isMappingEnd) {
        this.removePossibleSimpleKey();
        --this.flowLevel;
        this.allowSimpleKey = false;
        Mark startMark = this.reader.getMark();
        this.reader.forward();
        Mark endMark = this.reader.getMark();
        Token token = isMappingEnd ? new FlowMappingEndToken(startMark, endMark) : new FlowSequenceEndToken(startMark, endMark);
        this.addToken(token);
    }

    private void fetchFlowEntry() {
        this.allowSimpleKey = true;
        this.removePossibleSimpleKey();
        Mark startMark = this.reader.getMark();
        this.reader.forward();
        Mark endMark = this.reader.getMark();
        FlowEntryToken token = new FlowEntryToken(startMark, endMark);
        this.addToken(token);
    }

    private void fetchBlockEntry() {
        if (this.isBlockContext()) {
            if (!this.allowSimpleKey) {
                throw new ScannerException("", null, "sequence entries are not allowed here", this.reader.getMark());
            }
            if (this.addIndent(this.reader.getColumn())) {
                Mark mark = this.reader.getMark();
                this.addToken(new BlockSequenceStartToken(mark, mark));
            }
        }
        this.allowSimpleKey = true;
        this.removePossibleSimpleKey();
        Mark startMark = this.reader.getMark();
        this.reader.forward();
        Mark endMark = this.reader.getMark();
        BlockEntryToken token = new BlockEntryToken(startMark, endMark);
        this.addToken(token);
    }

    private void fetchValue() {
        SimpleKey key = this.possibleSimpleKeys.remove(this.flowLevel);
        if (key != null) {
            this.addToken(key.getTokenNumber() - this.tokensTaken, new KeyToken(key.getMark(), key.getMark()));
            if (this.isBlockContext() && this.addIndent(key.getColumn())) {
                this.addToken(key.getTokenNumber() - this.tokensTaken, new BlockMappingStartToken(key.getMark(), key.getMark()));
            }
            this.allowSimpleKey = false;
        } else {
            if (this.isBlockContext() && !this.allowSimpleKey) {
                throw new ScannerException("mapping values are not allowed here", this.reader.getMark());
            }
            if (this.isBlockContext() && this.addIndent(this.reader.getColumn())) {
                Mark mark = this.reader.getMark();
                this.addToken(new BlockMappingStartToken(mark, mark));
            }
            this.allowSimpleKey = this.isBlockContext();
            this.removePossibleSimpleKey();
        }
        Mark startMark = this.reader.getMark();
        this.reader.forward();
        Mark endMark = this.reader.getMark();
        ValueToken token = new ValueToken(startMark, endMark);
        this.addToken(token);
    }

    private void fetchAlias() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        Token tok = this.scanAnchor(false);
        this.addToken(tok);
    }

    private void fetchAnchor() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        Token tok = this.scanAnchor(true);
        this.addToken(tok);
    }

    private void fetchBlockScalar(ScalarStyle style) {
        this.allowSimpleKey = true;
        this.removePossibleSimpleKey();
        this.addAllTokens(this.scanBlockScalar(style));
    }

    private void fetchFlowScalar(ScalarStyle style) {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        Token tok = this.scanFlowScalar(style);
        this.addToken(tok);
    }

    private void fetchPlain() {
        this.savePossibleSimpleKey();
        this.allowSimpleKey = false;
        Token tok = this.scanPlain();
        this.addToken(tok);
    }

    private boolean checkBlockEntry() {
        return CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(1));
    }

    private boolean checkValue() {
        if (this.isFlowContext()) {
            return true;
        }
        return CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(1));
    }

    private boolean checkPlain() {
        int c = this.reader.peek();
        boolean notForbidden = CharConstants.NULL_BL_T_LINEBR.hasNo(c, "-?:,[]{}#&*!|>'\"%@`");
        if (notForbidden) {
            return true;
        }
        if (this.isBlockContext()) {
            return CharConstants.NULL_BL_T_LINEBR.hasNo(this.reader.peek(1)) && "-?:".indexOf(c) != -1;
        }
        return CharConstants.NULL_BL_T_LINEBR.hasNo(this.reader.peek(1), ",]") && "-?".indexOf(c) != -1;
    }

    private void scanToNextToken() {
        if (this.reader.getIndex() == 0 && this.reader.peek() == 65279) {
            this.reader.forward();
        }
        boolean found = false;
        int inlineStartColumn = -1;
        while (!found) {
            Optional<String> breaksOpt;
            Mark startMark = this.reader.getMark();
            int columnBeforeComment = this.reader.getColumn();
            boolean commentSeen = false;
            int ff = 0;
            while (this.reader.peek(ff) == 32) {
                ++ff;
            }
            if (ff > 0) {
                this.reader.forward(ff);
            }
            if (this.reader.peek() == 35) {
                CommentType type;
                commentSeen = true;
                if (columnBeforeComment != 0 && (this.lastToken == null || this.lastToken.getTokenId() != Token.ID.BlockEntry)) {
                    type = CommentType.IN_LINE;
                    inlineStartColumn = this.reader.getColumn();
                } else if (inlineStartColumn == this.reader.getColumn()) {
                    type = CommentType.IN_LINE;
                } else {
                    inlineStartColumn = -1;
                    type = CommentType.BLOCK;
                }
                this.addToken(this.scanComment(type));
            }
            if ((breaksOpt = this.scanLineBreak()).isPresent()) {
                if (!commentSeen && columnBeforeComment == 0) {
                    this.addToken(new CommentToken(CommentType.BLANK_LINE, breaksOpt.get(), startMark, this.reader.getMark()));
                }
                if (!this.isBlockContext()) continue;
                this.allowSimpleKey = true;
                continue;
            }
            found = true;
        }
    }

    private CommentToken scanComment(CommentType type) {
        Mark startMark = this.reader.getMark();
        this.reader.forward();
        int length = 0;
        while (CharConstants.NULL_OR_LINEBR.hasNo(this.reader.peek(length))) {
            ++length;
        }
        String value = this.reader.prefixForward(length);
        Mark endMark = this.reader.getMark();
        return new CommentToken(type, value, startMark, endMark);
    }

    private Token scanAnchor(boolean isAnchor) {
        Mark startMark = this.reader.getMark();
        int indicator = this.reader.peek();
        String name = indicator == 42 ? "alias" : "anchor";
        this.reader.forward();
        int length = 0;
        int c = this.reader.peek(length);
        while (CharConstants.NULL_BL_T_LINEBR.hasNo(c, ",[]{}/.*&")) {
            c = this.reader.peek(++length);
        }
        if (length == 0) {
            String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning an " + name, startMark, "unexpected character found " + s + '(' + c + ')', this.reader.getMark());
        }
        String value = this.reader.prefixForward(length);
        c = this.reader.peek();
        if (CharConstants.NULL_BL_T_LINEBR.hasNo(c, "?:,]}%@`")) {
            String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning an " + name, startMark, "unexpected character found " + s + '(' + c + ')', this.reader.getMark());
        }
        Mark endMark = this.reader.getMark();
        Token tok = isAnchor ? new AnchorToken(new Anchor(value, startMark), startMark, endMark) : new AliasToken(new Anchor(value, startMark), startMark, endMark);
        return tok;
    }

    private void scanBlockScalarIndicators(Mark startMark) {
        String s;
        Boolean chomping = null;
        int increment = -1;
        int c = this.reader.peek();
        if (c == 45 || c == 43) {
            chomping = c == 43 ? Boolean.TRUE : Boolean.FALSE;
            this.reader.forward();
            c = this.reader.peek();
            if (Character.isDigit(c)) {
                s = String.valueOf(Character.toChars(c));
                increment = Integer.parseInt(s);
                if (increment == 0) {
                    throw new ScannerException(SCANNING_SCALAR, startMark, "expected indentation indicator in the range 1-9, but found 0", this.reader.getMark());
                }
                this.reader.forward();
            }
        } else if (Character.isDigit(c)) {
            s = String.valueOf(Character.toChars(c));
            increment = Integer.parseInt(s);
            if (increment == 0) {
                throw new ScannerException(SCANNING_SCALAR, startMark, "expected indentation indicator in the range 1-9, but found 0", this.reader.getMark());
            }
            this.reader.forward();
            c = this.reader.peek();
            if (c == 45 || c == 43) {
                chomping = c == 43 ? Boolean.TRUE : Boolean.FALSE;
                this.reader.forward();
            }
        }
        if (CharConstants.NULL_BL_LINEBR.hasNo(c = this.reader.peek())) {
            s = String.valueOf(Character.toChars(c));
            throw new ScannerException(SCANNING_SCALAR, startMark, "expected chomping or indentation indicators, but found " + s + '(' + c + ')', this.reader.getMark());
        }
    }

    private List<Token> scanBlockScalar(ScalarStyle style) {
        StringBuilder chunks = new StringBuilder();
        Mark startMark = this.reader.getMark();
        this.reader.forward();
        this.scanBlockScalarIndicators(startMark);
        CommentToken commentToken = this.scanBlockScalarIgnoredLine(startMark);
        int minIndent = this.indent + 1;
        if (minIndent < 1) {
            minIndent = 1;
        }
        int maxIndent = 0;
        Mark endMark = this.reader.getMark();
        StringBuilder breaksChunks = new StringBuilder();
        while (CharConstants.LINEBR.has(this.reader.peek(), " \r")) {
            if (this.reader.peek() != 32) {
                breaksChunks.append(this.scanLineBreak().orElse(""));
                endMark = this.reader.getMark();
                continue;
            }
            this.reader.forward();
            if (this.reader.getColumn() <= maxIndent) continue;
            maxIndent = this.reader.getColumn();
        }
        String breaks = breaksChunks.toString();
        int blockIndent = Math.max(minIndent, maxIndent);
        if (this.reader.getColumn() < blockIndent && this.indent != this.reader.getColumn()) {
            throw new ScannerException(SCANNING_SCALAR, startMark, " the leading empty lines contain more spaces (" + blockIndent + ") than the first non-empty line.", this.reader.getMark());
        }
        while (this.reader.getColumn() == blockIndent && this.reader.peek() != 0) {
            chunks.append(breaks);
            boolean leadingNonSpace = " \t".indexOf(this.reader.peek()) == -1;
            int length = 0;
            while (CharConstants.NULL_OR_LINEBR.hasNo(this.reader.peek(length))) {
                ++length;
            }
            chunks.append(this.reader.prefixForward(length));
            Optional<String> lineBreakOpt = this.scanLineBreak();
            Object[] brme = this.scanBlockScalarBreaks(blockIndent);
            breaks = (String)brme[0];
            endMark = (Mark)brme[1];
            if (this.reader.getColumn() != blockIndent || this.reader.peek() == 0) break;
            if (leadingNonSpace && style == ScalarStyle.FOLDED && "\n".equals(lineBreakOpt.orElse("")) && " \t".indexOf(this.reader.peek()) == -1) {
                if (!breaks.isEmpty()) continue;
                chunks.append(' ');
                continue;
            }
            chunks.append(lineBreakOpt.orElse(""));
        }
        ScalarToken scalarToken = new ScalarToken(chunks.toString(), style, startMark, endMark);
        return ScannerImpl.makeTokenList(commentToken, scalarToken);
    }

    private CommentToken scanBlockScalarIgnoredLine(Mark startMark) {
        while (this.reader.peek() == 32) {
            this.reader.forward();
        }
        CommentToken commentToken = null;
        if (this.reader.peek() == 35) {
            commentToken = this.scanComment(CommentType.IN_LINE);
        }
        int c = this.reader.peek();
        if (!this.scanLineBreak().isPresent() && c != 0) {
            String s = String.valueOf(Character.toChars(c));
            throw new ScannerException(SCANNING_SCALAR, startMark, "expected a comment or a line break, but found " + s + '(' + c + ')', this.reader.getMark());
        }
        return commentToken;
    }

    private Object[] scanBlockScalarBreaks(int indent) {
        Optional<String> lineBreakOpt;
        int col;
        StringBuilder chunks = new StringBuilder();
        Mark endMark = this.reader.getMark();
        for (col = this.reader.getColumn(); col < indent && this.reader.peek() == 32; ++col) {
            this.reader.forward();
        }
        while ((lineBreakOpt = this.scanLineBreak()).isPresent()) {
            chunks.append(lineBreakOpt.get());
            endMark = this.reader.getMark();
            for (col = this.reader.getColumn(); col < indent && this.reader.peek() == 32; ++col) {
                this.reader.forward();
            }
        }
        return new Object[]{chunks.toString(), endMark};
    }

    private Token scanFlowScalar(ScalarStyle style) {
        boolean doubleValue = style == ScalarStyle.DOUBLE_QUOTED;
        StringBuilder chunks = new StringBuilder();
        Mark startMark = this.reader.getMark();
        int quote = this.reader.peek();
        this.reader.forward();
        chunks.append(this.scanFlowScalarNonSpaces(doubleValue, startMark));
        while (this.reader.peek() != quote) {
            chunks.append(this.scanFlowScalarSpaces(startMark));
            chunks.append(this.scanFlowScalarNonSpaces(doubleValue, startMark));
        }
        this.reader.forward();
        Mark endMark = this.reader.getMark();
        return new ScalarToken(chunks.toString(), style, startMark, endMark);
    }

    private String scanFlowScalarNonSpaces(boolean doubleQuoted, Mark startMark) {
        StringBuilder chunks;
        block8: {
            int c;
            chunks = new StringBuilder();
            while (true) {
                int length = 0;
                while (CharConstants.NULL_BL_T_LINEBR.hasNo(this.reader.peek(length), "'\"\\")) {
                    ++length;
                }
                if (length != 0) {
                    chunks.append(this.reader.prefixForward(length));
                }
                c = this.reader.peek();
                if (!doubleQuoted && c == 39 && this.reader.peek(1) == 39) {
                    chunks.append('\'');
                    this.reader.forward(2);
                    continue;
                }
                if (doubleQuoted ? c == 39 : "\"\\".indexOf(c) != -1) {
                    chunks.appendCodePoint(c);
                    this.reader.forward();
                    continue;
                }
                if (!doubleQuoted || c != 92) break block8;
                this.reader.forward();
                c = this.reader.peek();
                if (!Character.isSupplementaryCodePoint(c) && CharConstants.ESCAPE_REPLACEMENTS.containsKey(Character.valueOf((char)c))) {
                    chunks.append(CharConstants.ESCAPE_REPLACEMENTS.get(Character.valueOf((char)c)));
                    this.reader.forward();
                    continue;
                }
                if (!Character.isSupplementaryCodePoint(c) && CharConstants.ESCAPE_CODES.containsKey(Character.valueOf((char)c))) {
                    length = CharConstants.ESCAPE_CODES.get(Character.valueOf((char)c));
                    this.reader.forward();
                    String hex = this.reader.prefix(length);
                    if (NOT_HEXA.matcher(hex).find()) {
                        throw new ScannerException("while scanning a double-quoted scalar", startMark, "expected escape sequence of " + length + " hexadecimal numbers, but found: " + hex, this.reader.getMark());
                    }
                    int decimal = Integer.parseInt(hex, 16);
                    String unicode = new String(Character.toChars(decimal));
                    chunks.append(unicode);
                    this.reader.forward(length);
                    continue;
                }
                if (!this.scanLineBreak().isPresent()) break;
                chunks.append(this.scanFlowScalarBreaks(startMark));
            }
            String s = String.valueOf(Character.toChars(c));
            throw new ScannerException("while scanning a double-quoted scalar", startMark, "found unknown escape character " + s + '(' + c + ')', this.reader.getMark());
        }
        return chunks.toString();
    }

    private String scanFlowScalarSpaces(Mark startMark) {
        StringBuilder chunks = new StringBuilder();
        int length = 0;
        while (" \t".indexOf(this.reader.peek(length)) != -1) {
            ++length;
        }
        String whitespaces = this.reader.prefixForward(length);
        int c = this.reader.peek();
        if (c == 0) {
            throw new ScannerException("while scanning a quoted scalar", startMark, "found unexpected end of stream", this.reader.getMark());
        }
        Optional<String> lineBreakOpt = this.scanLineBreak();
        if (lineBreakOpt.isPresent()) {
            String breaks = this.scanFlowScalarBreaks(startMark);
            if (!"\n".equals(lineBreakOpt.get())) {
                chunks.append(lineBreakOpt.get());
            } else if (breaks.isEmpty()) {
                chunks.append(' ');
            }
            chunks.append(breaks);
        } else {
            chunks.append(whitespaces);
        }
        return chunks.toString();
    }

    private String scanFlowScalarBreaks(Mark startMark) {
        StringBuilder chunks = new StringBuilder();
        while (true) {
            if (" \t".indexOf(this.reader.peek()) != -1) {
                this.reader.forward();
                continue;
            }
            Optional<String> lineBreakOpt = this.scanLineBreak();
            if (!lineBreakOpt.isPresent()) break;
            chunks.append(lineBreakOpt.get());
        }
        return chunks.toString();
    }

    private Token scanPlain() {
        Mark startMark;
        StringBuilder chunks = new StringBuilder();
        Mark endMark = startMark = this.reader.getMark();
        int plainIndent = this.indent + 1;
        String spaces = "";
        do {
            int c;
            int length = 0;
            if (this.reader.peek() == 35) break;
            while (!(CharConstants.NULL_BL_T_LINEBR.has(c = this.reader.peek(length)) || c == 58 && CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(length + 1), this.isFlowContext() ? ",[]{}" : "") || this.isFlowContext() && ",[]{}".indexOf(c) != -1)) {
                ++length;
            }
            if (length == 0) break;
            this.allowSimpleKey = false;
            chunks.append(spaces);
            chunks.append(this.reader.prefixForward(length));
            endMark = this.reader.getMark();
        } while (!(spaces = this.scanPlainSpaces()).isEmpty() && this.reader.peek() != 35 && (!this.isBlockContext() || this.reader.getColumn() >= plainIndent));
        return new ScalarToken(chunks.toString(), ScalarStyle.PLAIN, startMark, endMark);
    }

    private boolean atEndOfPlain() {
        int c;
        int wsLength = 0;
        int wsColumn = this.reader.getColumn();
        while ((c = this.reader.peek(wsLength)) != 0 && CharConstants.NULL_BL_T_LINEBR.has(c)) {
            if (!(CharConstants.LINEBR.has(c) || c == 13 && this.reader.peek(++wsLength + 1) == 10 || c == 65279)) {
                ++wsColumn;
                continue;
            }
            wsColumn = 0;
        }
        if (this.reader.peek(wsLength) == 35 || this.reader.peek(wsLength + 1) == 0 || this.isBlockContext() && wsColumn < this.indent) {
            return true;
        }
        if (this.isBlockContext()) {
            int extra = 1;
            while ((c = this.reader.peek(wsLength + extra)) != 0 && !CharConstants.NULL_BL_T_LINEBR.has(c)) {
                if (c == 58 && CharConstants.NULL_BL_T_LINEBR.has(this.reader.peek(wsLength + extra + 1))) {
                    return true;
                }
                ++extra;
            }
        }
        return false;
    }

    private String scanPlainSpaces() {
        int length = 0;
        while (this.reader.peek(length) == 32 || this.reader.peek(length) == 9) {
            ++length;
        }
        String whitespaces = this.reader.prefixForward(length);
        Optional<String> lineBreakOpt = this.scanLineBreak();
        if (lineBreakOpt.isPresent()) {
            this.allowSimpleKey = true;
            if (this.atEndOfPlain()) {
                return "";
            }
            StringBuilder breaks = new StringBuilder();
            while (true) {
                if (this.reader.peek() == 32) {
                    this.reader.forward();
                    continue;
                }
                Optional<String> lbOpt = this.scanLineBreak();
                if (!lbOpt.isPresent()) break;
                breaks.append(lbOpt.get());
            }
            if (!"\n".equals(lineBreakOpt.orElse(""))) {
                return lineBreakOpt.orElse("") + breaks;
            }
            if (breaks.length() == 0) {
                return " ";
            }
            return breaks.toString();
        }
        return whitespaces;
    }

    private Optional<String> scanLineBreak() {
        int c = this.reader.peek();
        if (c == 13 || c == 10 || c == 133) {
            if (c == 13 && 10 == this.reader.peek(1)) {
                this.reader.forward(2);
            } else {
                this.reader.forward();
            }
            return Optional.of("\n");
        }
        if (c == 8232 || c == 8233) {
            this.reader.forward();
            return Optional.of(String.valueOf(Character.toChars(c)));
        }
        return Optional.empty();
    }

    private static List<Token> makeTokenList(Token ... tokens) {
        ArrayList<Token> tokenList = new ArrayList<Token>();
        for (Token token : tokens) {
            if (token == null) continue;
            tokenList.add(token);
        }
        return tokenList;
    }
}

