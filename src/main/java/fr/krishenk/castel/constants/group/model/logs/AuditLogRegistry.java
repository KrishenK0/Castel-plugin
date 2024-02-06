package fr.krishenk.castel.constants.group.model.logs;

import fr.krishenk.castel.constants.namespace.Lockable;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.namespace.NamespaceRegistery;

import java.util.Map;

public class AuditLogRegistry extends NamespaceRegistery<AuditLogProvider> implements Lockable {
    private static boolean ACCEPT_REGISTERS = true;

    @Override
    public void register(AuditLogProvider value) {
        if (value.getNamespace().getNamespace().equals("castel")) {
            throw new IllegalArgumentException("Cannot register custom permission as castel namespace: " + value);
        }
        super.register(value);
    }

    protected Map<Namespace, AuditLogProvider> getRawRegistry() {
        return this.registry;
    }

    @Override
    public void lock() throws IllegalAccessException {
        if (!ACCEPT_REGISTERS) throw new IllegalAccessException("Registers are already closed");
        ACCEPT_REGISTERS = false;
    }
}
