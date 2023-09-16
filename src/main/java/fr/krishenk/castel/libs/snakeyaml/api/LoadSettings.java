
package fr.krishenk.castel.libs.snakeyaml.api;

import fr.krishenk.castel.libs.snakeyaml.nodes.Tag;
import fr.krishenk.castel.libs.snakeyaml.resolver.ScalarResolver;
import fr.krishenk.castel.libs.snakeyaml.resolver.StandardScalarResolver;

import java.util.*;
import java.util.function.IntFunction;

public final class LoadSettings {
    private String label;
    private final Map<Tag, ConstructNode> tagConstructors;
    private final ScalarResolver scalarResolver;
    private final IntFunction<List> defaultList;
    private final IntFunction<Map> defaultMap;
    private final int bufferSize;
    private final int maxAliasesForCollections;

    public LoadSettings(String label, Map<Tag, ConstructNode> tagConstructors, ScalarResolver scalarResolver, IntFunction<List> defaultList, IntFunction<Map> defaultMap, int bufferSize, int maxAliasesForCollections) {
        this.label = label;
        this.tagConstructors = tagConstructors;
        this.scalarResolver = scalarResolver;
        this.defaultList = defaultList;
        this.defaultMap = defaultMap;
        this.bufferSize = bufferSize;
        this.maxAliasesForCollections = maxAliasesForCollections;
    }

    public LoadSettings() {
        this.label = "reader";
        this.tagConstructors = new HashMap<>();
        this.scalarResolver = new StandardScalarResolver();
        this.defaultList = ArrayList::new;
        this.defaultMap = LinkedHashMap::new;
        this.bufferSize = 1024;
        this.maxAliasesForCollections = 50;
    }

    public String getLabel() {
        return this.label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Map<Tag, ConstructNode> getTagConstructors() {
        return this.tagConstructors;
    }

    public ScalarResolver getScalarResolver() {
        return this.scalarResolver;
    }

    public IntFunction<List> getDefaultList() {
        return this.defaultList;
    }

    public IntFunction<Map> getDefaultMap() {
        return this.defaultMap;
    }

    public int getBufferSize() {
        return this.bufferSize;
    }

    public int getMaxAliasesForCollections() {
        return this.maxAliasesForCollections;
    }
}

