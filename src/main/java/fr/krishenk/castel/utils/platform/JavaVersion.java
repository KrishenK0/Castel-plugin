package fr.krishenk.castel.utils.platform;

import java.util.Locale;

public final class JavaVersion {
    private static final int VERSION = JavaVersion.getVersion0();

    private static int getVersion0() {
        String ver = System.getProperty("java.version");
        try {
            String parse = ver.toLowerCase(Locale.ENGLISH);
            if (parse.startsWith("1.8")) {
                return 8;
            }
            if (parse.endsWith("-ea")) {
                parse = parse.substring(0, parse.length() - "-ea".length());
            }
            if (!parse.contains(".")) {
                return Integer.parseInt(ver);
            }
            return Integer.parseInt(parse.split("\\.")[0]);
        }
        catch (Throwable ex) {
            throw new IllegalStateException("Unknown Java version: '" + ver + '\'');
        }
    }

    public static boolean supports(int version) {
        return VERSION >= version;
    }

    public static int getVersion() {
        return VERSION;
    }
}

