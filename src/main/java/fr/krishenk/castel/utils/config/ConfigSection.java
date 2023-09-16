package fr.krishenk.castel.utils.config;

import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.nodes.*;
import fr.krishenk.castel.utils.compilers.ConditionalCompiler;
import fr.krishenk.castel.utils.compilers.MathCompiler;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.MemorySection;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ConfigSection {
    protected final ScalarNode key;
    protected final MappingNode root;
    private MemorySection t;
    private static final ScalarNode FAKE_ROOT_NODE = new ScalarNode(Tag.STR, "[({<ROOT>})]", ScalarStyle.PLAIN);

    public ConfigSection(ScalarNode key, MappingNode root) {
        this.key = key;
        this.root = Objects.requireNonNull(root);
    }

    public ConfigSection(MappingNode root) {
        this(FAKE_ROOT_NODE, root);
    }

    public static ConfigSection empty() {
        return new ConfigSection(new MappingNode());
    }

    public ConfigSection(NodePair pair) {
        this(pair.getKey(), (MappingNode)pair.getValue());
    }

    @Deprecated
    @NotNull
    public Set<String> getKeys(boolean deep) {
        if (deep) {
            throw new UnsupportedOperationException("Deep keys are not supported");
        }
        return this.getKeys();
    }

    public MappingNode getNode() {
        return this.root;
    }

    @NotNull
    public String getName() {
        return this.key.getValue();
    }

    public ScalarNode getKey() {
        return this.key;
    }

    @NotNull
    public Set<String> getKeys() {
        return this.root.getPairs().keySet();
    }

    public MappingNode getCurrentNode() {
        return this.root;
    }

    @NotNull
    public Map<String, ConfigSection> getSections() {
        LinkedHashMap<String, ConfigSection> result = new LinkedHashMap<String, ConfigSection>();
        for (NodePair pair : this.root.getPairs().values()) {
            if (!(pair.getValue() instanceof MappingNode)) continue;
            result.put(pair.getKey().getValue(), new ConfigSection(pair.getKey(), (MappingNode)pair.getValue()));
        }
        return result;
    }

    @NotNull
    public Map<String, Object> getValues(boolean deep) {
        if (deep) {
            throw new UnsupportedOperationException("Deep keys are not supported");
        }
        LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
        for (NodePair pair : this.root.getPairs().values()) {
            result.put(pair.getKey().getValue(), pair.getValue().getParsed());
        }
        return result;
    }

    public MathCompiler.Expression getMathExpression(String path) {
        return NodeInterpreter.MATH.parse(this.findNode(path));
    }

//    public Long getTime(String path, PlaceholderContextProvider ctx) {
//        return NodeInterpreter.getTime(this.getNode(path), ctx);
//    }

    public ConditionalCompiler.LogicalOperand getCondition(String path) {
        return NodeInterpreter.CONDITION.parse(this.findNode(path));
    }

    public NodePair getPair(String path) {
        return this.root.getPairs().get(path);
    }

    public Node getNode(String path) {
        NodePair pair = this.getPair(path);
        if (pair == null) {
            return null;
        }
        return pair.getValue();
    }

    public boolean isSet(@NotNull String path) {
        return this.root.getPairs().containsKey(path);
    }

    public boolean isSet(String ... path) {
        return this.findNode(path) != null;
    }

    @Nullable
    public Object get(String ... path) {
        Node node = this.findNode(path);
        return node == null ? null : node.getParsed();
    }

    @Nullable
    public String getString(String ... path) {
        return NodeInterpreter.STRING.parse(this.findNode(path));
    }

    @Nullable
    public String getString(@NotNull String path) {
        return NodeInterpreter.STRING.parse(this.getNode(path));
    }

    public int getInt(String ... path) {
        return NodeInterpreter.INT.parse(this.findNode(path));
    }

    public boolean getBoolean(String ... path) {
        return NodeInterpreter.BOOLEAN.parse(this.findNode(path));
    }

    public double getDouble(String ... path) {
        return NodeInterpreter.DOUBLE.parse(this.findNode(path));
    }

    public long getLong(String ... path) {
        return NodeInterpreter.LONG.parse(this.findNode(path));
    }

    public float getFloat(String ... path) {
        return NodeInterpreter.FLOAT.parse(this.findNode(path)).floatValue();
    }

    public <T> T get(NodeInterpreter<T> interpreter, String path) {
        return interpreter.parse(this.getNode(path));
    }

    @NotNull
    public List<String> getStringList(String ... path) {
        return NodeInterpreter.STRING_LIST.parse(this.findNode(path));
    }

    @NotNull
    public List<Integer> getIntegerList(String ... path) {
        return NodeInterpreter.INT_LIST.parse(this.findNode(path));
    }

    public ConfigSection getSection(String ... path) {
        Pair<ConfigSection, NodePair> pair = this.traverseNodePairs(path, false);
        if (pair == null) {
            return null;
        }
        if (pair.getValue().getValue().getNodeType() != NodeType.MAPPING) {
            return null;
        }
        return new ConfigSection(pair.getValue().getKey(), (MappingNode)pair.getValue().getValue());
    }

    public Node findNode(String[] sections) {
        Pair<ConfigSection, NodePair> pair = this.traverseNodePairs(sections, false);
        return pair == null ? null : pair.getValue().getValue();
    }

    public Pair<ConfigSection, NodePair> traverseNodePairs(String[] sections, boolean createIfDoesntExist) {
        if (sections.length == 1) {
            NodePair pair = this.getPair(sections[0]);
            if (pair == null) {
                if (createIfDoesntExist) {
                    return Pair.of(this, this.root.put(sections[0], new MappingNode()));
                }
                return null;
            }
            return Pair.of(this, pair);
        }
        ConfigSection currentSection = this;
        NodePair current = new NodePair(this.key == null ? FAKE_ROOT_NODE : this.key, this.root);
        boolean startCreating = false;
        for (String key : sections) {
            if (startCreating) {
                currentSection = new ConfigSection(null, (MappingNode)current.getValue());
                current = currentSection.root.put(key, new MappingNode());
                continue;
            }
            if (current.getValue().getNodeType() != NodeType.MAPPING) {
                current = null;
            } else {
                currentSection = new ConfigSection(null, (MappingNode)current.getValue());
                current = currentSection.getPair(key);
            }
            if (current != null) continue;
            if (createIfDoesntExist) {
                startCreating = true;
                current = currentSection.root.put(key, new MappingNode());
                continue;
            }
            return null;
        }
        return Pair.of(currentSection, current);
    }

    public NodePair set(String key, Object obj) {
        if (obj == null) {
            this.root.getPairs().remove(key);
            return null;
        }
        NodePair pair = this.getPair(key);
        if (pair == null) {
            pair = this.root.put(key, NodeInterpreter.nodeOfObject(obj));
        } else {
            pair.setValue(NodeInterpreter.nodeOfObject(obj));
        }
        return pair;
    }

    public Pair<ConfigSection, NodePair> set(String[] key, Object obj) {
        Pair<ConfigSection, NodePair> pair = this.traverseNodePairs(key, true);
        pair.getValue().setValue(NodeInterpreter.nodeOfObject(obj));
        return pair;
    }

    public ConfigSection createSection(String ... path) {
        return this.createSection(path, (Object)null).getKey();
    }

    public Pair<ConfigSection, NodePair> createSection(String[] path, Object val) {
        MappingNode lastSection = this.root;
        NodePair lastNodePair = new NodePair(FAKE_ROOT_NODE, this.root);
        int i = 0;
        for (String section : path) {
            if (val != null && ++i == path.length) {
                NodePair pair = new ConfigSection(null, lastSection).set(section, val);
                return Pair.of(new ConfigSection(lastNodePair), pair);
            }
            lastNodePair = lastSection.getPairs().get(section);
            if (lastNodePair == null || lastNodePair.getValue().getNodeType() != NodeType.MAPPING) {
                MappingNode newSection = new MappingNode();
                lastNodePair = lastSection.put(section, newSection);
                lastSection = newSection;
                continue;
            }
            Node nodeVal = lastNodePair.getValue();
            lastSection = (MappingNode)nodeVal;
        }
        return Pair.of(new ConfigSection(null, lastSection), lastNodePair);
    }

    public String toString() {
        return "ConfigSection{ " + this.root + " }";
    }

    public Node findNode(String path) {
        Node current = this.root;
        int seperator = path.indexOf(46);
        if (seperator == -1) {
            return this.getNode(path);
        }
        int lastIndex = 0;
        while (true) {
            if (((Node)current).getNodeType() != NodeType.MAPPING) {
                return null;
            }
            String first = path.substring(lastIndex, seperator);
            if ((current = new ConfigSection(null, (MappingNode)current).getNode(first)) == null) {
                return null;
            }
            if (seperator == path.length()) break;
            lastIndex = seperator + 1;
            if ((seperator = path.indexOf(46, lastIndex)) != -1) continue;
            seperator = path.length();
        }
        return current;
    }

    public @NonNull BukkitConfigSection toBukkitConfigurationSection() {
        return new BukkitConfigSection(this);
    }

    @Nullable
    public ConfigurationSection getConfigurationSection(@NotNull String path) {
        ConfigSection section = this.getSection(path);
        if (section == null) {
            return null;
        }
        return section.toBukkitConfigurationSection();
    }
}

