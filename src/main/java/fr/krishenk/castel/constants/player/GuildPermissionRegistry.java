package fr.krishenk.castel.constants.player;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.namespace.Lockable;
import fr.krishenk.castel.constants.namespace.Namespace;
import fr.krishenk.castel.constants.namespace.NamespaceRegistery;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class GuildPermissionRegistry extends NamespaceRegistery<GuildPermission> implements Lockable {
    private static boolean ACCEPT_REGISTERS = true;

    protected Map<Namespace, GuildPermission> getRawRegistry() {
        return this.registry;
    }

    @Override
    public void register(GuildPermission value) {
        if (value.getNamespace().equals("castel")) {
            throw new IllegalArgumentException("Cannot register custom permission as castel namespace: " + value);
        }
        super.register(value);
    }

    public static Set<GuildPermission> parse(Collection<String> permissionList) {
        HashSet<GuildPermission> permissions = new HashSet<>();
        GuildPermissionRegistry registry = CastelPlugin.getInstance().getPermissionRegistry();
        for (String permission : permissionList) {
            GuildPermission perm = registry.getRegistered(Namespace.fromString(permission));
            if (perm == null) continue;
            permissions.add(perm);
        }
        return permissions;
    }

    @Override
    public void lock() throws IllegalAccessException {
        if (!ACCEPT_REGISTERS) throw new IllegalAccessException("Registers are already closed");
        ACCEPT_REGISTERS = false;
    }
}
