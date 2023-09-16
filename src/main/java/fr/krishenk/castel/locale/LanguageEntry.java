package fr.krishenk.castel.locale;

import fr.krishenk.castel.utils.string.StringUtils;

import java.util.Arrays;
import java.util.Objects;
import java.util.regex.Pattern;

public class LanguageEntry {
    private static final Pattern ACCEPTED_PATTERN = Pattern.compile("[A-Za-z0-9-]+((\\.[A-Za-z0-9-]+)+)?");
    private final String[] path;

    public LanguageEntry(String[] path) {
        this.path = Objects.requireNonNull(path);
    }

    public String[] getPath() {
        return this.path;
    }

    public final int hashCode() {
        int result = 1;
        for (String element : this.path) {
            result = 31 * result + element.hashCode();
        }
        return result;
    }

    public static LanguageEntry fromConfig(String str) {
        return new LanguageEntry(StringUtils.splitArray(str, '.'));
    }

    public static boolean isValidConfigLanguageEntry(String str) {
        return ACCEPTED_PATTERN.matcher(str).matches();
    }

    public final boolean equals(Object obj) {
        LanguageEntry other = (LanguageEntry)obj;
        if (other.path.length != this.path.length) {
            return false;
        }
        for (int i = 0; i < this.path.length; ++i) {
            if (this.path[i].equals(other.path[i])) continue;
            return false;
        }
        return true;
    }

    public String toString() {
        return "LanguageEntry{" + Arrays.toString(this.path) + '}';
    }
}
