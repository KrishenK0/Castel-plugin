package fr.krishenk.castel.utils;

import com.google.common.base.Strings;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.locale.compiler.PlaceholderTranslationContext;
import fr.krishenk.castel.locale.compiler.placeholders.PlaceholderContextBuilder;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class MathUtils {
    private static final int INT_MAX_LENGTH = 11;
    private static final int LONG_MAX_LENGTH = 20;
    private static final int DECIMAL_SYSTEM_RADIX = 10;

    public static int getPageNumbers(int items, int size) {
        return Math.max(1, items % size != 0 ? items / size + 1 : items / size);
    }


    public static double evaluateEquation(String eqn, Object ... edits) {
        return MathUtils.withContext(eqn, new PlaceholderContextBuilder().raws(edits));
    }

    public static double eval(String eqn, OfflinePlayer placeholder, Object ... edits) {
        return MathUtils.withContext(eqn, new PlaceholderContextBuilder().raws(edits).withContext(placeholder));
    }

    private static double withContext(String eqn, PlaceholderContextBuilder settings) {
        if (Strings.isNullOrEmpty(eqn)) {
            return 0.0;
        }
        return MathUtils.eval(MathCompiler.compile(eqn), settings);
    }

    public static double eval(MathCompiler.Expression eqn, PlaceholderContextProvider context) {
        return eqn.eval(id -> MathUtils.expectDouble(id, context.processPlaceholder(id)));
    }

    public static double eval(String eqn, MessageBuilder builder) {
        return MathUtils.eval(MathCompiler.compile(eqn), builder);
    }

    public static Double expectDouble(String id, Object data) {
        if (data == null) {
            return null;
        }
        if (data instanceof PlaceholderTranslationContext && (data = ((PlaceholderTranslationContext)data).getValue()) instanceof String) {
            try {
                data = Double.parseDouble(data.toString());
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        if (data instanceof Number) {
            return ((Number)data).doubleValue();
        }
        if (data instanceof Boolean) {
            return (Boolean) data ? 1.0 : 0.0;
        }
        if (data instanceof String) {
            return (double) data.hashCode();
        }
        throw new IllegalArgumentException("Expected an arithmetic placeholder for '" + id + "' instead got '" + data + "' (" + data.getClass().getName() + ')');
    }

    public static double eval(String eqn, Guild placeholder, Object ... edits) {
        return MathUtils.withContext(eqn, new PlaceholderContextBuilder().raws(edits).withContext(placeholder));
    }

//    public static double eval(String eqn, Nation placeholder, Object ... edits) {
//        return MathUtils.withContext(eqn, new PlaceholderContextBuilder().raws(edits).withContext(placeholder));
//    }


    public static Integer parseInt(CharSequence str) {
        return MathUtils.parseInt(str, true);
    }

    public static Integer parseInt(CharSequence str, boolean signed) {
        int len = str.length();
        if (len == 0 || len > 11) {
            return null;
        }
        int i = 0;
        if (signed) {
            if (str.charAt(0) != '-') {
                signed = false;
            } else {
                if (len == 1) {
                    return null;
                }
                ++i;
            }
        }
        int limit = signed ? Integer.MIN_VALUE : -2147483647;
        int multmin = limit / 10;
        int result = 0;
        while (i < len) {
            int digit;
            if ((digit = MathUtils.digit(str.charAt(i++))) < 0 || result < multmin) {
                return null;
            }
            if ((result *= 10) < limit + digit) {
                return null;
            }
            result -= digit;
        }
        return signed ? result : -result;
    }

    public static Integer parseIntUnchecked(CharSequence str, boolean signed) {
        int i = 0;
        if (signed) {
            if (str.charAt(0) != '-') {
                signed = false;
            } else {
                ++i;
            }
        }
        int len = str.length();
        int result = 0;
        while (i < len) {
            result *= 10;
            result -= str.charAt(i++) - 48;
        }
        return signed ? result : -result;
    }

    public static Long parseLong(CharSequence str) {
        return MathUtils.parseLong(str, true);
    }

    public static Long parseLong(CharSequence str, boolean signed) {
        int len = str.length();
        if (len == 0 || len > 20) {
            return null;
        }
        int i = 0;
        if (signed) {
            if (str.charAt(0) != '-') {
                signed = false;
            } else {
                ++i;
            }
        }
        long limit = signed ? Long.MIN_VALUE : -9223372036854775807L;
        long multmin = limit / 10L;
        long result = 0L;
        while (i < len) {
            int digit;
            if ((digit = MathUtils.digit(str.charAt(i++))) < 0 || result < multmin) {
                return null;
            }
            if ((result *= 10L) < limit + (long)digit) {
                return null;
            }
            result -= digit;
        }
        return signed ? result : -result;
    }

    private static int digit(int ch) {
        int digit = ch - 48;
        return digit >= 0 && digit < 10 ? digit : -1;
    }

    public static boolean hasChance(double percent) {
        return ThreadLocalRandom.current().nextDouble(0.0, 100.0) <= percent;
    }

    public static int randInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static int getPercent(double amount, double max) {
        return MathUtils.getAmountFromAmount(amount, max, 100.0);
    }

    public static double percentOfAmount(double percent, double amount) {
        return amount * percent / 100.0;
    }

    public static int percentOfPercent(int ... percents) {
        int finalPercent = 1;
        for (int percent : percents) {
            finalPercent = (int)((double)finalPercent * ((double)percent / 100.0));
        }
        return finalPercent;
    }

    public static int getAmountFromAmount(double current, double max, double amount) {
        return (int)(current / max * amount);
    }

    public static double roundToDigits(double value, int precision) {
        if (precision <= 0) {
            return Math.round(value);
        }
        int scale = (int)Math.pow(10.0, precision);
        return (double)Math.round(value * (double)scale) / (double)scale;
    }

    public static String getShortNumber(double number) {
        if (number < 1000.0) {
            return Double.toString(number);
        }
        if (number >= 1.0E15) {
            return MathUtils.createShortNumber(number) + 'Q';
        }
        if (number >= 1.0E12) {
            return MathUtils.createShortNumber(number) + 'T';
        }
        if (number >= 1.0E9) {
            return MathUtils.createShortNumber(number) + 'B';
        }
        if (number >= 1000000.0) {
            return MathUtils.createShortNumber(number) + 'M';
        }
        return MathUtils.createShortNumber(number) + 'K';
    }

    public static String getShortNumber(long number) {
        if (number < 1000L) {
            return Long.toString(number);
        }
        if (number >= 1000000000L) {
            return MathUtils.createShortNumber(number) + 'B';
        }
        if (number >= 1000000L) {
            return MathUtils.createShortNumber(number) + 'M';
        }
        return MathUtils.createShortNumber(number) + 'K';
    }

    private static String createShortNumber(double number) {
        String str = String.valueOf((int)number);
        int candiv = str.length() % 3;
        if (candiv == 0) {
            candiv = 3;
        }
        return str.substring(0, candiv) + '.' + str.charAt(candiv);
    }

    private static String createShortNumber(long number) {
        String str = Long.toString(number);
        int candiv = str.length() % 3;
        if (candiv == 0) {
            candiv = 3;
        }
        return str.substring(0, candiv) + '.' + str.charAt(candiv);
    }

    public static int increasingRandInt(int min, int max) {
        ArrayList<Integer> numbers = new ArrayList<Integer>();
        for (int i = min; i < max; ++i) {
            for (int j = i; j >= 0; --j) {
                numbers.add(j);
            }
        }
        int randInt = MathUtils.randInt(0, numbers.size() - 1);
        return numbers.get(randInt);
    }

    public static int decreasingRandInt(int min, int max) {
        Validate.isTrue(min > 0, "Minimum number cannot be less than 1");
        Validate.isTrue(max > min, "Maximum number cannot be less than the minimum number");
        int[] numbers = new int[MathUtils.naturalSum(max - min)];
        int q = 0;
        for (int i = min; i < max; ++i) {
            int j = max - i + 1;
            while (j >= 0) {
                numbers[q++] = j--;
            }
        }
        int randomIndex = MathUtils.randInt(0, numbers.length - 1);
        return numbers[randomIndex];
    }

    public static int naturalSum(int n) {
        return n * (n + 1) / 2;
    }

    public static double factorial(int n) {
        double result = n;
        while (n > 1) {
            result *= --n;
        }
        return result;
    }

    public static boolean isPrime(int number) {
        if (number < 2) {
            return false;
        }
        if (number == 2) {
            return true;
        }
        if (number % 2 == 0) {
            return false;
        }
        int i = 3;
        while (i * i <= number) {
            if (number % i == 0) {
                return false;
            }
            i += 2;
        }
        return true;
    }

    public static int[] generatePrimes(int until) {
        if (until < 2) {
            return new int[0];
        }
        if (until == 2) {
            return new int[]{2};
        }
        int[] primes = new int[until / 2];
        int count = 1;
        primes[0] = 2;
        int i = 3;
        while (i * i <= until) {
            if (until % i == 0) {
                primes[count++] = i;
            }
            i += 2;
        }
        return Arrays.copyOf(primes, count);
    }

    public static double sqrtn(double num, int n) {
        return Math.round(Math.pow(num, 1.0 / (double)n));
    }

    public static boolean isEven(int number) {
        return (number & 1) == 0;
    }

    public static boolean isOdd(int number) {
        return (number & 1) == 1;
    }
}

