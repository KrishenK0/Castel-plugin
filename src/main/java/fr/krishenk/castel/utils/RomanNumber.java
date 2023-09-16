package fr.krishenk.castel.utils;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.common.value.qual.IntRange;

public class RomanNumber {
    private static final int[] ROMAN_CHARS = new int[22];
    private static final String[] M = new String[]{"", "M", "MM", "MMM"};
    private static final String[] C = new String[]{"", "C", "CC", "CCC", "CD", "D", "DC", "DCC", "DCCC", "CM"};
    private static final String[] X = new String[]{"", "X", "XX", "XXX", "XL", "L", "LX", "LXX", "LXXX", "XC"};
    private static final String[] I = new String[]{"", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX"};

    public static @NonNull String toRoman(@IntRange(from=1L, to=3999L) int num) {
        if (num <= 0 || num >= 4000) {
            return String.valueOf(num);
        }
        String thousands = M[num / 1000];
        String hundereds = C[num % 1000 / 100];
        String tens = X[num % 100 / 10];
        String ones = I[num % 10];
        return thousands + hundereds + tens + ones;
    }

    private static @IntRange(from=0L, to=1000L) int value(char chr) {
        int index = chr - 67;
        return index < 0 || index > ROMAN_CHARS.length ? 0 : ROMAN_CHARS[index];
    }

    public static @IntRange(from=0L, to=3999L) int fromRoman(@Nullable CharSequence roman) {
        if (roman == null) {
            return 0;
        }
        int len = roman.length();
        if (len == 0) {
            return 0;
        }
        int result = 0;
        for (int i = 0; i < --len; ++i) {
            int first = RomanNumber.value(roman.charAt(i));
            if (first == 0) {
                return 0;
            }
            if (first >= RomanNumber.value(roman.charAt(i + 1))) {
                result += first;
                continue;
            }
            result -= first;
        }
        int last = RomanNumber.value(roman.charAt(len));
        return last == 0 ? 0 : result + last;
    }

    static {
        int start = 67;
        RomanNumber.ROMAN_CHARS[77 - start] = 1000;
        RomanNumber.ROMAN_CHARS[68 - start] = 500;
        RomanNumber.ROMAN_CHARS[67 - start] = 100;
        RomanNumber.ROMAN_CHARS[76 - start] = 50;
        RomanNumber.ROMAN_CHARS[88 - start] = 10;
        RomanNumber.ROMAN_CHARS[86 - start] = 5;
        RomanNumber.ROMAN_CHARS[73 - start] = 1;
    }
}


