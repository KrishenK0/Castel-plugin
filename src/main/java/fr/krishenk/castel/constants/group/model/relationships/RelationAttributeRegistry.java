package fr.krishenk.castel.constants.group.model.relationships;

import fr.krishenk.castel.constants.namespace.Lockable;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.namespace.NamespaceRegistery;

import java.util.Map;

public class RelationAttributeRegistry extends NamespaceRegistery<RelationAttribute> implements Lockable {
    private static boolean ACCEPT_REGISTERS = true;

    protected Map<Namespace, RelationAttribute> getRawRegistry() {
        return this.registry;
    }

    @Override
    public void register(RelationAttribute value) {
        if (value.getNamespace().getNamespace().equals("castel")) {
            throw new IllegalArgumentException("Cannot register custom relation attribute as castel namespace: " + value);
        }
        super.register(value);
    }

    @Override
    public void lock() throws IllegalAccessException {
        if (!ACCEPT_REGISTERS)
            throw new IllegalAccessException("Registers are already closed");
        ACCEPT_REGISTERS = false;
    }
}
