
package fr.krishenk.castel.libs.snakeyaml.nodes;

import fr.krishenk.castel.libs.snakeyaml.common.Anchor;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

public class AliasNode
extends Node {
    private final Anchor anchor;
    private final SequenceNode parameters;
    private Object cached;

    public AliasNode(Anchor anchor, SequenceNode parameters, Mark startMark, Mark endMark) {
        super(Tag.ALIAS, startMark, endMark);
        this.anchor = anchor;
        this.parameters = parameters;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ALIAS;
    }

    @Override
    public void cacheConstructed(Object obj) {
        this.cached = obj;
    }

    @Override
    public Object getParsed() {
        return this.cached;
    }

    @Override
    public AliasNode clone() {
        AliasNode scalarNode = new AliasNode(this.anchor, this.parameters, this.getStartMark(), this.getEndMark());
        scalarNode.copyPropertiesOf(this);
        return scalarNode;
    }

    public String toString() {
        return '<' + this.getClass().getName() + " (tag=" + this.getTag() + ", anchor=" + this.anchor.getIdentifier() + ", parameters=" + this.parameters + ")>";
    }
}

