package fr.krishenk.castel.locale.compiler;

import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.MessageHandler;
import fr.krishenk.castel.locale.SupportedLanguage;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderBuilder;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderType;
import fr.krishenk.castel.locale.compiler.placeholders.StandardCastelPlaceholder;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.string.StringUtils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

public class MessageCompiler {
    public static final MessageCompilerSettings DEFAULT_COMPILER_SETTINGS = MessageCompiler.defaultSettingsWithErroHandler(null);
    private static final boolean CONTENT = XMaterial.supports(16);
    private final List<MessagePiece> pieces = new ArrayList<MessagePiece>(10);
    private static final String NOPREFIX = "NOPREFIX|";
    private static final String PREFIX = "PREFIX|";
    private final StringBuilder plain;
    private final char[] str;
    private final int len;
    private int i;
    private char ch;
    private final Boolean usePrefix;
    private final MessageCompilerSettings settings;
    private final List<MessageCompilerException> exceptions = new ArrayList<MessageCompilerException>();
    private boolean used = false;
    private static final String HOVER = "hover:{";
    private static final char SEPARATOR = ';';
    private static final char COMMAND = '/';
    private static final char COMMAND_SUGGESTION = '|';
    private static final char URL = '@';
    private static final char CLOSING_HOVER_CHAR = '}';
    int lastBackRefSepIndex = -1;

    public char[] getChars() {
        return this.str;
    }

    public static MessageCompilerSettings defaultSettingsWithErroHandler(Consumer<MessageCompiler> errorHandler) {
        return new MessageCompilerSettings(true, false, true, true, true, null).withErrorHandler(errorHandler);
    }

    public List<MessageCompilerException> getExceptions() {
        return this.exceptions;
    }

    public boolean hasErrors() {
        return !this.exceptions.isEmpty();
    }

    public String joinExceptions() {
        StringBuilder builder = new StringBuilder(this.exceptions.size() * this.len * 2);
        int i = this.exceptions.size();
        for (MessageCompilerException ex : this.exceptions) {
            builder.append(ex.getMessage());
            if (--i == 0) continue;
            builder.append('\n');
        }
        return builder.toString();
    }

    public int getIndex() {
        return this.i;
    }

    public char getChar() {
        return this.ch;
    }

    public MessageCompiler(String str) {
        this(str, DEFAULT_COMPILER_SETTINGS);
    }

    public MessageCompiler(String str, MessageCompilerSettings settings) {
        this(str.toCharArray(), settings);
    }

    public MessageCompiler(char[] str, MessageCompilerSettings settings) {
        this.settings = Objects.requireNonNull(settings);
        this.str = str;
        this.len = str.length;
        this.plain = new StringBuilder(this.len);
        this.usePrefix = this.shouldUsePrefix();
    }

    private boolean isLast() {
        if (this.i == this.len - 1) {
            this.plain.append(this.ch);
            return true;
        }
        return false;
    }

    public List<MessagePiece> getPieces() {
        return this.pieces;
    }

    private void validateColors() {
        MessagePiece.SimpleColor simple;
        if (!this.settings.validate) {
            return;
        }
        if (this.pieces.size() < 2) {
            return;
        }
        MessagePiece first = this.pieces.get(this.pieces.size() - 1);
        MessagePiece second = this.pieces.get(this.pieces.size() - 2);
        if (!(first instanceof MessagePiece.Color) || !(second instanceof MessagePiece.Color)) {
            return;
        }
        if (first.equals(second)) {
            this.exception(this.i - 2, "Repeated color code", "||||");
            return;
        }
        boolean isFirstColor = first instanceof MessagePiece.HexColor;
        boolean isSecondColor = second instanceof MessagePiece.HexColor;
        if (!isFirstColor) {
            simple = (MessagePiece.SimpleColor)first;
            isFirstColor = simple.getColor().isColor();
        }
        if (!isSecondColor) {
            simple = (MessagePiece.SimpleColor)second;
            isSecondColor = simple.getColor().isColor();
        }
        if (isFirstColor && isSecondColor) {
            this.exception(this.i - 2, "Two non-formatting colors cannot follow each other as it's overridden by the later.", "||||");
            return;
        }
        if (!isSecondColor && isFirstColor) {
            this.exception(this.i - 2, "A formatting color cannot follow a non-formatting color as it's overridden by the later. Consider putting the formatting color after the non-formatting one.", "||||");
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder(30);
        for (MessagePiece piece : this.pieces) {
            builder.append(" | ");
            builder.append(piece.toString());
        }
        return builder.toString();
    }

    void savePlainStateAndReset() {
        if (this.plain.length() == 0) {
            return;
        }
        this.pieces.add(new MessagePiece.Plain(this.plain.toString()));
        this.plain.setLength(0);
    }

    public static List<String> link(Collection<String> lines, MessageBuilder settings) {
        ArrayList<String> compiled = new ArrayList<String>(lines.size());
        MessageObject lastColor = null;
        for (String line : lines) {
            MessageObject loreLine = MessageCompiler.compile(line);
            MessageObject newLastColors = loreLine.findLastColors();
            if (lastColor != null) {
                loreLine = lastColor.merge(loreLine);
            }
            if (newLastColors != null) {
                lastColor = newLastColors;
            }
            compiled.add(loreLine.buildPlain(settings));
        }
        return compiled;
    }

    public static MessageObject compile(String str) {
        return MessageCompiler.compile(str, true, false);
    }

    public static MessageObject compile(String str, boolean validate, boolean plainOnly) {
        return MessageCompiler.compile(str, validate, plainOnly, null);
    }

    public static MessageObject compile(String str, boolean validate, boolean plainOnly, MessageTokenHandler tokenHandler) {
        MessageTokenHandler[] arrmessageTokenHandler;
        if (tokenHandler == null) {
            arrmessageTokenHandler = null;
        } else {
            MessageTokenHandler[] arrmessageTokenHandler2 = new MessageTokenHandler[1];
            arrmessageTokenHandler = arrmessageTokenHandler2;
            arrmessageTokenHandler2[0] = tokenHandler;
        }
        MessageTokenHandler[] handlers = arrmessageTokenHandler;
        return MessageCompiler.compile(str, new MessageCompilerSettings(validate, plainOnly, true, true, true, handlers));
    }

    public static MessageObject compile(String str, MessageCompilerSettings compilerSettings) {
        Objects.requireNonNull(str, "Cannot compile a null message");
        Objects.requireNonNull(compilerSettings, "Cannot compile with null compiler settings");
        MessageCompiler compiler = new MessageCompiler(str.toCharArray(), compilerSettings);
        MessageObject obj = compiler.compileObject();
        if (compiler.hasErrors()) {
            if (compilerSettings.errorHandler != null) {
                compilerSettings.errorHandler.accept(compiler);
            } else {
                throw new MessageCompilerException("{UNCAUGHT}", "[UNCAUGHT]", 0, compiler.joinExceptions());
            }
        }
        return obj;
    }

    public void compile() {
        if (this.used) {
            throw new IllegalStateException("This compiler has already compiled");
        }
        this.used = true;
        block8: while (this.i < this.len) {
            block30: {
                this.ch = this.str[this.i];
                if (this.settings.tokenHandlers != null) {
                    for (MessageTokenHandler tokenHandler : this.settings.tokenHandlers) {
                        MessageTokenResult end = tokenHandler.consumeUntil(this);
                        if (end == null) continue;
                        if (end.index <= this.i) {
                            throw new IllegalStateException("Less or no characters consumed for token: " + this.i + " -> " + end.index);
                        }
                        this.savePlainStateAndReset();
                        this.pieces.add(end.piece);
                        this.i = end.index;
                        break block30;
                    }
                }
                block1 : switch (this.ch) {
                    case '\n': {
                        if (!this.settings.allowNewLines) {
                            this.plain.append('\n');
                            break;
                        }
                        this.savePlainStateAndReset();
                        this.pieces.add(new MessagePiece.NewLine());
                        break;
                    }
                    case '%': {
                        if (!this.settings.translatePlaceholders) {
                            this.plain.append('%');
                            break;
                        }
                        if (this.isLast()) break block8;
                        PlaceholderBuilder placeholderBuilder = new PlaceholderBuilder(this.i + 1, this.str);
                        boolean result = placeholderBuilder.evaluate();
                        if (result) {
                            this.savePlainStateAndReset();
                            try {
                                this.pieces.add(new MessagePiece.Variable(placeholderBuilder.build()));
                            }
                            catch (Exception ex) {
                                int end = placeholderBuilder.getStopIndex();
                                if (this.settings.validate) {
                                    this.exception(this.i, ex.getMessage(), new String(this.str, this.i, end - this.i));
                                }
                                this.plain.append(this.str, this.i, end - this.i + 1);
                            }
                            this.i = placeholderBuilder.getStopIndex();
                            break;
                        }
                        this.plain.append('%');
                        break;
                    }
                    case '&':
                    case '§': {
                        if (!this.settings.colorize) {
                            this.plain.append('&');
                            break;
                        }
                        if (this.isLast()) break block8;
                        char next = this.str[this.i + 1];
                        if (MessageHandler.isColorCode(next)) {
                            this.savePlainStateAndReset();
                            ChatColor bukkitColor = Objects.requireNonNull(ChatColor.getByChar((char)(next | 0x20)));
                            this.pieces.add(new MessagePiece.SimpleColor(bukkitColor));
                            this.validateColors();
                            ++this.i;
                            break;
                        }
                        if (this.ch == '&' && next == '#') {
                            if (this.i + 1 < this.len - 6) {
                                StringBuilder hexBuilder = new StringBuilder(6);
                                ++this.i;
                                while (hexBuilder.length() != 6) {
                                    if (MessageHandler.isColorCode(next = this.str[++this.i])) {
                                        hexBuilder.append(next);
                                        continue;
                                    }
                                    this.exception(this.i, "Invalid hex color character '" + next + "' or possibly unfinished hex color");
                                    break block1;
                                }
                                this.savePlainStateAndReset();
                                Color color = new Color(Integer.parseInt(hexBuilder.toString(), 16));
                                this.pieces.add(new MessagePiece.HexColor(color));
                                this.validateColors();
                                break;
                            }
                            this.exception(this.i, "Unfinished hex color", new String(this.str, this.i, this.len - this.i));
                            break block8;
                        }
                        if (this.ch == '§' && next == 'x') {
                            if (this.i + 1 >= this.len - 12) break block8;
                            StringBuilder hexBuilder = new StringBuilder(6);
                            ++this.i;
                            boolean skipCode = true;
                            while (hexBuilder.length() != 6) {
                                next = this.str[++this.i];
                                if (skipCode) {
                                    skipCode = false;
                                    if (next == '\u00a7') continue;
                                    break block1;
                                }
                                skipCode = true;
                                if (!MessageHandler.isColorCode(next)) break block1;
                                hexBuilder.append(next);
                            }
                            this.savePlainStateAndReset();
                            Color color = new Color(Integer.parseInt(hexBuilder.toString(), 16));
                            this.pieces.add(new MessagePiece.HexColor(color));
                            this.validateColors();
                            break;
                        }
                        this.plain.append('&');
                        break;
                    }
                    case '{': {
                        if (this.isLast()) break block8;
                        MessagePiece piece = this.handleColorSpecifier();
                        if (piece != null) {
                            this.pieces.add(piece);
                            break;
                        }
                        this.plain.append('{');
                        break;
                    }
                    default: {
                        if (!this.settings.plainOnly && this.ch == HOVER.charAt(0)) {
                            if (this.i + HOVER.length() <= this.len) {
                                for (int hoverIndex = 1; hoverIndex < HOVER.length(); ++hoverIndex) {
                                    this.ch = this.str[++this.i];
                                    if (this.ch == HOVER.charAt(hoverIndex)) continue;
                                    this.plain.append(HOVER, 0, hoverIndex);
                                    --this.i;
                                    break block1;
                                }
                                ++this.i;
                                MessagePiece.Hover hoverPiece = this.buildHover();
                                if (hoverPiece == null) break;
                                this.savePlainStateAndReset();
                                this.pieces.add(hoverPiece);
                                break;
                            }
                        }
                        this.plain.append(this.ch);
                    }
                }
            }
            ++this.i;
        }
        this.savePlainStateAndReset();
    }

    public MessagePiece[] compileToArray() {
        this.compile();
        return this.pieces.toArray(new MessagePiece[0]);
    }

    public MessageObject compileObject() {
        return new MessageObject(this.compileToArray(), this.usePrefix, this.settings);
    }

    private Boolean shouldUsePrefix() {
        if (this.startsWith(NOPREFIX)) {
            this.i = NOPREFIX.length();
            return false;
        }
        if (this.startsWith(PREFIX)) {
            this.i = PREFIX.length();
            return true;
        }
        return null;
    }

    public static HoverEvent constructHoverEvent(BaseComponent[] baseComponents) {
        if (CONTENT) {
            Text text = new Text(baseComponents);
            return new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Content[]{text});
        }
        return new HoverEvent(HoverEvent.Action.SHOW_TEXT, baseComponents);
    }

    private boolean startsWith(String str) {
        if (this.len < str.length()) {
            return false;
        }
        for (int i = 0; i < str.length(); ++i) {
            if (this.str[i] == str.charAt(i)) continue;
            return false;
        }
        return true;
    }

    private MessagePiece.Hover buildHover() {
        HoverParserState state = HoverParserState.NORMAL_MESSAGE;
        StringBuilder normalMessage = new StringBuilder();
        StringBuilder hoverMessage = new StringBuilder();
        StringBuilder action = new StringBuilder();
        ClickEvent.Action clickActionType = null;
        int hoverMessageStartIndex = 0;
        int actionStartIndex = 0;
        int enclosingLevel = 0;
        block23: for (int j = this.i; j < this.len; ++j) {
            char ch = this.str[j];
            switch (ch) {
                case '}': {
                    if (enclosingLevel != 0) {
                        --enclosingLevel;
                        switch (state) {
                            case ACTION: {
                                action.append('}');
                                continue block23;
                            }
                            case NORMAL_MESSAGE: {
                                normalMessage.append('}');
                                continue block23;
                            }
                            case HOVER_MESSAGE: {
                                hoverMessage.append('}');
                            }
                        }
                        continue block23;
                    }
                    if (state == HoverParserState.NORMAL_MESSAGE && hoverMessage.length() == 0) {
                        this.exception(j, "Hover message is empty");
                        return null;
                    }
                    if (state == HoverParserState.ACTION && clickActionType == null) {
                        this.exception(j, "Hover message action is empty");
                        return null;
                    }
                    MessageCompiler normalObj = new MessageCompiler(MessageCompiler.builderToChars(normalMessage), this.settings);
                    MessageCompiler hoverObj = new MessageCompiler(MessageCompiler.builderToChars(hoverMessage), this.settings);
                    MessageCompiler actionObj = new MessageCompiler(MessageCompiler.builderToChars(action), this.settings);
                    MessagePiece.Hover piece = new MessagePiece.Hover(clickActionType, normalObj.compileToArray(), hoverObj.compileToArray(), actionObj.compileToArray());
                    this.mergeExceptions(this.i, normalObj);
                    this.mergeExceptions(hoverMessageStartIndex, hoverObj);
                    this.mergeExceptions(actionStartIndex, actionObj);
                    this.i = j;
                    return piece;
                }
                case ';': {
                    enclosingLevel = 0;
                    switch (state) {
                        case ACTION: {
                            this.exception(j, "An extra separator found. Hover messages only take two separators");
                            return null;
                        }
                        case HOVER_MESSAGE: {
                            actionStartIndex = j;
                            state = HoverParserState.ACTION;
                            continue block23;
                        }
                        case NORMAL_MESSAGE: {
                            hoverMessageStartIndex = j;
                            if (normalMessage.length() == 0) {
                                this.exception(j, "Normal message is empty");
                                return null;
                            }
                            state = HoverParserState.HOVER_MESSAGE;
                        }
                    }
                    continue block23;
                }
                default: {
                    if (ch == '{') {
                        ++enclosingLevel;
                    }
                    switch (state) {
                        case ACTION: {
                            if (clickActionType == null) {
                                switch (ch) {
                                    case '|': {
                                        clickActionType = ClickEvent.Action.SUGGEST_COMMAND;
                                        continue block23;
                                    }
                                    case '@': {
                                        clickActionType = ClickEvent.Action.OPEN_URL;
                                        continue block23;
                                    }
                                }
                                clickActionType = ClickEvent.Action.RUN_COMMAND;
                                action.append(ch);
                                continue block23;
                            }
                            action.append(ch);
                            continue block23;
                        }
                        case NORMAL_MESSAGE: {
                            normalMessage.append(ch);
                            continue block23;
                        }
                        case HOVER_MESSAGE: {
                            hoverMessage.append(ch);
                        }
                    }
                }
            }
        }
        String extra = enclosingLevel == 0 ? "" : ". There are also " + enclosingLevel + " remaining curly bracket(s) that need to closed.";
        this.exception(this.i, "Unclosed hover message" + extra, HOVER);
        return null;
    }

    private String findEndOfColorSpecifier(ColorSpecifierType type) {
        this.savePlainStateAndReset();
        StringBuilder builder = new StringBuilder(10);
        int i = this.i;
        ++i;
        while (i + 1 < this.len) {
            char next;
            if ((next = this.str[++i]) == '}') {
                this.i = i;
                if (builder.length() == 0) {
                    this.exception(i, "Empty " + type.name);
                    return null;
                }
                return builder.toString();
            }
            if (type == ColorSpecifierType.BACKREF && this.lastBackRefSepIndex != -1) {
                if (!(next == '-' || next >= '0' && next <= '9' || next == ' ' && builder.length() == 0)) {
                    this.exception(i, "Invalid character '" + next + "' in " + ColorSpecifierType.BACKREF.name + " color specifier expected a number");
                    return null;
                }
                builder.append(next);
                continue;
            }
            if (next == '&') {
                if (type != ColorSpecifierType.BACKREF) {
                    this.exception(i, "Invalid character '&' in " + type.name + " specifier");
                    return null;
                }
                this.lastBackRefSepIndex = i;
                continue;
            }
            if (!(type == ColorSpecifierType.HEX && next >= '0' && next <= '9' || next >= 'a' && next <= 'z' || next >= 'A' && next <= 'Z' || type == ColorSpecifierType.MACRO && (next == '-' || next == '_') || type == ColorSpecifierType.BACKREF && next == ' ')) {
                if (next == ' ') {
                    this.exception(i, "Spaces aren't allowed in " + type.name + " (Did you forget to close the braces with '}'?)");
                } else {
                    this.exception(i, "Invalid character '" + next + "' in " + type.name);
                }
                return null;
            }
            builder.append(next);
        }
        this.exception(this.i, "Cannot find end of " + type.name, "{" + this.str[this.i]);
        return null;
    }

    private MessagePiece handleColorSpecifier() {
        int start = this.i + 2;
        switch (this.str[this.i + 1]) {
            case '#': {
                if (!this.settings.colorize) {
                    return null;
                }
                String hex = this.findEndOfColorSpecifier(ColorSpecifierType.HEX);
                if (hex == null) {
                    return null;
                }
                if (hex.length() != 3 && hex.length() != 6) {
                    this.exception(start, "Invalid hex color length. 3 digit and 6 digit formats are supported", hex);
                    return null;
                }
                try {
                    Color color = new Color(Integer.parseInt(hex, 16));
                    return new MessagePiece.HexColor(color);
                }
                catch (NumberFormatException ex) {
                    this.exception(start, "Invalid hex color", hex);
                    return null;
                }
            }
            case '$': {
                if (!this.settings.translatePlaceholders) {
                    return null;
                }
                String name = this.findEndOfColorSpecifier(ColorSpecifierType.MACRO);
                if (name == null) {
                    return null;
                }
                Object variable = StandardCastelPlaceholder.getRawMacro(name);
                if (variable == null) {
                    variable = SupportedLanguage.EN.getVariableRaw(name);
                }
                if (variable == null) {
                    String similar = StringUtils.findSimilar(name, StandardCastelPlaceholder.getGlobalMacros().keySet());
                    similar = similar == null ? "" : " Did you mean '" + similar + "'?";
                    this.exception(start, "Unknown macro '" + name + '\'' + similar, name);
                    return null;
                }
                return new MessagePiece.Variable(new PlaceholderType.Macro(name));
            }
            case '%': {
                if (!this.settings.colorize) {
                    return null;
                }
                PlaceholderBuilder placeholderBuilder = new PlaceholderBuilder(this.i + 2, this.str);
                if (!placeholderBuilder.evaluate()) {
                    this.exception(start, "Could not parse placeholder for color accessor");
                    return null;
                }
                MessagePiece.Variable var = new MessagePiece.Variable(placeholderBuilder.build());
                this.i = placeholderBuilder.getStopIndex();
                String indexStr = this.findEndOfColorSpecifier(ColorSpecifierType.BACKREF);
                if (indexStr == null) {
                    return null;
                }
                indexStr = indexStr.trim();
                int index = -1;
                if (!indexStr.isEmpty()) {
                    try {
                        index = Integer.parseInt(indexStr);
                    }
                    catch (NumberFormatException ex) {
                        this.exception(this.lastBackRefSepIndex + 1, "Invalid color accessor index '" + indexStr.trim() + '\'', indexStr);
                        return null;
                    }
                }
                if (index == 0) {
                    this.exception(this.lastBackRefSepIndex + 1, "Color accessor cannot have an index of 0", indexStr);
                    return null;
                }
                return new MessagePiece.ColorAccessor(index, var);
            }
        }
        return null;
    }

    private static char[] builderToChars(StringBuilder builder) {
        char[] chars = new char[builder.length()];
        builder.getChars(0, builder.length(), chars, 0);
        return chars;
    }

    private void exception(int ofs, String problem) {
        this.exception(ofs, problem, null);
    }

    private void exception(int ofs, String problem, String target) {
        int i;
        if (!this.settings.validate) {
            return;
        }
        String msg = new String(this.str);
        String errMsg = problem + " at offset " + ofs;
        int totalOfs = 0;
        List<String> lines = StringUtils.split(msg, '\n', true);
        for (i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            if (totalOfs + line.length() >= ofs) {
                errMsg = lines.size() == 1 ? errMsg + " in message:\n" : errMsg + " in " + StringUtils.toOrdinalNumeral(i + 1) + " line of message:\n";
                if (i != 0) {
                    errMsg = errMsg + "...\n";
                }
                errMsg = errMsg + '\"' + line + '\"';
                break;
            }
            totalOfs += line.length() + 1;
        }
        int max = 0;
        Collection<Integer> pointers = MessageCompilerException.pointerToName(ofs - totalOfs, target);
        pointers.add(ofs - totalOfs);
        for (Integer pointer : pointers) {
            if (pointer <= max) continue;
            max = pointer;
        }
        StringBuilder pointerStr = new StringBuilder(MessageCompilerException.spaces(max + 2));
        pointers.forEach(x -> pointerStr.setCharAt(x + 1, '^'));
        errMsg = errMsg + '\n' + pointerStr.toString();
        if (i + 1 != lines.size()) {
            errMsg = errMsg + "\n...";
        }
        this.exceptions.add(new MessageCompilerException(target, problem, ofs, errMsg));
    }

    void mergeExceptions(int fromIndex, MessageCompiler other) {
        if (!other.hasErrors()) {
            return;
        }
        for (MessageCompilerException ex : other.exceptions) {
            this.exception(fromIndex + ex.getIndex(), ex.getProblem(), ex.getTarget());
        }
    }

    private enum HoverParserState {
        NORMAL_MESSAGE,
        HOVER_MESSAGE,
        ACTION;
    }

    private enum ColorSpecifierType {
        HEX("hex"),
        MACRO("macro"),
        BACKREF("back reference");
        private final String name;

        private ColorSpecifierType(String name) {
            this.name = name;
        }
    }
}

