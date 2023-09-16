package fr.krishenk.castel.locale.compiler.placeholders;

import fr.krishenk.castel.locale.MessageObjectBuilder;
import fr.krishenk.castel.locale.compiler.PlaceholderTranslationContext;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.utils.string.StringUtils;
import org.apache.commons.lang.Validate;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PlaceholderParser {
    public static Placeholder parsePlaceholder(String placeholder) {
        String fn;
        String identifier;
        Validate.notEmpty(placeholder, "Cannot parse null or empty placeholder");
        HashMap<String, String> parameters = new HashMap<String, String>();
        Placeholder.Modifier modifier = PlaceholderParser.getModifierOf(StringUtils.toLatinUpperCase(placeholder), false);
        if (modifier != null) {
            placeholder = placeholder.substring(modifier.skip);
        }
        int fnbegin = placeholder.indexOf(58);
        boolean fnAsFormat = false;
        if (fnbegin > 1) {
            identifier = placeholder.substring(0, fnbegin);
            int fnEnd = placeholder.indexOf(32, fnbegin + 1);
            if (fnEnd < 0) {
                fnEnd = placeholder.length();
            }
            fn = placeholder.substring(fnbegin + 1, fnEnd);
            int lastParam = fnEnd + 1;
            while (lastParam < placeholder.length()) {
                int paramIndex = placeholder.indexOf(44, lastParam);
                if (paramIndex < 0) {
                    paramIndex = placeholder.length();
                }
                String param = placeholder.substring(lastParam, paramIndex);
                int sep = param.indexOf(61);
                String namedParam = StringUtils.deleteWhitespace(param.substring(0, sep));
                String namedValue = param.substring(sep + 1);
                parameters.put(namedParam.toLowerCase(Locale.ENGLISH), namedValue);
                lastParam = paramIndex + 1;
            }
        } else {
            fnbegin = placeholder.lastIndexOf(64);
            if (fnbegin != -1) {
                identifier = placeholder.substring(0, fnbegin);
                fn = placeholder.substring(fnbegin + 1);
                fnAsFormat = true;
            } else {
                fn = null;
                fnbegin = placeholder.indexOf(32);
                if (fnbegin == -1) {
                    identifier = placeholder;
                } else {
                    identifier = placeholder.substring(0, fnbegin);
                    List<String> params = StringUtils.split(placeholder.substring(fnbegin + 1), ' ', true);
                    for (int i = 1; i <= params.size(); ++i) {
                        parameters.put(Character.toString((char)(48 + i)), params.get(i - 1));
                    }
                }
            }
        }
        String pointer = null;
        if (identifier.startsWith("other_")) {
            identifier = identifier.substring("other_".length());
            pointer = "other";
        } else {
            int pointerIndex = identifier.indexOf(42);
            if (pointerIndex > 1) {
                identifier = identifier.substring(pointerIndex + 1);
                pointer = identifier.substring(0, pointerIndex);
            }
        }
        CastelPlaceholder receiver = CastelPlaceholder.getByName(identifier);
        if (receiver == null) {
            return null;
        }
        return new Placeholder(receiver, fn, pointer, modifier, fnAsFormat, parameters);
    }

    public static PlaceholderType parseType(String full) {
        int sep = full.indexOf(95);
        if (sep == -1) {
            return new PlaceholderType.Local(null, full);
        }
        String id = full.substring(0, sep);
        String parameter = full.substring(sep + 1);
        return PlaceholderParser.parseType(false, id, parameter);
    }

    public static PlaceholderType parseType(boolean isLocal, String id, String parameter) {
        int nextSep;
        if (isLocal || parameter.isEmpty()) {
            return new PlaceholderType.Local(null, id);
        }
        boolean relational = id.equals("rel");
        if (relational) {
            int nextSep2 = parameter.indexOf(95);
            if (nextSep2 == -1) {
                return new PlaceholderType.Local(null, id + '_' + parameter);
            }
            id = parameter.substring(0, nextSep2);
            parameter = parameter.substring(nextSep2 + 1);
        }
        if (id.equals("guilds")) {
            Placeholder parsed = PlaceholderParser.parsePlaceholder(parameter);
            if (parsed == null) {
                throw new IllegalArgumentException("Unknown internal placeholder " + parameter);
            }
            return new PlaceholderType.Internal(relational, parsed);
        }
        Placeholder.Modifier modifier = PlaceholderParser.getModifierOf(StringUtils.toLatinUpperCase(id), true);
        if (modifier != null) {
            nextSep = parameter.indexOf(95);
            if (nextSep == -1) {
                return new PlaceholderType.Local(modifier, parameter);
            }
            id = parameter.substring(0, nextSep);
            parameter = parameter.substring(nextSep + 1);
        }
        if (id.equals("perm")) {
            nextSep = parameter.indexOf(95);
            if (nextSep == -1) {
                return new PlaceholderType.Local(modifier, id + '_' + parameter);
            }
            return new PlaceholderType.Permission(modifier, StringUtils.replace(parameter, '_', '.').toString());
        }
        if (modifier != null) {
            return new PlaceholderType.Local(modifier, id + '_' + parameter);
        }
        return new PlaceholderType.ExternalOrLocal(relational, id, parameter);
    }

    public static Placeholder.Modifier getModifierOf(String identifier, boolean parsed) {
        if (!parsed) {
            int min = Placeholder.Modifier.Data.MIN_LENGTH + 3;
            if (identifier.length() <= min) {
                return null;
            }
        }
        for (Placeholder.Modifier modifier : Placeholder.Modifier.values()) {
            if (!(parsed ? identifier.equals(modifier.name()) : identifier.startsWith(modifier.constName))) continue;
            return modifier;
        }
        return null;
    }

    public static Map<String, Object> serializeVariables(Object ... edits) {
        return PlaceholderParser.serializeVariables(new HashMap<>(edits.length / 2), edits);
    }

    public static Map<String, Object> serializeVariables(Map<String, Object> source, Object ... edits) {
        if (edits.length == 0) {
            return source;
        }
        int len = edits.length - 1;
        for (int i = 0; i < len; i += 2) {
            Object replacement = edits[i + 1];
            if (replacement == null) continue;
            source.put(String.valueOf(edits[i]), replacement);
        }
        return source;
    }

    public static Map<String, Object> serializeVariablesIntoContext(Object ... edits) {
        return PlaceholderParser.serializeVariablesIntoContext(new HashMap<String, Object>(edits.length / 2), edits);
    }

    public static Map<String, Object> serializeVariablesIntoContext(Map<String, Object> replacers, Object ... edits) {
        if (edits.length == 0) {
            return replacers;
        }
        int len = edits.length - 1;
        for (int i = 0; i < len; i += 2) {
            Object replacement = edits[i + 1];
            if (replacement == null) continue;
            if (!(replacement instanceof Messenger) && !(replacement instanceof MessageObjectBuilder)) {
                replacement = PlaceholderTranslationContext.withDefaultContext(replacement);
            }
            replacers.put(String.valueOf(edits[i]), replacement);
        }
        return replacers;
    }

    private static Object[] legacyVariables(Object ... edits) {
        int len = edits.length - 1;
        for (int i = 0; i < len; i += 2) {
            if (edits[i].toString().charAt(0) == '%') continue;
            edits[i] = '%' + String.valueOf(edits[i]) + '%';
        }
        return edits;
    }
}


