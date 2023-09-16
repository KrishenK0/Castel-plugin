
package fr.krishenk.castel.libs.snakeyaml.resolver;

import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.nodes.Tag;

public interface ScalarResolver {
    public Tag resolve(ResolverContext var1);

    default public Tag resolve(String value) {
        return this.resolve(new ResolverContext(value, ScalarStyle.PLAIN));
    }
}

