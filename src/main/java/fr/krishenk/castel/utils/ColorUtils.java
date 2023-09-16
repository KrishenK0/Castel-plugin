package fr.krishenk.castel.utils;

import fr.krishenk.castel.locale.compiler.MessagePiece;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.ChatColor;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ColorUtils {
    private static final byte[] DIGITS;

    public static Color hex(String color) {
        try {
            Integer codeColor;
            boolean hasAlpha;
            color = StringUtils.deleteWhitespace(StringUtils.remove(color, '#'));
            int len = color.length();
            boolean bl = hasAlpha = len == 4 || len == 8;
            if (len == 3 || len == 4) {
                color = ColorUtils.shortHexToLongHex(color);
            } else if (len != 6 && len != 8) {
                return null;
            }
            if (len == 8) {
                color = color.substring(6, 8) + color.substring(0, 6);
            }
            if ((codeColor = ColorUtils.parseHex(color)) == null) {
                return null;
            }
            return new Color(codeColor, hasAlpha);
        }
        catch (NumberFormatException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static String toString(Color color) {
        return "{ R:" + color.getRed() + " | G:" + color.getGreen() + " | B:" + color.getBlue() + " | A:" + color.getAlpha() + " }";
    }

    public static int getRGB(Color color) {
        return (color.getRed() & 0xFF) << 16 | (color.getGreen() & 0xFF) << 8 | (color.getBlue() & 0xFF) << 0;
    }

    private static Integer parseHex(CharSequence str) {
        int i = 0;
        int len = str.length();
        int result = 0;
        while (i < len) {
            char index;
            result *= 16;
            if ((index = str.charAt(i++)) >= DIGITS.length) {
                return null;
            }
            byte num = DIGITS[index];
            if (num == -1) {
                return null;
            }
            result -= num;
        }
        return -result;
    }

    public static Color parseColor(String str) {
        Color color = ColorUtils.hex(str);
        return color == null ? ColorUtils.rgb(str) : color;
    }

    public static String shortHexToLongHex(String shortHex) {
        char first = shortHex.charAt(0);
        char sec = shortHex.charAt(1);
        char th = shortHex.charAt(2);
        char alpha = shortHex.length() == 4 ? (char)shortHex.charAt(3) : (char)'F';
        return String.valueOf(alpha) + alpha + first + first + sec + sec + th + th;
    }

    public static int toHex(Color color) {
        return color.getRed() << 16 | color.getGreen() << 8 | color.getBlue();
    }

    public static int getHue(@Nonnull Color color) {
        float max;
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        float min = Math.min(Math.min(red, green), blue);
        if (min == (max = (float)Math.max(Math.max(red, green), blue))) {
            return 0;
        }
        float diff = max - min;
        float hue = max == (float)red ? (float)(green - blue) / diff : (max == (float)green ? 2.0f + (float)(blue - red) / diff : 4.0f + (float)(red - green) / diff);
        if ((hue *= 60.0f) < 0.0f) {
            hue += 360.0f;
        }
        return Math.round(hue);
    }

    public static float[] getHSB(Color color) {
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        hsb[0] = hsb[0] * 360.0f;
        hsb[1] = hsb[1] * 100.0f;
        hsb[2] = hsb[2] * 100.0f;
        return hsb;
    }

    public static String toHexString(Color color) {
        Objects.requireNonNull(color, "Cannot convert null color to hex");
        return String.format("#%06x", color.getRGB() & 0xFFFFFF);
    }

    static int legacyColorToHex(ChatColor color) {
        switch (color) {
            case WHITE: {
                return 0xFFFFFF;
            }
            case BLACK: {
                return 0;
            }
            case DARK_BLUE: {
                return 170;
            }
            case DARK_GREEN: {
                return 43520;
            }
            case DARK_AQUA: {
                return 43690;
            }
            case DARK_RED: {
                return 0xAA0000;
            }
            case DARK_PURPLE: {
                return 0xAA00AA;
            }
            case DARK_GRAY: {
                return 0x555555;
            }
            case GRAY: {
                return 0xAAAAAA;
            }
            case GOLD: {
                return 0xFFAA00;
            }
            case BLUE: {
                return 0x5555FF;
            }
            case GREEN: {
                return 0x55FF55;
            }
            case AQUA: {
                return 0x55FFFF;
            }
            case RED: {
                return 0xFF5555;
            }
            case LIGHT_PURPLE: {
                return 0xFF55FF;
            }
            case YELLOW: {
                return 0xFFFF55;
            }
        }
        throw new IllegalArgumentException("Specified chat color is not a color: " + (Object)color);
    }

    public static Color legacyColorToAwt(ChatColor color) {
        return new Color(ColorUtils.legacyColorToHex(color));
    }

    public static ChatColor hexColorToLegacy(Color color) {
        switch (ColorUtils.getRGB(color)) {
            case 0: {
                return ChatColor.BLACK;
            }
            case 0xFFFFFF: {
                return ChatColor.WHITE;
            }
            case 170: {
                return ChatColor.DARK_BLUE;
            }
            case 43520: {
                return ChatColor.DARK_GREEN;
            }
            case 43690: {
                return ChatColor.DARK_AQUA;
            }
            case 0xAA0000: {
                return ChatColor.DARK_RED;
            }
            case 0xAA00AA: {
                return ChatColor.DARK_PURPLE;
            }
            case 0x555555: {
                return ChatColor.DARK_GRAY;
            }
            case 0xAAAAAA: {
                return ChatColor.GRAY;
            }
            case 0xFFAA00: {
                return ChatColor.GOLD;
            }
            case 0x5555FF: {
                return ChatColor.BLUE;
            }
            case 0x55FF55: {
                return ChatColor.GREEN;
            }
            case 0x55FFFF: {
                return ChatColor.AQUA;
            }
            case 0xFF5555: {
                return ChatColor.RED;
            }
            case 0xFF55FF: {
                return ChatColor.LIGHT_PURPLE;
            }
            case 0xFFFF55: {
                return ChatColor.YELLOW;
            }
        }
        float[] hsb = ColorUtils.getHSB(color);
        float hue = hsb[0];
        float saturation = hsb[1];
        float brightness = hsb[2];
        if (saturation < 40.0f) {
            if (brightness < 10.0f) {
                return ChatColor.BLACK;
            }
            if (brightness < 30.0f) {
                return ChatColor.DARK_GRAY;
            }
            if (brightness < 60.0f) {
                return ChatColor.GRAY;
            }
            return ChatColor.WHITE;
        }
        if (brightness < 20.0f) {
            return ChatColor.BLACK;
        }
        float minDarkColorBrightness = 66.67f;
        if (hue > 340.0f || hue >= 0.0f && hue < 10.0f) {
            if (brightness <= 66.67f) {
                return ChatColor.DARK_RED;
            }
            return ChatColor.RED;
        }
        if (hue >= 10.0f && hue < 40.0f) {
            return ChatColor.GOLD;
        }
        if (hue >= 40.0f && hue < 70.0f) {
            return ChatColor.YELLOW;
        }
        if (hue >= 70.0f && hue < 150.0f) {
            if (brightness <= 66.67f) {
                return ChatColor.DARK_GREEN;
            }
            return ChatColor.GREEN;
        }
        if (hue >= 150.0f && hue < 190.0f) {
            if (brightness <= 66.67f) {
                return ChatColor.DARK_AQUA;
            }
            return ChatColor.AQUA;
        }
        if (hue >= 190.0f && hue < 250.0f) {
            if (brightness <= 66.67f) {
                return ChatColor.DARK_BLUE;
            }
            return ChatColor.BLUE;
        }
        if (hue >= 250.0f && hue < 280.0f) {
            return ChatColor.DARK_PURPLE;
        }
        if (hue >= 280.0f && hue < 340.0f) {
            return ChatColor.LIGHT_PURPLE;
        }
        throw new AssertionError((Object)("Undetectable legacy hex color with properties " + hue + " | " + saturation + " | " + brightness));
    }

    public static Color rgb(String color) {
        List<String> rgb = (List) StringUtils.cleanSplitManaged(color, color.contains(",") ? ',' : ' ');
        if (rgb.size() < 3) {
            return null;
        }
        try {
            int r = Integer.parseInt(rgb.get(0));
            int g = Integer.parseInt(rgb.get(1));
            int b = Integer.parseInt(rgb.get(2));
            int a = rgb.size() > 3 ? Integer.parseInt(rgb.get(3)) : 255;
            return new Color(r, g, b, a);
        }
        catch (NumberFormatException ex) {
            return null;
        }
    }

    public static ArrayList<MessagePiece> gradient(List<MessagePiece> pieces, Color from, Color to) {
        int length = 0;
        for (MessagePiece piece : pieces) {
            if (piece instanceof MessagePiece.Plain) {
                length += piece.length();
                continue;
            }
            if (piece instanceof MessagePiece.Color) continue;
            throw new IllegalArgumentException("Disallowed piece in gradient color: " + piece);
        }
        double rFactor = Math.abs((double)(from.getRed() - to.getRed()) / (double)length);
        double gFactor = Math.abs((double)(from.getGreen() - to.getGreen()) / (double)length);
        double bFactor = Math.abs((double)(from.getBlue() - to.getBlue()) / (double)length);
        if (from.getRed() > to.getRed()) {
            rFactor = -rFactor;
        }
        if (from.getGreen() > to.getGreen()) {
            gFactor = -gFactor;
        }
        if (from.getBlue() > to.getBlue()) {
            bFactor = -bFactor;
        }
        ArrayList<MessagePiece> newPieces = new ArrayList<MessagePiece>(length * 3);
        Color currentColor = new Color(from.getRGB());
        boolean encounteredColorFormat = false;
        String currentMessage = null;
        int currentMessageIndex = 0;
        for (MessagePiece piece : pieces) {
            if (piece instanceof MessagePiece.Plain) {
                if (currentMessage == null) {
                    currentMessage = ((MessagePiece.Plain)piece).getMessage();
                }
                if (encounteredColorFormat) {
                    encounteredColorFormat = false;
                    continue;
                }
                char ch = currentMessage.charAt(currentMessageIndex++);
                newPieces.add(new MessagePiece.HexColor(currentColor));
                newPieces.add(new MessagePiece.Plain(String.valueOf(ch)));
            } else if (piece instanceof MessagePiece.Color) {
                if (piece instanceof MessagePiece.SimpleColor) {
                    encounteredColorFormat = ((MessagePiece.SimpleColor)piece).getColor().isFormat();
                }
                newPieces.add(piece);
            }
            int red = (int)Math.round((double)currentColor.getRed() + rFactor);
            int green = (int)Math.round((double)currentColor.getGreen() + gFactor);
            int blue = (int)Math.round((double)currentColor.getBlue() + bFactor);
            if (red > 255) {
                red = 255;
            }
            if (red < 0) {
                red = 0;
            }
            if (green > 255) {
                green = 255;
            }
            if (green < 0) {
                green = 0;
            }
            if (blue > 255) {
                blue = 255;
            }
            if (blue < 0) {
                blue = 0;
            }
            currentColor = new Color(red, green, blue);
        }
        return newPieces;
    }

    public static Color mixColors(Color ... colors) {
        float ratio = 1.0f / (float)colors.length;
        int r = 0;
        int g = 0;
        int b = 0;
        int a = 0;
        for (Color color : colors) {
            r = (int)((float)r + (float)color.getRed() * ratio);
            g = (int)((float)g + (float)color.getGreen() * ratio);
            b = (int)((float)b + (float)color.getBlue() * ratio);
            a = (int)((float)a + (float)color.getAlpha() * ratio);
        }
        return new Color(r, g, b, a);
    }

    static {
        int i;
        DIGITS = new byte[103];
        Arrays.fill(DIGITS, (byte)-1);
        for (i = 48; i <= 57; ++i) {
            ColorUtils.DIGITS[i] = (byte)(i - 48);
        }
        for (i = 65; i <= 70; ++i) {
            ColorUtils.DIGITS[i] = (byte)(i - 65 + 10);
        }
        for (i = 97; i <= 102; ++i) {
            ColorUtils.DIGITS[i] = (byte)(i - 97 + 10);
        }
    }
}


