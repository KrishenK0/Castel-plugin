package fr.krishenk.castel.config.implementation;

import fr.krishenk.castel.config.ConfigAccessor;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.config.ConfigSection;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class YamlConfigAccessor implements ConfigAccessor {
    private final ConfigSection config;
    private final ConfigSection defaults;
    private boolean noDefault;

    public YamlConfigAccessor(ConfigSection config, ConfigSection defaults) {
        this.config = Objects.requireNonNull(config, "Configuration section cannot be null");
        this.defaults = Objects.requireNonNull(defaults, "Configuration section defaults cannot be null");
    }

    @Override
    public YamlConfigAccessor noDefault() {
        this.noDefault = true;
        return this;
    }

    @Override
    public boolean isSet(String ... option) {
        return this.config.isSet(option);
    }

    @Override
    public String getString(String ... option) {
        if (this.noDefault || this.isSet(option)) {
            return this.config.getString(option);
        }
        return this.defaults.getString(option);
    }

    @Override
    public List<String> getStringList(String ... option) {
        if (this.noDefault || this.isSet(option)) {
            return this.config.getStringList(option);
        }
        return this.defaults.getStringList(option);
    }

    @Override
    public Set<String> getKeys() {
        return this.config.getKeys();
    }

    @Override
    public boolean getBoolean(String ... option) {
        if (this.noDefault || this.isSet(option)) {
            return this.config.getBoolean(option);
        }
        return this.defaults.getBoolean(option);
    }

    public ConditionalCompiler.LogicalOperand getCondition(String option) {
        block3: {
            block2: {
                if (this.noDefault) break block2;
                if (!this.isSet(option)) break block3;
            }
            return this.config.getCondition(option);
        }
        return this.defaults.getCondition(option);
    }

    @Override
    public int getInt(String ... option) {
        if (this.noDefault || this.isSet(option)) {
            return this.config.getInt(option);
        }
        return this.defaults.getInt(option);
    }

    @Override
    public Object get(String ... option) {
        if (this.noDefault || this.isSet(option)) {
            return this.config.get(option);
        }
        return this.defaults.get(option);
    }

    @Override
    public double getDouble(String ... option) {
        if (this.noDefault || this.isSet(option)) {
            return this.config.getDouble(option);
        }
        return this.defaults.getDouble(option);
    }

    @Override
    public long getLong(String ... option) {
        if (this.noDefault || this.isSet(option)) {
            return this.config.getLong(option);
        }
        return this.defaults.getLong(option);
    }

    @Override
    public ConfigSection getSection() {
        return this.config;
    }

    @Override
    public List<Integer> getIntegerList(String ... option) {
        if (this.noDefault || this.isSet(option)) {
            return this.config.getIntegerList(option);
        }
        return this.defaults.getIntegerList(option);
    }

    @Override
    public Map<String, Object> getEntries() {
        return this.config.getValues(false);
    }

    @Override
    public YamlConfigAccessor gotoSection(String ... option) {
        ConfigSection val = this.config.getSection(option);
        ConfigSection defaultsSection = this.defaults.getSection(option);
        if (val != null) {
            YamlConfigAccessor newSection = new YamlConfigAccessor(val, this.noDefault ? val : defaultsSection);
            if (this.noDefault) {
                newSection.noDefault();
            }
            return newSection;
        }
        if (this.noDefault || defaultsSection == null) {
            return null;
        }
        YamlConfigAccessor newSection = new YamlConfigAccessor(defaultsSection, defaultsSection);
        if (this.noDefault) {
            newSection.noDefault();
        }
        return newSection;
    }

    @Override
    public String getCurrentPath() {
        return this.config.getName();
    }
}

