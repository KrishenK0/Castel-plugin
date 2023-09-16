package fr.krishenk.castel.utils.string;

import com.google.common.base.Strings;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.MessageHandler;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.apache.commons.lang.Validate;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {
    public static final int INDEX_NOT_FOUND = -1;
    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###.##", new DecimalFormatSymbols(Locale.ENGLISH));
    private static final Pattern CAPITALIZED_NAME = Pattern.compile("([A-Z])");

    public static String random(int minLength, int maxLength, String characters) {
        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int length = rand.nextInt(minLength, maxLength + 1);
        char[] buffer = new char[length <= 0 ? rand.nextInt(Math.abs(length)) : length];
        while (length-- > 0) {
            buffer[length] = characters.charAt(rand.nextInt(characters.length()));
        }
        return new String(buffer);
    }

    public static String findCapitalized(String str) {
        Matcher matcher = CAPITALIZED_NAME.matcher(str);
        StringBuilder finalStr = new StringBuilder();
        while (matcher.find()) {
            finalStr.append(matcher.group());
        }
        return finalStr.toString();
    }

    public static Integer parseInt(Player player, String str) {
        try {
            return Integer.parseInt(str);
        }
        catch (NumberFormatException ex) {
            Lang.INVALID_NUMBER.sendError(player, "arg", str);
            player.sendMessage("Error : " + str + " not a number");
            return null;
        }
    }

    public static Double parseDouble(Player player, String str) {
        try {
            return Double.parseDouble(str);
        }
        catch (NumberFormatException ex) {
            Lang.INVALID_NUMBER.sendError(player, "arg", str);
            player.sendMessage("Error : " + str + " not a number");
            return null;
        }
    }

    @Nullable
    public static String capitalize(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        int len = str.length();
        StringBuilder builder = new StringBuilder(len);
        boolean capitalizeNext = true;
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (ch == ' ' || ch == '_') {
                if (ch == '_') {
                    builder.append(' ');
                }
                capitalizeNext = true;
                continue;
            }
            if (capitalizeNext) {
                builder.append(Character.toTitleCase(ch));
                capitalizeNext = false;
                continue;
            }
            builder.append(Character.toLowerCase(ch));
        }
        return builder.toString();
    }

    public static boolean contains(String str, char searchChar) {
        return str.indexOf(searchChar) >= 0;
    }

    public static List<String> splitByLength(List<String> strings, int length) {
        ArrayList<String> cutStrings = new ArrayList<String>(strings.size());
        for (String string : strings) {
            while (string.length() > length) {
                cutStrings.add(string.substring(0, length));
                string = string.substring(length);
            }
            cutStrings.add(string);
        }
        return cutStrings;
    }

    public static String configOption(@Nullable Enum<?> enumeral) {
        return enumeral == null ? null : StringUtils.configOption(enumeral.name());
    }

    public static String configOptionToEnum(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        int len = str.length();

        for (int i = 0; i < len; ++i) {
            char ch = chars[i];
            if (ch == '-') {
                chars[i] = '_';
            } else {
                chars[i] = (char) (ch & 95);
            }
        }

        return new String(chars);

    }

    public static String configOption(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        int len = str.length();

        for (int i = 0; i < len; ++i) {
            char ch = chars[i];
            if (ch == '_') {
                chars[i] = '-';
            } else {
                chars[i] = (char) (ch | 32);
            }
        }
        return new String(chars);
    }

    public static void logComponents(BaseComponent... components) {
        for (BaseComponent component : components) {
            String color = component.getColor().name();
            StringBuilder formats = new StringBuilder();
            if (component.isBold()) {
                formats.append('b');
            }
            if (component.isItalic()) {
                formats.append('i');
            }
            if (component.isObfuscated()) {
                formats.append('k');
            }
            if (component.isUnderlined()) {
                formats.append('_');
            }
            if (component.isStrikethrough()) {
                formats.append('-');
            }
            if (formats.length() != 0) {
                formats.insert(0, " &2Formats&8: &3");
            }
            String text = component instanceof TextComponent ? "&2Text&8: &r'" + ((TextComponent)component).getText() + "' " : "";
            MessageHandler.sendConsolePluginMessage(text + "&2Color&8: &e" + color + formats);
        }
    }

    public static String findSimilar(String name, Collection<String> names) {
        String lowerCaseName = name.toLowerCase();
        Optional<String> exact = names.stream().filter(x -> lowerCaseName.equals(x.toLowerCase())).findFirst();
        return exact.orElseGet(() -> names.stream().filter(x -> {
            boolean contains;
            String fn = x.toLowerCase();
            boolean bl = contains = lowerCaseName.contains(fn) || fn.contains(lowerCaseName);
            if (!contains) {
                return false;
            }
            int len1 = lowerCaseName.length();
            int len2 = fn.length();
            return Math.abs(len1 - len2) < (len1 >= 5 && len2 >= 5 ? 10 : 3);
        }).findFirst().orElse(null));
    }

    public static boolean areElementsEmpty(Collection<String> strings) {
        if (strings == null || strings.isEmpty()) {
            return true;
        }
        for (String string : strings) {
            if (string.trim().isEmpty()) continue;
            return false;
        }
        return true;
    }

    public static String repeat(String str, int times) {
        if (str.isEmpty() || times == 0) {
            return "";
        }
        char[] chars = str.toCharArray();
        char[] builder = new char[chars.length * times];
        int index = 0;
        while (times-- > 0) {
            for (char character : chars) {
                builder[index++] = character;
            }
        }
        return new String(builder);
    }

    public static String repeat(char ch, int times) {
        char[] builder = new char[times];
        Arrays.fill(builder, ch);
        return new String(builder);
    }

    public static boolean containsWhitespace(String str) {
        for (char ch : str.toCharArray()) {
            if (!Character.isWhitespace(ch)) continue;
            return false;
        }
        return true;
    }

    @Nullable
    public static String toLatinLowerCase(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        int len = chars.length;
        boolean changed = false;
        for (int i = 0; i < len; ++i) {
            char ch = chars[i];
            if (ch >= 'A' && ch <= 'Z') {
                ch = (char)(ch | 0x20);
                changed = true;
            }
            chars[i] = ch;
        }
        return changed ? new String(chars) : str;
    }

    public static String replaceOnce(String text, char search, String replacement) {
        if (Strings.isNullOrEmpty(text)) {
            return text;
        }
        int end = text.indexOf(search);
        if (end == -1) {
            return text;
        }
        return text.substring(0, end) + replacement + text.substring(end + 1);
    }

    public static CharSequence replace(String text, char search, String replacement) {
        if (Strings.isNullOrEmpty(text)) {
            return text;
        }
        int end = text.indexOf(search);
        if (end == -1) {
            return text;
        }
        int increase = Math.max(0, replacement.length() - 1) * 50;
        StringBuilder buf = new StringBuilder(text.length() + increase);
        int start = 0;
        while (end != -1) {
            buf.append(text, start, end).append(replacement);
            start = end + 1;
            end = text.indexOf(search, start);
        }
        return buf.append(text.substring(start));
    }

    public static CharSequence replace(String text, char search, char replacement) {
        if (Strings.isNullOrEmpty(text)) {
            return text;
        }
        int index = text.indexOf(search);
        if (index < 0) {
            return text;
        }
        char[] chars = text.toCharArray();
        while (index > 0) {
            chars[index] = replacement;
            index = text.indexOf(search, index + 1);
        }
        return new String(chars);
    }

    public static String generatedToString(Object instance) {
        StringBuilder builder = new StringBuilder(instance.getClass().getSimpleName() + '{');
        for (Field declaredField : instance.getClass().getDeclaredFields()) {
            declaredField.setAccessible(true);
            try {
                Object result = declaredField.get(instance);
                builder.append(declaredField.getName()).append('=').append(result).append(", ");
            }
            catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return builder.append('}').toString();
    }

    @Nullable
    public static String toLatinUpperCase(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        int len = chars.length;
        boolean changed = false;
        for (int i = 0; i < len; ++i) {
            char ch = chars[i];
            if (ch >= 'a' && ch <= 'z') {
                ch = (char)(ch & 0x5F);
                changed = true;
            }
            chars[i] = ch;
        }
        return changed ? new String(chars) : str;
    }

    public static String upperCaseReplaceChar(@Nullable String str, char variable, char replacement) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        int len = chars.length;
        for (int i = 0; i < len; ++i) {
            char ch = chars[i];
            if (ch == variable) {
                ch = replacement;
            } else if (ch >= 'a' && ch <= 'z') {
                ch = (char)(ch & 0x5F);
            }
            chars[i] = ch;
        }
        return new String(chars);
    }

    public static String join(Object[] array, String separator) {
        if (array == null) {
            return null;
        }
        return StringUtils.join(array, separator, 0, array.length);
    }

    public static String join(Object[] array, String separator, int startIndex, int endIndex) {
        int bufSize;
        if (array == null) {
            return null;
        }
        if (separator == null) {
            separator = "";
        }
        if ((bufSize = endIndex - startIndex) <= 0) {
            return "";
        }
        StringBuilder buf = new StringBuilder(bufSize *= (array[startIndex] == null ? 16 : array[startIndex].toString().length()) + separator.length());
        for (int i = startIndex; i < endIndex; ++i) {
            if (i > startIndex) {
                buf.append(separator);
            }
            if (array[i] == null) continue;
            buf.append(array[i]);
        }
        return buf.toString();
    }

    public static String lowerCaseReplaceChar(@Nullable String str, char variable, char replacement) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        int len = chars.length;
        for (int i = 0; i < len; ++i) {
            char ch = chars[i];
            if (ch == variable) {
                ch = replacement;
            } else if (ch >= 'a' && ch <= 'z') {
                ch = (char)(ch | 0x20);
            }
            chars[i] = ch;
        }
        return new String(chars);
    }

    public static boolean isEnglish(@Nullable CharSequence str) {
        if (str == null) {
            return false;
        }
        int len = str.length();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            char chr = str.charAt(i);
            if (chr == '_' || chr == ' ' || StringUtils.isEnglishLetterOrDigit(chr)) continue;
            return false;
        }
        return true;
    }

    public static URL validateURL(String str) {
        try {
            URL url = new URL(str);
            url.toURI();
            return url;
        }
        catch (MalformedURLException | URISyntaxException e) {
            return null;
        }
    }

    public static boolean hasSymbol(@Nullable CharSequence str) {
        if (str == null) return false;
        int len = str.length();
        if (len == 0) return false;
        for(int i = 0; i < len; ++i) {
            char chr = str.charAt(i);
            if (chr != '_' && chr != ' ' && !Character.isLetterOrDigit(chr)) {
                return true;
            }
        }

        return false;
    }

    public static boolean isEnglishLetter(char ch) {
        return ch >= 'A' && ch <= 'Z' || ch >= 'a' && ch <= 'z';
    }

    public static boolean isEnglishDigit(char ch) {
        return ch >= '0' && ch <= '9';
    }

    @Nonnull
    public static List<String> cleanSplit(@Nonnull String str, char separator) {
        return StringUtils.split(StringUtils.deleteWhitespace(str), separator, false);
    }

    @Nonnull
    public static List<String> cleanSplitManaged(@Nonnull String str, char separator) {
        if (separator != ' ') {
            str = StringUtils.deleteWhitespace(str);
        }
        return StringUtils.split(str, separator, false);
    }

    @Nullable
    public static String deleteWhitespace(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        int len = str.length();
        char[] chs = new char[len];
        int count = 0;
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (ch == ' ') continue;
            chs[count++] = ch;
        }
        if (count == len) {
            return str;
        }
        return new String(chs, 0, count);
    }

    public static CharSequence join(char delimiter, CharSequence ... elements) {
        int len = elements.length;
        if (len == 0) {
            return null;
        }
        if (len == 1) {
            return elements[0];
        }
        for (CharSequence ele : elements) {
            len += ele.length();
        }
        char[] builder = new char[len - 1];
        len = elements.length;
        int i = 0;
        for (CharSequence ele : elements) {
            int eleLen = ele.length();
            for (int j = 0; j < eleLen; ++j) {
                builder[i++] = ele.charAt(j);
            }
            if (--len <= 0) continue;
            builder[i++] = delimiter;
        }
        return new String(builder);
    }

    public static String join(String delimiter, Collection<String> elements, Function<String, String> decorator) {
        return StringUtils.join(delimiter, elements, decorator, "[]");
    }

    public static String join(String delimiter, Collection<String> elements, Function<String, String> decorator, String empty) {
        if (elements.isEmpty()) {
            return empty;
        }
        StringBuilder joined = new StringBuilder();
        int i = 0;
        for (String element : elements) {
            if (i++ != 0) {
                joined.append(delimiter);
            }
            joined.append(decorator.apply(element));
        }
        return joined.toString();
    }

    public static String toOrdinalNumeral(int num) {
        if (num <= 0) {
            throw new IllegalArgumentException("Ordinal numerals must start from 1");
        }
        String str = Integer.toString(num);
        char lastDigit = str.charAt(str.length() - 1);
        if (lastDigit == '1') {
            return str + "st";
        }
        if (lastDigit == '2') {
            return str + "nd";
        }
        if (lastDigit == '3') {
            return str + "rd";
        }
        return str + "th";
    }

    @Nonnull
    public static List<String> split(@Nonnull String str, char separatorChar, boolean preserveAllTokens) {
        if (str == null) {
            throw new IllegalArgumentException("Cannot split a null string: " + str);
        }
        ArrayList<String> list2 = new ArrayList<String>();
        if (str.isEmpty()) {
            list2.add("");
            return list2;
        }
        boolean match = false;
        int len = str.length();
        int start = 0;
        for (int i = 0; i < len; ++i) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list2.add(str.substring(start, i));
                    match = false;
                }
                start = i + 1;
                continue;
            }
            match = true;
        }
        if (match || preserveAllTokens) {
            list2.add(str.substring(start, len));
        }
        return list2;
    }

    @Nonnull
    public static List<SplitInfo> advancedSplit(@Nonnull String str, char separatorChar, boolean preserveAllTokens) {
        if (Strings.isNullOrEmpty(str)) {
            throw new IllegalArgumentException("Cannot split a null or empty string: " + str);
        }
        ArrayList<SplitInfo> list2 = new ArrayList<SplitInfo>();
        boolean match = false;
        boolean lastMatch = false;
        int len = str.length();
        int start = 0;
        for (int i = 0; i < len; ++i) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list2.add(new SplitInfo(str.substring(start, i), start, i));
                    match = false;
                    lastMatch = true;
                }
                start = i + 1;
                continue;
            }
            lastMatch = false;
            match = true;
        }
        if (match || preserveAllTokens && lastMatch) {
            list2.add(new SplitInfo(str.substring(start, len), start, len));
        }
        return list2;
    }

    public static String[] splitLocation(@Nonnull String str, int size) {
        String[] split = new String[size];
        int len = str.length();
        int start = 0;
        size = 0;
        for (int i = 0; i < len; ++i) {
            if (str.charAt(i) != ',') continue;
            split[size++] = str.substring(start, i);
            start = i += 2;
        }
        split[size] = str.substring(start, len);
        return split;
    }

    public static boolean isEnglishLetterOrDigit(char ch) {
        return StringUtils.isEnglishDigit(ch) || StringUtils.isEnglishLetter(ch);
    }

    public static void printStackTrace() {
        MessageHandler.sendConsolePluginMessage("&f--------------------------------------------");
        Arrays.stream(Thread.currentThread().getStackTrace()).skip(2L).forEach(stack -> {
            String clazz = stack.getClassName();
            String color = clazz.startsWith("net.minecraft") ? "&6" : (clazz.startsWith("org.bukkit") ? "&d" : (clazz.startsWith("co.aikar") || clazz.startsWith("io.papermc") || clazz.startsWith("com.destroystokyo") ? "&d" : (clazz.startsWith("java") ? "&c" : "&2")));
            MessageHandler.sendConsolePluginMessage(color + stack.getClassName() + "&8.&9" + stack.getMethodName() + "&8: &5" + stack.getLineNumber());
        });
        MessageHandler.sendConsolePluginMessage("&f--------------------------------------------");
    }

    public static boolean isCalledFromClass(Class<?> clazz) {
        String name = clazz.getSimpleName().concat(".java");
        return Arrays.stream(Thread.currentThread().getStackTrace()).skip(2L).anyMatch(stack -> name.equalsIgnoreCase(stack.getFileName()));
    }

    public static boolean fakeBool(String e) {
        System.out.println(e);
        return true;
    }

    public static boolean containsNumber(@Nullable CharSequence str) {
        if (str == null) {
            return false;
        }
        int len = str.length();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (!StringUtils.isEnglishDigit(ch)) continue;
            return true;
        }
        return false;
    }

    public static boolean containsAnyLangNumber(@Nullable CharSequence str) {
        if (str == null) {
            return false;
        }
        int len = str.length();
        if (len == 0) {
            return false;
        }
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (!Character.isDigit(ch)) continue;
            return true;
        }
        return false;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     * Loose catch block
     * Enabled aggressive exception aggregation
     */
    @Nullable
    public static Dimension getImageSize(@Nonnull URL url) {
        try (InputStream stream = url.openStream();
             ImageInputStream in = ImageIO.createImageInputStream(stream)){
            Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    Dimension dimension = new Dimension(reader.getWidth(0), reader.getHeight(0));
                    reader.dispose();
                    return dimension;
                } catch (Throwable throwable) {
                    reader.dispose();
                    throw throwable;
                }
            }
        } catch (IOException iOException) {
            // empty catch block
        } catch (Throwable throwable) {
            throw throwable;
        }
        return null;
    }

    public static boolean isNumeric(@Nullable String str) {
        char first;
        if (str == null) {
            return false;
        }
        int len = str.length();
        if (len == 0) {
            return false;
        }
        int i = 0;
        if (len != 1 && ((first = str.charAt(0)) == '-' || first == '+')) {
            i = 1;
        }
        while (i < len) {
            if (StringUtils.isEnglishDigit(str.charAt(i++))) continue;
            return false;
        }
        return true;
    }

    public static <K, V> StringBuilder associatedArrayMap(Map<K, V> map) {
        return StringUtils.associatedArrayMap(map, 1);
    }

    private static <K, V> StringBuilder associatedArrayMap(Map<K, V> map, int nestLevel) {
        if (map == null || map.isEmpty()) {
            return new StringBuilder("{}");
        }
        StringBuilder builder = new StringBuilder(map.size() * 15);
        builder.append('{');
        for (Map.Entry<K, V> entry : map.entrySet()) {
            builder.append('\n');
            builder.append(StringUtils.spaces(nestLevel * 2));
            builder.append(entry.getKey()).append(" => ");
            V val = entry.getValue();
            builder.append(val instanceof Map ? StringUtils.associatedArrayMap((Map)val, nestLevel + 1) : val);
            builder.append('\n');
        }
        return builder.append('}');
    }

    public static String spaces(int times) {
        if (times <= 0) {
            return "";
        }
        char[] spaces = new char[times];
        Arrays.fill(spaces, ' ');
        return new String(spaces);
    }

    public static boolean isPureNumber(@Nullable String str) {
        if (Strings.isNullOrEmpty(str)) {
            return false;
        }
        int len = str.length();
        for (int i = 0; i < len; ++i) {
            char ch = str.charAt(i);
            if (StringUtils.isEnglishDigit(ch)) continue;
            return false;
        }
        return true;
    }

    public static int indexOfAny(String str, String[] searchStrs) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(searchStrs);
        int ret = Integer.MAX_VALUE;
        for (String search : searchStrs) {
            int tmp;
            if (search == null || (tmp = str.indexOf(search)) == -1 || tmp >= ret) continue;
            ret = tmp;
        }
        return ret == Integer.MAX_VALUE ? -1 : ret;
    }

    @Nonnull
    public static String getGroupedOption(@Nonnull String option, int ... grouped) {
        Objects.requireNonNull(option, "Enum option name cannot be null");
        option = StringUtils.toLatinLowerCase(option);
        if (grouped.length == 0) {
            return option.replace('_', '-');
        }
        String[] split = StringUtils.splitArray(option, '_', false);
        if (split.length < grouped.length) {
            throw new IllegalArgumentException("Groups cannot be greater than enum separators");
        }
        boolean[] groups = new boolean[split.length];
        for (int groupedInt : grouped) {
            groups[groupedInt - 1] = true;
        }
        StringBuilder sb = new StringBuilder(option.length());
        for (int i = 0; i < split.length; ++i) {
            sb.append(split[i]);
            if (groups[i]) {
                sb.append('.');
                continue;
            }
            sb.append('-');
        }
        sb.setLength(sb.length() - 1);
        return sb.toString();
    }

    public static String[] splitArray(String str, char separatorChar) {
        return StringUtils.splitArray(str, separatorChar, false);
    }

    public static String[] splitArray(String str, char separatorChar, boolean preserveAllTokens) {
        if (str == null) {
            return null;
        }
        int len = str.length();
        if (len == 0) {
            return new String[0];
        }
        ArrayList<String> list2 = new ArrayList<String>();
        int i = 0;
        int start = 0;
        boolean match = false;
        boolean lastMatch = false;
        while (i < len) {
            if (str.charAt(i) == separatorChar) {
                if (match || preserveAllTokens) {
                    list2.add(str.substring(start, i));
                    match = false;
                    lastMatch = true;
                }
                start = ++i;
                continue;
            }
            lastMatch = false;
            match = true;
            ++i;
        }
        if (match || preserveAllTokens && lastMatch) {
            list2.add(str.substring(start, i));
        }
        return list2.toArray(new String[list2.size()]);
    }

    public static String reverse(String input) {
        char[] chars = input.toCharArray();
        int begin = 0;
        for (int end = chars.length - 1; end > begin; --end, ++begin) {
            char temp = chars[begin];
            chars[begin] = chars[end];
            chars[end] = temp;
        }
        return new String(chars);
    }

    public static boolean isOneOf(@Nullable String str, String ... strings) {
        return !Strings.isNullOrEmpty(str) && Arrays.asList(strings).contains(str);
    }

    @Nonnull
    public static String toFancyNumber(double number) {
        return CURRENCY_FORMAT.format(number);
    }

    public static void printConfig(ConfigurationSection section) {
        if (section == null) {
            MessageHandler.sendConsolePluginMessage("&4Attempt to print null config section");
            StringUtils.printStackTrace();
            return;
        }
        MessageHandler.sendConsolePluginMessage("&4" + section.getName() + " &7->");
        StringUtils.printConfig(section, "");
    }

    public static void printConfig(ConfigurationSection section, String nestLevel) {
        for (Map.Entry<String, Object> entry : section.getValues(false).entrySet()) {
            Object value = entry.getValue();
            if (value instanceof ConfigurationSection) {
                MessageHandler.sendConsolePluginMessage(nestLevel + "&6" + entry.getKey() + "&8:");
                StringUtils.printConfig((ConfigurationSection)value, nestLevel + ' ');
                continue;
            }
            String printer = Objects.toString(value);
            if (value == null) {
                printer = "&4null";
            } else if (value instanceof String) {
                printer = printer.contains(" ") || !StringUtils.isEnglish(printer) ? "&f\"" + printer + "&f\"" : "&f" + printer;
            } else if (value instanceof Integer) {
                printer = "&9" + printer;
            } else if (value instanceof Number) {
                printer = "&5" + printer;
            } else if (value instanceof Boolean) {
                printer = ((Boolean) value ? "&a" : "&c") + printer;
            } else if (value instanceof List) {
                CharSequence[] elements = (String[])((List)value).stream().map(Object::toString).toArray(String[]::new);
                printer = "&3[&b" + String.join("&7, &b", elements) + "&3]";
            } else {
                printer = "&e" + printer;
            }
            MessageHandler.sendConsolePluginMessage(nestLevel + "&6" + entry.getKey() + "&8: " + printer);
        }
    }

//    public static void printConfig(ConfigurationSection section) {
//        StringUtils.printConfig(section, "");
//    }

//    private static void printConfig(ConfigurationSection section, String nestLevel) {
//        for (String key : section.getKeys(false)) {
//            String str = null;
//            if (section.isInt(key)) str = "&2" + section.getInt(key);
//            else if (section.isString(key)) str = "&6" + section.getString(key);
//            else if (section.isBoolean(key)) str = "&2" + section.getBoolean(key);
//            else if (section.isList(key)) str = "&8[&6" + String.join("&7, &6", section.getStringList(key)) + "&8]";
//            else if (section.isConfigurationSection(key)) {
//                Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', nestLevel + "&f" + key + "&8:"));
//                printConfig(section.getConfigurationSection(key), nestLevel + ' ');
//            } else if (section.isSet(key)) str = "&c" + section.getBoolean(key);
//            else throw new IllegalArgumentException("Unsupported node type " + node.getNodeType());
//            Bukkit.getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', nestLevel + "&f" + key + "&8: " + str));
//        }
//    }

    public static String remove(@Nullable String str, char remove) {
        if (Strings.isNullOrEmpty(str)) {
            return str;
        }
        char[] chars = str.toCharArray();
        int pos = 0;
        for (char ch : chars) {
            if (ch == remove) continue;
            chars[pos++] = ch;
        }
        return chars.length == pos ? str : new String(chars, 0, pos);
    }

    public static String remove(String str, String remove) {
        Objects.requireNonNull(str);
        Objects.requireNonNull(remove);
        return StringUtils.replace(str, remove, "", -1);
    }

    public static String replaceOnce(String text, String searchString, String replacement) {
        return StringUtils.replace(text, searchString, replacement, 1);
    }

    public static String replace(String text, String searchString, String replacement) {
        return StringUtils.replace(text, searchString, replacement, -1);
    }

    public static String replace(String text, String searchString, String replacement, int max) {
        if (text.isEmpty() || searchString.isEmpty() || replacement == null || max == 0) {
            return text;
        }
        int start = 0;
        int end = text.indexOf(searchString, start);
        if (end == -1) {
            return text;
        }
        int replLength = searchString.length();
        int increase = replacement.length() - replLength;
        increase = Math.max(increase, 0);
        StringBuilder buf = new StringBuilder(text.length() + (increase *= max < 0 ? 16 : Math.min(max, 64)));
        while (end != -1) {
            buf.append(text, start, end).append(replacement);
            start = end + replLength;
            if (--max == 0) break;
            end = text.indexOf(searchString, start);
        }
        buf.append(text.substring(start));
        return buf.toString();
    }

    public static boolean containsAny(@Nullable String str, String ... strings) {
        if (Strings.isNullOrEmpty(str)) {
            return false;
        }
        for (String string : strings) {
            if (!str.contains(string)) continue;
            return true;
        }
        return false;
    }

    public static String buildArguments(@Nonnull String[] args, @Nonnull String joinStr, int from) {
        Objects.requireNonNull(args, "Cannot build arguments for null argument list");
        Validate.isTrue(from >= 0, "Start index should be at least 0: " + from);
        Validate.isTrue(from < args.length, "Start index cannot be equal or greater than arguments length: " + from);
        return String.join(joinStr, Arrays.stream(args).skip(from).toArray(String[]::new));
    }

    /*
    public static void performCommands(OfflinePlayer offlinePlayer, Collection<String> commands) {
        Objects.requireNonNull(offlinePlayer, "Cannot perform commands for null player");
        Player player = offlinePlayer.getPlayer();
        MessageBuilder settings = new MessageBuilder();
        if (player == null) {
            settings.withContext(offlinePlayer);
        } else {
            settings.withContext(player);
        }
        ConfigCommand.execute(player, ConfigCommand.parse(commands), new MessageBuilder().withContext(offlinePlayer), false);
    }*/

    public static String buildArguments(@Nonnull String[] args, @Nonnull String joinStr) {
        return StringUtils.buildArguments(args, joinStr, 0);
    }

    public static String buildArguments(@Nonnull String[] args, int from) {
        return StringUtils.buildArguments(args, " ", from);
    }

    public static String buildArguments(@Nonnull String[] args) {
        return StringUtils.buildArguments(args, " ", 0);
    }

    public static final class SplitInfo {
        public final String text;
        public final int index;
        public final int endIndex;

        public SplitInfo(String text, int index, int endIndex) {
            this.text = text;
            this.index = index;
            this.endIndex = endIndex;
        }
    }
}
