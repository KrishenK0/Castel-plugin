package fr.krishenk.castel.utils.config;

import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.utils.string.StringUtils;

import java.util.List;
import java.util.Objects;

public class ConfigPath {
    private final String[] paths;
    private final boolean hasVar;

    public ConfigPath(String path) {
        this.paths = StringUtils.splitArray(path, '.');
        this.hasVar = path.contains("{");
    }

    public ConfigPath(String[] paths) {
        this.paths = Objects.requireNonNull(paths);
        this.hasVar = false;
    }

    public ConfigPath(String option, int ... grouped) {
        this.paths = StringUtils.splitArray(StringUtils.getGroupedOption(option, grouped), '.');
        this.hasVar = false;
    }

    public static String[] buildRaw(String path) {
        ConfigPath configPath = new ConfigPath(path);
        if (configPath.hasVar) throw new IllegalStateException("Raw config path cannot contain variables");
        return configPath.paths;
    }

    @Override
    public String toString() {
        return "ConfigPath { "+ String.join(" -> ", this.paths ) + " }";
    }

    public String[] build(List<Pair<String, String>> edits, List<String> property) {
        if (!this.hasVar && property == null) return this.paths;
        String[] paths = new String[this.paths.length + (property == null ? 0 : property.size())];
        block0: for (int i = 0; i < this.paths.length; ++i) {
            String path = this.paths[i];
            if (this.hasVar && path.charAt(0) == '{') {
                String varName = path.substring(1, path.length() - 1);
                for (Pair<String, String> edit : edits) {
                    if (!edit.getKey().equals(varName)) continue;
                    paths[i] = edit.getValue();
                    continue block0;
                }
            throw new IllegalArgumentException("Cannot find replacement variable for '" + varName + "' -> " + edits);
            }
            paths[i] = path;
        }
        if (property != null) {
            int len = property.size();
            for (int i = 0; i < len; ++i) {
                paths[paths.length - len + i] = property.get(i);
            }
        }
        return paths;
    }
}
