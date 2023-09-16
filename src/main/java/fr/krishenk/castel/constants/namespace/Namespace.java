package fr.krishenk.castel.constants.namespace;

import fr.krishenk.castel.utils.string.StringUtils;
import lombok.NonNull;
import org.intellij.lang.annotations.Pattern;

public class Namespace {
    private final @NonNull String namespace;
    private final @NonNull String key;
    private static final String ACCEPTED_KEYS = "[A-Z0-9_]{3,100}";
    private static final String ACCEPTED_NAMESPACES = "[A-Za-z]{3,20}";
    private static final java.util.regex.Pattern ACCEPTED_KEYS_PATTERN = java.util.regex.Pattern.compile("[A-Z0-9_]{3,100}");
    private static final java.util.regex.Pattern ACCEPTED_NAMESPACES_PATTERN = java.util.regex.Pattern.compile("[A-Za-z]{3,20}");
    public static final String CASTEL = "castel";
    private static final char SEPARATOR = ':';

    public Namespace(@Pattern(value="[A-Za-z]{3,20}") @NonNull String namespace, @Pattern(value="[A-Z0-9_]{3,100}") @NonNull String key) {
        if (namespace == null || !ACCEPTED_NAMESPACES_PATTERN.matcher(namespace).matches()) {
            throw new IllegalStateException("Namespace string '" + namespace + "' doesn't match: " + ACCEPTED_NAMESPACES);
        }
        if (key == null || !ACCEPTED_KEYS_PATTERN.matcher(key).matches()) {
            throw new IllegalStateException("Key string '" + key + "' doesn't match: " + ACCEPTED_KEYS);
        }
        this.namespace = namespace;
        this.key = key;
    }

    public String getConfigOptionName() {
        String keyConfig = StringUtils.configOption(this.key);
        if (this.namespace.equals(CASTEL)) {
            return keyConfig;
        }
        return this.namespace + ':' + keyConfig;
    }

    public final String asString() {
        return this.namespace + ':' + this.key;
    }

    public final String asNormalizedString() {
        if (this.namespace.equals(CASTEL)) {
            return this.key;
        }
        return this.asString();
    }

    public static Namespace castel(@Pattern(value="[A-Z0-9_]{3,100}") String key) {
        return new Namespace(CASTEL, key);
    }

    public final @NonNull String getNamespace() {
        return this.namespace;
    }

    public final @NonNull String getKey() {
        return this.key;
    }

    public static @NonNull Namespace fromString(String str) {
        int separator = str.indexOf(58);
        if (separator == -1) {
            return Namespace.castel(str);
        }
        String namespace = str.substring(0, separator);
        String key = str.substring(separator + 1);
        return new Namespace(namespace, key);
    }

    public static Namespace fromConfigString(String str) {
        int separator = str.indexOf(58);
        if (separator == -1) {
            return Namespace.castel(StringUtils.configOptionToEnum(str));
        }
        String namespace = str.substring(0, separator);
        String key = str.substring(separator + 1);
        return new Namespace(namespace, StringUtils.configOptionToEnum(key));
    }

    public String toString() {
        return this.getClass().getSimpleName() + '[' + this.asString() + ']';
    }

    public final boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Namespace)) {
            return false;
        }
        Namespace other = (Namespace)obj;
        return this.namespace.equals(other.namespace) && this.key.equals(other.key);
    }

    public final int hashCode() {
        int hash = 5;
        hash = 47 * hash + this.namespace.hashCode();
        hash = 47 * hash + this.key.hashCode();
        return hash;
    }
}
