
package fr.krishenk.castel.libs.snakeyaml.nodes;

public class AnchorNode
extends Node {
    private final Node realNode;

    public AnchorNode(Node realNode) {
        super(realNode.getTag(), realNode.getStartMark(), realNode.getEndMark());
        this.realNode = realNode;
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.ANCHOR;
    }

    @Override
    public void cacheConstructed(Object obj) {
    }

    @Override
    public Object getParsed() {
        return null;
    }

    @Override
    public Node clone() {
        return new AnchorNode(this.realNode).copyPropertiesOf(this);
    }

    public Node getRealNode() {
        return this.realNode;
    }
}

