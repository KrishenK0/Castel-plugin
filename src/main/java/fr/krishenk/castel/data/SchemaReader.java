package fr.krishenk.castel.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SchemaReader {
    private static final Pattern MACRO_USAGE = Pattern.compile("\\[\\[(\\w+)\\]\\]");

    private SchemaReader() {}

    public static List<String> getStatements(InputStream is) throws IOException {
        Objects.requireNonNull(is, "Cannot get statements from null stream");
        ArrayList<String> queries = new ArrayList<>(100);
        HashMap<String, String> macros = new HashMap<>(3);
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));){
            String line;
            StringBuilder sb = new StringBuilder();
            String parsingMacro = null;
            while ((line = reader.readLine()) != null) {
                if ((line = line.trim()).isEmpty() || line.startsWith("--") || line.startsWith("#")) continue;
                if (line.startsWith("{{")) {
                    if (parsingMacro != null) {
                        throw new IllegalStateException("Unexpected start of macro: " + line);
                    }
                    parsingMacro = line.substring(2).trim();
                    continue;
                }
                if (line.startsWith("}}")) {
                    if (parsingMacro == null) {
                        throw new IllegalStateException("Unexpected end of macro: " + line);
                    }
                    macros.put(parsingMacro, sb.toString());
                    sb.setLength(0);
                    continue;
                }
                int commentStart = line.indexOf("--");
                if (commentStart > 0) {
                    line = line.substring(0, commentStart);
                }
                Matcher macroMatcher = MACRO_USAGE.matcher(line);
                while (macroMatcher.find()) {
                    String macroName = macroMatcher.group(1);
                    String macro = macros.get(macroName);
                    if (macro == null) {
                        throw new IllegalStateException("Unknown macro '" + macroName + "': " + line);
                    }
                    line = macroMatcher.replaceAll(macro);
                }
                sb.append(line);
                if (!line.endsWith(";")) continue;
                sb.deleteCharAt(sb.length() - 1);
                queries.add(sb.toString());
                sb.setLength(0);
            }
        }
        return queries;
    }
}
