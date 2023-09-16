package fr.krishenk.castel.locale;

import com.google.common.base.Strings;
import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.compiler.MessageCompiler;
import fr.krishenk.castel.locale.compiler.MessageObject;
import fr.krishenk.castel.locale.compiler.placeholders.StandardCastelPlaceholder;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.locale.provider.MessageProvider;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandler {
    protected static final boolean SIXTEEN;
    protected static final char HEX_CHAR = 'x';
    protected static final char HEX_CODE = '#';
    protected static final char COLOR_CHAR = '\u00a7';
    protected static final char COLOR_CODE = '&';
    protected static final char[] HEXADECIMAL;

    public static String translateComplexColor(String str) {
        if ((str = StringUtils.deleteWhitespace(str)).charAt(0) == '&') {
            return MessageHandler.colorize(str);
        }
        if (str.indexOf(44) > 1) {
            List<String> rgb = StringUtils.split(str, ',', true);
            int r = MathUtils.parseInt(rgb.get(0), false);
            int g = MathUtils.parseInt(rgb.get(1), false);
            int b = MathUtils.parseInt(rgb.get(2), false);
            if (r > 255) {
                throw new IllegalArgumentException("Invalid R value of RGB: " + r);
            }
            if (g > 255) {
                throw new IllegalArgumentException("Invalid G value of RGB: " + g);
            }
            if (b > 255) {
                throw new IllegalArgumentException("Invalid B value of RGB: " + b);
            }
            return MessageHandler.rgbToHexString(r, g, b);
        }
        int len = str.length();
        if (len == 3) {
            MessageHandler.allHexOrDigit(str);
            char r = str.charAt(0);
            char g = str.charAt(1);
            char b = str.charAt(2);
            return String.valueOf('\u00a7') + 'x' + '\u00a7' + r + '\u00a7' + r + '\u00a7' + g + '\u00a7' + g + '\u00a7' + b + '\u00a7' + b;
        }
        if (len == 6) {
            MessageHandler.allHexOrDigit(str);
            return String.valueOf('\u00a7') + 'x' + '\u00a7' + str.charAt(0) + '\u00a7' + str.charAt(1) + '\u00a7' + str.charAt(2) + '\u00a7' + str.charAt(3) + '\u00a7' + str.charAt(4) + '\u00a7' + str.charAt(5);
        }
        throw new IllegalArgumentException("Unknown complex color format/name: " + str);
    }

    public static String hex(String str) {
        return String.valueOf('\u00a7') + 'x' + '\u00a7' + str.charAt(0) + '\u00a7' + str.charAt(1) + '\u00a7' + str.charAt(2) + '\u00a7' + str.charAt(3) + '\u00a7' + str.charAt(4) + '\u00a7' + str.charAt(5);
    }

    private static void allHexOrDigit(String str) {
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            if (MessageHandler.isHexOrDigit(str.charAt(i))) continue;
            throw new IllegalArgumentException("Invalid character for hex color " + str + ": " + str.charAt(i));
        }
    }

    public static String replaceVariables(String str, Object ... edits) {
        if (edits == null || edits.length == 0 || Strings.isNullOrEmpty(str)) {
            return str;
        }
        if (edits[0] instanceof Collection) {
            throw new IllegalArgumentException("First element of edits is a collection (bad method use)");
        }
        if ((edits.length & 1) != 0) {
            throw new IllegalArgumentException("No replacement is specified for the last variable: \"" + edits[edits.length - 1] + "\" in \"" + str + '\"' + " with edits: \"" + Arrays.toString(edits) + '\"');
        }
        int len = edits.length - 1;
        for (int i = 0; i < len; ++i) {
            String variable = String.valueOf(edits[i]);
            Object replacement = edits[++i];
            str = MessageHandler.replace(str, variable, replacement);
        }
        return str;
    }

    public static String replaceVariables(String str, List<Object> edits) {
        if (edits == null || edits.isEmpty() || Strings.isNullOrEmpty(str)) {
            return str;
        }
        if (edits.get(0) instanceof Collection) {
            throw new IllegalArgumentException("First element of edits is a collection (bad method use)");
        }
        if ((edits.size() & 1) != 0) {
            throw new IllegalArgumentException("No replacement is specified for the last variable: \"" + edits.get(edits.size() - 1) + "\" in \"" + str + '\"' + " with edits: \"" + Arrays.toString(edits.toArray()) + '\"');
        }
        String variable = null;
        for (Object edit : edits) {
            if (variable == null) {
                variable = String.valueOf(edit);
                continue;
            }
            str = MessageHandler.replace(str, variable, edit);
            variable = null;
        }
        return str;
    }

    public static String replace(String text, String variable, Object replace) {
        String replacement;
        int find = text.indexOf(variable);
        if (find == -1) {
            return text;
        }
        int start = 0;
        int len = text.length();
        int varLen = variable.length();
        StringBuilder builder = new StringBuilder(len);
        if (replace instanceof Supplier) {
            Supplier replacer = (Supplier)replace;
            replacement = String.valueOf(replacer.get());
        } else {
            replacement = String.valueOf(replace);
        }
        while (find != -1) {
            builder.append(text, start, find).append(replacement);
            start = find + varLen;
            find = text.indexOf(variable, start);
        }
        builder.append(text, start, len);
        return builder.toString();
    }

    public static void sendMessage(CommandSender receiver, String message, boolean prefix) {
        Objects.requireNonNull(receiver, "Cannot send message to null receiver");
        if (!Strings.isNullOrEmpty(message)) {
            MessageProvider msg = MessageCompiler.compile(message, false, false).getSimpleProvider();
            msg.send(receiver, (new MessageBuilder()).usePrefix(prefix));
        }
    }

    /*
     * Unable to fully structure code
     * Enabled aggressive block sorting
     * Lifted jumps to return sites
     */
    public static String colorize(String str) {
        if (str != null && !str.isEmpty()) {
            if (false/*Masswar.QUANTUM_STATE*/) {
//                return Masswar.colorize(str);
                return "null";
            } else {
                int len = str.length() - 1;
                int hexState;
                if (!SIXTEEN) {
                    char[] chars = str.toCharArray();

                    for(hexState = 0; hexState < len; ++hexState) {
                        if (chars[hexState] == '&' && isColorCode(chars[hexState + 1])) {
                            chars[hexState++] = 167;
                        }
                    }

                    return new String(chars);
                } else {
                    StringBuilder builder = new StringBuilder(len + 50);
                    hexState = -1;
                    boolean escape = false;

                    for(int i = 0; i < len; ++i) {
                        char current = str.charAt(i);
                        if (current == '\\') {
                            escape = true;
                        } else if (current != '{') {
                            if (escape) {
                                escape = false;
                                builder.append('\\');
                            } else if (hexState >= 0) {
                                if (isHexOrDigit(current)) {
                                    builder.append('§').append(current);
                                    if (hexState++ == 6) {
                                        hexState = -1;
                                    }
                                } else {
                                    hexState = -1;
                                    builder.append(current);
                                }
                            } else if (current == '&') {
                                char next = str.charAt(i + 1);
                                boolean isHex = next == '#';
                                if (!isHex && !isColorCode(next)) {
                                    builder.append('&');
                                } else {
                                    builder.append('§');
                                    if (isHex) {
                                        builder.append('x');
                                        hexState = 1;
                                        ++i;
                                    }
                                }
                            } else {
                                builder.append(current);
                            }
                        } else {
                            int last;
                            if (i > 0) {
                                last = str.charAt(i - 1);
                                if (escape || last == 58) {
                                    if (escape) {
                                        builder.append('\\');
                                        escape = false;
                                    }

                                    builder.append('{');
                                    continue;
                                }
                            }

                            last = str.indexOf(125, i + 1);
                            if (last == -1) {
                                throw new IllegalStateException("Found unclosed replacement field at: " + i + ", " + str);
                            }

                            String replacementField = StringUtils.deleteWhitespace(str.substring(i + 1, last));
                            if (replacementField.length() >= 2) {
                                char functionality = replacementField.charAt(0);
                                if (functionality == '#') {
                                    String excludeSymbol = replacementField.substring(1);
                                    MessageObject color = StandardCastelPlaceholder.getMacro(Objects.requireNonNull(StringUtils.toLatinLowerCase(excludeSymbol)), new MessageBuilder());
                                    if (color != null) {
                                        builder.append(color.buildPlain(new MessageBuilder()));
                                    } else {
                                        builder.append(translateComplexColor(excludeSymbol));
                                    }

                                    i = last;
                                } else {
                                    builder.append('{');
                                }
                            } else {
                                builder.append('{');
                            }
                        }
                    }

                    if (hexState == 6) {
                        builder.append('§');
                    }

                    builder.append(str.charAt(len));
                    return builder.toString();
                }
            }
        } else {
            return str;
        }
    }

    public static boolean isColorCode(char ch) {
        return MessageHandler.isHexOrDigit(ch) || MessageHandler.isFormattingCode(ch);
    }

    protected static boolean isFormattingCode(char ch) {
        return ch >= 'K' && ch <= 'O' || ch >= 'k' && ch <= 'o' || ch == 'R' || ch == 'r';
    }

    protected static boolean isHexOrDigit(char ch) {
        return ch >= '0' && ch <= '9' || ch >= 'A' && ch <= 'F' || ch >= 'a' && ch <= 'f';
    }

    public static String stripColors(String str, boolean strip) {
        int i;
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len < 2) {
            return str;
        }
        if (SIXTEEN) {
            StringBuilder builder = new StringBuilder(len);
            int hexState = -1;
            for (int i2 = 0; i2 < len - 1; ++i2) {
                char ch = str.charAt(i2);
                if (ch == '\u00a7') {
                    boolean isHex;
                    char next = str.charAt(i2 + 1);
                    if (hexState != -1) {
                        if (MessageHandler.isHexOrDigit(next)) {
                            if (strip) {
                                builder.append(next);
                            }
                            if (hexState++ == 6) {
                                hexState = -1;
                            }
                            ++i2;
                            continue;
                        }
                        hexState = -1;
                        continue;
                    }
                    boolean bl = isHex = next == 'x';
                    if (!isHex && !MessageHandler.isColorCode(next)) continue;
                    if (strip) {
                        builder.append('&');
                    }
                    if (isHex) {
                        if (strip) {
                            builder.append('#');
                        }
                        hexState = 0;
                    } else if (strip) {
                        builder.append(next);
                    }
                    ++i2;
                    continue;
                }
                hexState = -1;
                builder.append(ch);
            }
            builder.append(str.charAt(len - 1));
            return builder.toString();
        }
        if (strip) {
            char[] chars = str.toCharArray();
            for (int i3 = 0; i3 < len - 1; ++i3) {
                if (chars[i3] != '\u00a7' || !MessageHandler.isColorCode(chars[i3 + 1])) continue;
                chars[i3++] = 38;
            }
            return new String(chars);
        }
        char[] chars = new char[len];
        int count = 0;
        for (i = 0; i < len - 1; ++i) {
            char ch = str.charAt(i);
            if (ch == '\u00a7' && MessageHandler.isColorCode(str.charAt(i + 1))) {
                ++i;
                continue;
            }
            chars[count++] = ch;
        }
        if (i != len) {
            chars[count] = str.charAt(len - 1);
        }
        return new String(chars, 0, count + 1);
    }

    public static String removePattern(String str, Pattern pattern) {
        Matcher matcher = pattern.matcher(str);
        boolean result = matcher.find();
        if (!result) {
            return str;
        }
        StringBuilder builder = new StringBuilder(str.length());
        int lastAppendedPosition = 0;
        do {
            builder.append(str, lastAppendedPosition, matcher.start());
            lastAppendedPosition = matcher.end();
        } while (result = matcher.find());
        builder.append(str, lastAppendedPosition, str.length());
        return builder.toString();
    }

    public static String parseRGB(String rgb) {
        List<String> args = StringUtils.split(rgb, ',', false);
        if (args.size() != 3) {
            return null;
        }
        return rgb;
    }

    public static String rgbToHexString(int r, int g, int b) {
        return String.valueOf('\u00a7') + 'x' + '\u00a7' + HEXADECIMAL[r / 16 % 16] + '\u00a7' + HEXADECIMAL[r % 16] + '\u00a7' + HEXADECIMAL[g / 16 % 16] + '\u00a7' + HEXADECIMAL[g % 16] + '\u00a7' + HEXADECIMAL[b / 16 % 16] + '\u00a7' + HEXADECIMAL[b % 16];
    }

    public static void sendMessage(CommandSender receiver, String message) {
        if (receiver instanceof Player) {
            MessageHandler.sendPlayerMessage((Player)receiver, message);
        } else {
            MessageHandler.sendConsoleMessage(message);
        }
    }

    public static void sendPluginMessage(CommandSender receiver, String message) {
        if (receiver instanceof Player) {
            MessageHandler.sendPlayerPluginMessage((Player)receiver, message);
        } else {
            MessageHandler.sendConsolePluginMessage(message);
        }
    }

    public static void sendPlayerMessage(Player player, String message) {
        MessageHandler.sendMessage(player, message, false);
    }

    public static void sendPlayerPluginMessage(Player player, String message) {
        MessageHandler.sendMessage(player, message, true);
    }

    public static void sendConsoleMessage(String message) {
        MessageHandler.sendMessage(Bukkit.getConsoleSender(), message, false);
    }

    public static void sendConsolePluginMessage(String message) {
        MessageHandler.sendMessage(Bukkit.getConsoleSender(), message, true);
    }

    public static void sendPlayersMessage(String message) {
        for (Player players : Bukkit.getOnlinePlayers()) {
            MessageHandler.sendMessage(players, message, false);
        }
    }

    public static void sendPlayersPluginMessage(String message) {
        for (Player players : Bukkit.getOnlinePlayers()) {
            MessageHandler.sendMessage(players, message, true);
        }
    }

    public static void debug(String message) {
        if (!Config.DEBUG.getBoolean()) {
            return;
        }
        String msg = "&8[&5DEBUG&8] &4" + message;
        MessageHandler.sendMessage(Bukkit.getConsoleSender(), msg, true);
        for (Player players : Bukkit.getOnlinePlayers()) {
            if (!CastelPluginPermission.DEBUG.hasPermission(players)) continue;
            MessageHandler.sendMessage(players, msg, true);
        }
    }

    public static Supplier<?> supply(Supplier<?> replacer) {
        return replacer;
    }

    static {
        boolean sixteen;
        HEXADECIMAL = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        try {
            Class.forName("org.bukkit.entity.Zoglin");
            sixteen = true;
        }
        catch (ClassNotFoundException ignored) {
            sixteen = false;
        }
        SIXTEEN = sixteen;
    }
}
