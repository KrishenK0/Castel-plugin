package fr.krishenk.castel.utils.string;

import java.util.Objects;
import java.util.regex.Pattern;

public class StringCheckerOptions {
    protected final String text;
    protected final StringCheckerMode mode;
    private final Pattern compiledRegexPattern;

    public StringCheckerMode getMode() {
        return this.mode;
    }

    public StringCheckerOptions(String text) {
        Objects.requireNonNull(text, "Cannot construct checker from null text");
        if (text.startsWith("CONTAINS:")) {
            this.mode = StringCheckerMode.CONTAINS;
            this.text = text.substring("CONTAINS:".length());
            this.compiledRegexPattern = null;
        } else if (text.startsWith("REGEX:")) {
            this.mode = StringCheckerMode.REGEX;
            this.text = text.substring("REGEX:".length());
            this.compiledRegexPattern = Pattern.compile(this.text);
        } else {
            this.mode = StringCheckerMode.NORMAL;
            this.text = text;
            this.compiledRegexPattern = null;
        }
    }

    public boolean check(String text) {
        Objects.requireNonNull(text, "Cannot check null text");
        switch (this.mode) {
            case NORMAL: {
                return this.text.equalsIgnoreCase(text);
            }
            case CONTAINS: {
                return text.contains(this.text);
            }
            case REGEX: {
                return this.compiledRegexPattern.matcher(text).matches();
            }
        }
        throw new AssertionError((Object)("Unknown mode: " + (Object)((Object)this.mode)));
    }

    public enum StringCheckerMode {
        NORMAL,
        CONTAINS,
        REGEX;
    }
}


