package fr.krishenk.castel.config.implementation;

import fr.krishenk.castel.config.NewEnumConfig;
import fr.krishenk.castel.config.NewKeyedConfigAccessor;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.compilers.PlaceholderContextProvider;
import fr.krishenk.castel.utils.config.ConfigPath;
import fr.krishenk.castel.utils.config.ConfigPathBuilder;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class NewKeyedYamlConfigAccessor extends ConfigPathBuilder implements NewKeyedConfigAccessor, NewEnumConfig {
    private final FileConfiguration adapter;
    private boolean noDefault;

    public NewKeyedYamlConfigAccessor(FileConfiguration adapter, ConfigPath option) {
        super(Objects.requireNonNull(option, "option for keyed config accessor cannot be null"));
        this.adapter = Objects.requireNonNull(adapter, "Yaml Adapter cannot be null");
    }

    public boolean isInDisabledWorld(Entity entity) {
        String world = entity.getWorld().getName();
        return this.getStringList().contains(world);
    }

    @Override
    public NewKeyedYamlConfigAccessor withProperty(String prop) {
        super.withProperty(prop);
        return this;
    }

    @Override
    public NewKeyedYamlConfigAccessor clearExtras() {
        super.clearExtras();
        return this;
    }

    public NewKeyedYamlConfigAccessor noDefault() {
        this.noDefault = true;
        return this;
    }

    @Override
    public NewKeyedYamlConfigAccessor withOption(String first, String second) {
        this.replace(first, second);
        return this;
    }

    @Override
    public NewKeyedConfigAccessor applyProperties() {
        return new NewKeyedYamlConfigAccessor(this.adapter, new ConfigPath(this.build()));
    }

    @Override
    public String getDynamicOption() {
        return String.join(".", this.build());
    }

    @Override
    public String getKey() {
        return String.join(".", this.build());
    }

    @Override
    public boolean isSet() {
        return this.adapter.isSet(getKey());
    }

    @Override
    public String getString() {
        return this.adapter.getString(this.getKey());
    }

    @Override
    public MathCompiler.Expression getMathExpression() {
        String it = this.getKey();
        if (it == null) {
            return MathCompiler.DEFAULT_VALUE;
        }
        return MathCompiler.Companion.compile(this.adapter.getString(it));
    }

    @Override
    public Object get() {
        return this.adapter.get(this.getKey());
    }

    public ConditionalCompiler.LogicalOperand getCondition() {
        String it = this.getKey();
        if (it == null) return null;
        return ConditionalCompiler.compile(it).evaluate();
    }

    @Override
    public List<String> getStringList() {
        return this.adapter.getStringList(this.getKey());
    }

    @Override
    public Set<String> getSectionKeys() {
        return this.adapter.getKeys(true);
    }

    @Override
    public boolean getBoolean() {
        return this.adapter.getBoolean(this.getKey());
    }

    @Override
    public List<Integer> getIntegerList() {
        return this.adapter.getIntegerList(this.getKey());
    }

    @Override
    public int getInt() {
        return this.adapter.getInt(this.getKey());
    }

    @Override
    public double getDouble() {
        return this.adapter.getDouble(this.getKey());
    }

    @Override
    public long getLong() {
        return this.adapter.getLong(this.getKey());
    }

    @Override
    public ConfigurationSection getSection() {
        return this.adapter.getConfigurationSection(this.getKey());
    }

    @Override
    public Long getTime(PlaceholderContextProvider ctx) {
        Object value = this.adapter.get(this.getKey());
        if (value == null) return null;
        Object cache;
        if (value instanceof String) {
            try {
                cache = MathCompiler.Companion.compile((String) value);
            } catch (Throwable ignored) {
                cache = TimeUtils.parseTime((String) value);
            }
            if (cache == null) return null;
            this.adapter.set(this.getKey(), cache);
        } else cache = value;

        return cache instanceof Number ? Long.valueOf(((Number) cache).longValue()) : Long.valueOf((long) MathUtils.eval((MathCompiler.Expression) cache, ctx));
    }

    @Override
    public NewKeyedConfigAccessor getManager() {
        return this;
    }
}

