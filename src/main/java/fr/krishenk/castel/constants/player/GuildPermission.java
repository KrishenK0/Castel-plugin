package fr.krishenk.castel.constants.player;

import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.namespace.NamespaceContainer;

public class GuildPermission implements NamespaceContainer {
    private int hash;
    private final Namespace namespace;

    public GuildPermission(Namespace namespace) {
        this.namespace = namespace;
    }

    @Override
    public Namespace getNamespace() { return this.namespace; }

    protected void setHash(int hash) {
        this.hash = hash;
    }

    public final int hashCode() {
        return this.hash;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof GuildPermission)) return false;
        return this.hash == obj.hashCode();
    }

    public String toString() {
        return "GuildPermission[" + this.namespace.asString() + "]";
    }
}
