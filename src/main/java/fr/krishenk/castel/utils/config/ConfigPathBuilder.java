package fr.krishenk.castel.utils.config;

import fr.krishenk.castel.data.Pair;
import org.apache.commons.lang.Validate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigPathBuilder {
    private final ConfigPath path;
    private List<Pair<String, String>> replacements;
    private List<String> property;

    public ConfigPathBuilder(ConfigPath path) {
        this.path = Objects.requireNonNull(path);
    }

    public ConfigPathBuilder withProperty(String prop) {
        Objects.requireNonNull(prop);
        if (this.property == null) {
            this.property = new ArrayList<>(2);
        }
        this.property.add(prop);
        return this;
    }

    public ConfigPathBuilder clearExtras() {
        this.replacements = null;
        this.property = null;
        return this;
    }

    public ConfigPathBuilder replace(String variable, String replacement) {
        Validate.notEmpty(variable, "Variable cannot be null or empty");
        Validate.notEmpty(replacement, "Replacement cannot be null or empty");
        if (this.replacements == null) {
            this.replacements = new ArrayList<>(2);
        }
        this.replacements.add(Pair.of(variable, replacement));
        return this;
    }

    public String[] build() {
        return this.path.build(this.replacements, this.property);
    }
}


