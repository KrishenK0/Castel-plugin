package fr.krishenk.castel.constants.group.model.logs;

import fr.krishenk.castel.constants.namespace.NamespaceContainer;

public interface AuditLogProvider extends NamespaceContainer {
    AuditLog construct();
}
