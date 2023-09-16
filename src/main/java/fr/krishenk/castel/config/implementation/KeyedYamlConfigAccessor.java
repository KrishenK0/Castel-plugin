package fr.krishenk.castel.config.implementation;

import fr.krishenk.castel.config.EnumConfig;
import fr.krishenk.castel.config.KeyedConfigAccessor;
import fr.krishenk.castel.libs.snakeyaml.nodes.Node;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import fr.krishenk.castel.utils.config.ConfigPath;
import fr.krishenk.castel.utils.config.ConfigPathBuilder;
import fr.krishenk.castel.utils.config.ConfigSection;
import fr.krishenk.castel.utils.config.NodeInterpreter;
import fr.krishenk.castel.utils.config.adapters.YamlResource;
import org.bukkit.entity.Entity;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class KeyedYamlConfigAccessor extends ConfigPathBuilder implements KeyedConfigAccessor, EnumConfig {
    private final YamlResource adapter;
    private boolean noDefault;

    public KeyedYamlConfigAccessor(YamlResource adapter, ConfigPath option) {
        super(Objects.requireNonNull(option, "option for keyed config accessor cannot be null"));
        this.adapter = Objects.requireNonNull(adapter, "Yaml Adapter cannot be null");
    }

    public boolean isInDisabledWorld(Entity entity) {
        String world = entity.getWorld().getName();
        return this.getStringList().contains(world);
    }

    @Override
    public KeyedYamlConfigAccessor withProperty(String prop) {
        super.withProperty(prop);
        return this;
    }

    @Override
    public KeyedYamlConfigAccessor clearExtras() {
        super.clearExtras();
        return this;
    }

    public KeyedYamlConfigAccessor noDefault() {
        this.noDefault = true;
        return this;
    }

    @Override
    public KeyedYamlConfigAccessor withOption(String first, String second) {
        this.replace(first, second);
        return this;
    }

    @Override
    public KeyedConfigAccessor applyProperties() {
        return new KeyedYamlConfigAccessor(this.adapter, new ConfigPath(this.build()));
    }

    @Override
    public String getDynamicOption() {
        return String.join(".", this.build());
    }

    @Override
    public Node getNode() {
        String[] path = this.build();
        Node node = this.adapter.getConfig().findNode(path);
        if (node != null) {
            return node;
        }
        if (this.noDefault || this.adapter.getDefaults() == null) {
            return null;
        }
        return this.adapter.getDefaults().findNode(path);
    }

    @Override
    public boolean isSet() {
        return this.getNode() != null;
    }

    @Override
    public String getString() {
        return NodeInterpreter.STRING.parse(this.getNode());
    }

    @Override
    public MathCompiler.Expression getMathExpression() {
        return NodeInterpreter.MATH.parse(this.getNode());
    }

    @Override
    public <T> T get(NodeInterpreter<T> nodeInterpreter) {
        return nodeInterpreter.parse(this.getNode());
    }

    public ConditionalCompiler.LogicalOperand getCondition() {
        return NodeInterpreter.CONDITION.parse(this.getNode());
    }

    @Override
    public List<String> getStringList() {
        return NodeInterpreter.STRING_LIST.parse(this.getNode());
    }

    @Override
    public Set<String> getSectionKeys() {
        YamlConfigAccessor section = this.getSection();
        return section == null ? new HashSet() : section.getKeys();
    }

    @Override
    public boolean getBoolean() {
        return NodeInterpreter.BOOLEAN.parse(this.getNode());
    }

    @Override
    public List<Integer> getIntegerList() {
        return NodeInterpreter.INT_LIST.parse(this.getNode());
    }

    @Override
    public int getInt() {
        return NodeInterpreter.INT.parse(this.getNode());
    }

    @Override
    public double getDouble() {
        return NodeInterpreter.DOUBLE.parse(this.getNode());
    }

    @Override
    public long getLong() {
        return NodeInterpreter.LONG.parse(this.getNode());
    }

    @Override
    public YamlConfigAccessor getSection() {
        ConfigSection val = this.adapter.getConfig().getSection(this.build());
        ConfigSection defaultsSection = this.adapter.getDefaults() == null ? null : this.adapter.getDefaults().getSection(this.build());
        if (val != null) {
            return new YamlConfigAccessor(val, this.noDefault || defaultsSection == null ? val : defaultsSection);
        }
        if (this.noDefault || defaultsSection == null) {
            return null;
        }
        YamlConfigAccessor accessor = new YamlConfigAccessor(defaultsSection, defaultsSection);
        if (this.noDefault) {
            accessor.noDefault();
        }
        return accessor;
    }

    @Override
    public KeyedConfigAccessor getManager() {
        return this;
    }
}

