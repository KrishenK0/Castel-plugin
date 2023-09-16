package fr.krishenk.castel.locale.compiler.placeholders;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.RomanNumber;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.time.TimeFormatter;
import org.bukkit.configuration.ConfigurationSection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

public class Placeholder {
    public final @NonNull CastelPlaceholder identifier;
    protected final @Nullable String fn;
    public final @Nullable Modifier modifier;
    private final String pointer;
    private final boolean fnAsFormat;
    private final boolean positional;
    protected final @NonNull Map<String, String> parameters;
    private int position;

    protected Placeholder(@NotNull CastelPlaceholder identifier, @Nullable String fn, String pointer, @Nullable Modifier modifier, boolean fnAsFormat, Map<String, String> parameters) {
        this.identifier = identifier;
        this.fn = fn;
        this.modifier = modifier;
        this.fnAsFormat = fnAsFormat;
        this.pointer = pointer;
        this.parameters = parameters == null ? new HashMap<>() : parameters;
        this.positional = this.parameters.containsKey("1");
    }

    public String getPointer() {
        return this.pointer;
    }

    public boolean hasFormat() {
        return this.fnAsFormat;
    }

    public void unknownFunction() {
        CLogger.warn("Unknown function for placeholder " + this);
    }

    private void err(String type, String parameter) {
        CLogger.error("Could not parse " + type + " parameter '" + parameter + "' for placeholder function " + this);
    }

    public void invalidArg(String parameter) {
        CLogger.error("Invalid value for parameter '" + parameter + "' with value '" + this.parameters.get(parameter) + "' for placeholder function " + this);
    }

    String incArg() {
        return Character.toString((char)(48 + ++this.position));
    }

    public void requireArguments(String ... args) {
        if (this.positional && this.parameters.size() < args.length) {
            throw new IllegalArgumentException("Missing required parameter '" + this.parameters.size() + "' for placeholder function " + this);
        }
        for (String arg : args) {
            if (this.parameters.containsKey(arg)) continue;
            throw new IllegalArgumentException("Missing required parameter '" + arg + "' for placeholder function " + this);
        }
    }

    public String toString() {
        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, String> param : this.parameters.entrySet()) {
            params.append(param.getKey()).append('=').append(param.getValue()).append(", ");
        }
        if (params.length() != 0) {
            params.setLength(params.length() - 2);
        }
        return "Placeholder{ " + this.identifier.getName() + ':' + this.fn + " [" + params + "] }";
    }

    public boolean isPointerOther() {
        return "other".equals(this.pointer);
    }

    public Object request(PlaceholderContextBuilder placeholderContextBuilder) {
        boolean isDefault;
        Object translated;
        if (this.isPointerOther()) {
            placeholderContextBuilder = placeholderContextBuilder.clone();
            placeholderContextBuilder.switchContext();
        }
        if ((translated = this.identifier.translate(new CastelPlaceholderTranslationContext(placeholderContextBuilder, this))) == null) {
            isDefault = true;
            translated = this.identifier.getConfiguredDefaultValue();
            if (translated == null) {
                translated = this.identifier.getDefault();
            }
        } else {
            isDefault = false;
        }
        translated = Placeholder.modify(this.modifier, translated);
        if (this.fnAsFormat) {
            ConfigurationSection section = Config.PLACEHOLDERS_FORMATS.getManager().getSection();
            String formatter = this.fn.toLowerCase();
            String format = null;
            if (section != null) {
                ConfigurationSection placeholderSection = section.getConfigurationSection(formatter);
                format = placeholderSection != null ? (isDefault ? placeholderSection.getString("default") : placeholderSection.getString("normal")) : section.getString(formatter);
            }
            if (format == null) {
                CLogger.error("Unknown placeholder format '" + this.fn + "' in " + this);
            } else {
                translated = StringUtils.replaceOnce(format, '%', translated.toString());
            }
        }
        return translated;
    }

    public static Object modify(Modifier modifier, Object translated) {
        if (modifier == null) {
            return translated;
        }
        if (translated instanceof Number) {
            Number number = (Number)translated;
            if (modifier == Modifier.ROMAN) {
                return RomanNumber.toRoman(number.intValue());
            }
            if (modifier == Modifier.TIME) {
                return TimeFormatter.of(number.longValue());
            }
            if (modifier == Modifier.DATE) {
                return TimeFormatter.dateOf(number.longValue());
            }
            if (translated instanceof Double || translated instanceof Float) {
                double doubleValue = number.doubleValue();
                if (modifier == Modifier.SHORT) {
                    return MathUtils.getShortNumber(doubleValue);
                }
                return StringUtils.toFancyNumber(doubleValue);
            }
            double longValue = number.longValue();
            if (modifier == Modifier.SHORT) {
                return MathUtils.getShortNumber(longValue);
            }
            return StringUtils.toFancyNumber(longValue);
        }
        if (translated instanceof Boolean) {
            boolean enabled = (Boolean)translated;
            return (enabled ? CastelLang.ENABLED : CastelLang.DISABLED).parse(new Object[0]);
        }
        return translated;
    }

    public String getString(String parameter) {
        if (this.positional) {
            parameter = this.incArg();
        }
        return this.parameters.get(parameter);
    }

    public boolean getBool(String parameter) {
        switch (StringUtils.toLatinLowerCase(StringUtils.deleteWhitespace(this.positional ? this.incArg() : parameter))) {
            case "true": {
                return true;
            }
            case "false": {
                return false;
            }
        }
        this.err("boolean", parameter);
        return false;
    }

    public boolean fnIs(String fn) {
        return this.positional || this.fn != null && this.fn.equalsIgnoreCase(fn);
    }

    public int getNumber(String parameter) {
        try {
            String value = this.parameters.get(this.positional ? this.incArg() : parameter);
            if (value == null) {
                return 0;
            }
            return Integer.parseInt(StringUtils.deleteWhitespace(value));
        }
        catch (NumberFormatException ex) {
            this.err("number", parameter);
            ex.printStackTrace();
            return 0;
        }
    }

    public enum Modifier {
        SHORT,
        FANCY,
        ROMAN,
        BOOL,
        TIME,
        DATE;
        public final int skip = this.name().length() + 1;
        public final String constName = this.name() + '_';

        public static final class Data {
            public static final int MIN_LENGTH = Arrays.stream(values()).min(Comparator.comparingInt(a -> a.name().length())).get().name().length();
        }
    }
}


