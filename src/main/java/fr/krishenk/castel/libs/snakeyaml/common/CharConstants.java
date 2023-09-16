
package fr.krishenk.castel.libs.snakeyaml.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class CharConstants {
    private static final String ALPHA_S = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_";
    private static final String LINEBR_S = "\n";
    private static final String FULL_LINEBR_S = "\r\n";
    private static final String NULL_OR_LINEBR_S = "\u0000\r\n";
    private static final String NULL_BL_LINEBR_S = " \u0000\r\n";
    private static final String NULL_BL_T_LINEBR_S = "\t \u0000\r\n";
    private static final String NULL_BL_T_S = "\u0000 \t";
    private static final String URI_CHARS_SUFFIX_S = "abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_-;/?:@&=+$_.!~*'()%";
    public static final CharConstants LINEBR = new CharConstants("\n");
    public static final CharConstants NULL_OR_LINEBR = new CharConstants("\u0000\r\n");
    public static final CharConstants NULL_BL_LINEBR = new CharConstants(" \u0000\r\n");
    public static final CharConstants NULL_BL_T_LINEBR = new CharConstants("\t \u0000\r\n");
    public static final CharConstants NULL_BL_T = new CharConstants("\u0000 \t");
    public static final CharConstants URI_CHARS_FOR_TAG_PREFIX = new CharConstants("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_-;/?:@&=+$_.!~*'()%,[]");
    public static final CharConstants URI_CHARS_FOR_TAG_SUFFIX = new CharConstants("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_-;/?:@&=+$_.!~*'()%");
    public static final CharConstants ALPHA = new CharConstants("abcdefghijklmnopqrstuvwxyz0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-_");
    private static final int ASCII_SIZE = 128;
    private final boolean[] contains = new boolean[128];
    public static final Map<Character, String> ESCAPE_REPLACEMENTS;
    public static final Map<Character, Integer> ESCAPE_CODES;

    private CharConstants(String content) {
        for (int i = 0; i < content.length(); ++i) {
            int c = content.codePointAt(i);
            this.contains[c] = true;
        }
    }

    public boolean has(int c) {
        return c < 128 && this.contains[c];
    }

    public boolean hasNo(int c) {
        return !this.has(c);
    }

    public boolean has(int c, String additional) {
        return this.has(c) || additional.indexOf(c) != -1;
    }

    public boolean hasNo(int c, String additional) {
        return !this.has(c, additional);
    }

    public static String escapeChar(String chRepresentation) {
        for (Map.Entry<Character, String> entry : ESCAPE_REPLACEMENTS.entrySet()) {
            String v = entry.getValue();
            if (" ".equals(v) || "/".equals(v) || "\"".equals(v) || !v.equals(chRepresentation)) continue;
            return "\\" + entry.getKey();
        }
        return chRepresentation;
    }

    static {
        HashMap<Character, String> escapeReplacements = new HashMap<Character, String>();
        escapeReplacements.put(Character.valueOf('0'), "\u0000");
        escapeReplacements.put(Character.valueOf('a'), "\u0007");
        escapeReplacements.put(Character.valueOf('b'), "\b");
        escapeReplacements.put(Character.valueOf('t'), "\t");
        escapeReplacements.put(Character.valueOf('n'), LINEBR_S);
        escapeReplacements.put(Character.valueOf('v'), "\u000b");
        escapeReplacements.put(Character.valueOf('f'), "\f");
        escapeReplacements.put(Character.valueOf('r'), "\r");
        escapeReplacements.put(Character.valueOf('e'), "\u001b");
        escapeReplacements.put(Character.valueOf(' '), " ");
        escapeReplacements.put(Character.valueOf('\"'), "\"");
        escapeReplacements.put(Character.valueOf('/'), "/");
        escapeReplacements.put(Character.valueOf('\\'), "\\");
        escapeReplacements.put(Character.valueOf('N'), "\u0085");
        escapeReplacements.put(Character.valueOf('_'), "\u00a0");
        escapeReplacements.put(Character.valueOf('L'), "\u2028");
        escapeReplacements.put(Character.valueOf('P'), "\u2029");
        ESCAPE_REPLACEMENTS = Collections.unmodifiableMap(escapeReplacements);
        HashMap<Character, Integer> escapeCodes = new HashMap<Character, Integer>();
        escapeCodes.put(Character.valueOf('x'), 2);
        escapeCodes.put(Character.valueOf('u'), 4);
        escapeCodes.put(Character.valueOf('U'), 8);
        ESCAPE_CODES = Collections.unmodifiableMap(escapeCodes);
    }
}

