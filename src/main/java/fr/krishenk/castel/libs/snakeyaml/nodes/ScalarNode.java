package fr.krishenk.castel.libs.snakeyaml.nodes;

import fr.krishenk.castel.libs.snakeyaml.common.ScalarStyle;
import fr.krishenk.castel.libs.snakeyaml.exceptions.Mark;

import java.util.Objects;

public class ScalarNode
extends Node {
    private ScalarStyle style;
    private String value;
    private Object cached;

    public ScalarNode(Tag tag, String value, ScalarStyle style, Mark startMark, Mark endMark) {
        super(tag, startMark, endMark);
        this.value = Objects.requireNonNull(value, "value in a Node is required.");
        this.style = Objects.requireNonNull(style, "Scalar style must be provided.");
    }

    public ScalarNode(Tag tag, String value, ScalarStyle style) {
        this(tag, value, style, null, null);
    }

    public ScalarStyle getScalarStyle() {
        return this.style;
    }

    public void setScalarStyle(ScalarStyle style) {
        this.style = Objects.requireNonNull(style, "Style cannot be null");
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.SCALAR;
    }

    @Override
    public void cacheConstructed(Object obj) {
        this.cached = obj;
    }

    @Override
    public Object getParsed() {
        return this.cached;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public ScalarNode clone() {
        ScalarNode scalarNode = new ScalarNode(this.getTag(), this.value, this.style, this.getStartMark(), this.getEndMark());
        scalarNode.copyPropertiesOf(this);
        scalarNode.cacheConstructed(this.cached);
        return scalarNode;
    }

    public String getValue() {
        return this.value;
    }

    public String toString() {
        return '<' + this.getClass().getName() + " (tag=" + this.getTag() + ", value=" + this.value + ")>";
    }
}

