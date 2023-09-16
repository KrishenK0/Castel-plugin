package fr.krishenk.castel.constants.metadata;

import fr.krishenk.castel.constants.namespace.Lockable;
import fr.krishenk.castel.constants.namespace.NamespaceRegistery;
import fr.krishenk.castel.data.DataManager;

import java.util.Collection;

public class CastelMetadataRegistry extends NamespaceRegistery<CastelMetadataHandler> implements Lockable {
    private static boolean ACCEPT_REGISTERS = true;

    @Override
    public void register(CastelMetadataHandler value) {
        if (value.getNamespace().getNamespace().equals("castel")) {
            throw new IllegalArgumentException("Cannot register metadata handlers as castel: " + value);
        }
        super.register(value);
    }

    @Override
    public void lock() throws IllegalAccessException {
        if (!ACCEPT_REGISTERS)
            throw  new IllegalAccessException("Registers are already closed");
        ACCEPT_REGISTERS = true;
    }

    public static void removeMetadata(DataManager<?, ?> dataManager, Collection<CastelMetadataHandler> metadataHandlers) {
        for (CastelObject data : dataManager.getLoadedData()) {
            metadataHandlers.forEach(x -> data.getMetadata().remove(x));
        }
    }
}
