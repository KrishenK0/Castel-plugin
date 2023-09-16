package fr.krishenk.castel.constants.group.model.relationships;

import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.namespace.NamespaceContainer;

public abstract class RelationAttribute implements NamespaceContainer {
    private final Namespace namespace;
    private int hash;

    public RelationAttribute(Namespace namespace) {
        this.namespace = namespace;
    }

    protected final void setHash(int hash) {
        this.hash = hash;
    }

    @Override
    public int hashCode() {
        return this.hash;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof RelationAttribute && this.hash == ((RelationAttribute) other).hash;
    }

    public abstract boolean hasAttribute(Group group1, Group group2);

    @Override
    public String toString() {
        return "RelationAttribute[" + this.namespace + "]";
    }

    @Override
    public Namespace getNamespace() {
        return this.namespace;
    }
}
