
package fr.krishenk.castel.libs.snakeyaml.serializer;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.nodes.Node;

public interface AnchorGenerator {
    public Anchor nextAnchor(Node var1);
}

