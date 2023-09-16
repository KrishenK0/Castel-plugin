
package fr.krishenk.castel.libs.snakeyaml.resolver;

import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;

public class ResolverContext {
    private final String value;
    private final ScalarStyle scalarStyle;

    public ResolverContext(String value, ScalarStyle scalarStyle) {
        this.value = value;
        this.scalarStyle = scalarStyle;
    }

    public ScalarStyle getScalarStyle() {
        return this.scalarStyle;
    }

    public String getValue() {
        return this.value;
    }
}

