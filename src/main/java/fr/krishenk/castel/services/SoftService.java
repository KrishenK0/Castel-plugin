package fr.krishenk.castel.services;

import fr.krishenk.castel.CLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.function.Supplier;

public enum SoftService {
    PLACEHOLDERAPI("PlaceholderAPI", "fr.krishenk.castel.services.placeholders.ServicePlaceholderAPI"),
    VAULT("Vault", "fr.krishenk.castel.services.ServiceVault"),
    WORLD_GUARD("WorldGuard", "fr.krishenk.castel.services.worldguard.ServiceWorldGuardSeven"/*, "org.kingdoms.services.worldguard.ServiceWorldGuardSix"*/),
    WORLD_EDIT("WorldEdit", "fr.krishenk.castel.services.ServiceWorldEdit"),
    ESSENTIALS("Essentials", "fr.krishenk.castel.services.vanish.ServiceEssentialsX");

    private final String name;
    private final Service service;
    private boolean available;

    SoftService(String name, String ... classes) {
        this.name = name;
        this.available = this.checkAvailability();
        this.service = this.available ? SoftService.getCompatibleServiceOf(classes) : null;
        this.available = this.service != null && this.service.isAvailable();
    }

    static Service getCompatibleServiceOf(String ... classes) {
        for (String service : classes) {
            try {
                Service instance = (Service) Class.forName(service).getConstructor(new Class[0]).newInstance(new Object[0]);
                if (!instance.isAvailable()) continue;
                return instance;
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
        return null;
    }

    SoftService(String name, Supplier<Service> service) {
        this.name = name;
        this.available = this.checkAvailability();
        if (!this.available) {
            this.service = EmptyService.INSTANCE;
            return;
        }
        this.service = service.get();
        this.available = this.service.isAvailable();
    }

    public Service getService() {
        return service;
    }

    public String getName() {
        return name;
    }

    public static void reportAvailability() {
        for (SoftService service : SoftService.values()) {
            if (service.available) {
                CLogger.info("&6" + service.name + " &2found and hooked.");
                continue;
            }
            CLogger.info("&e" + service.name + " &cnot found.");
        }
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public static boolean anyAvailable(SoftService ... services) {
        return Arrays.stream(services).anyMatch(SoftService::isAvailable);
    }

    public Plugin getPlugin() {
        return Bukkit.getPluginManager().getPlugin(this.name);
    }

    public boolean isAvailable() {
        return available;
    }

    private boolean checkAvailability() {
        Plugin plugin = Bukkit.getPluginManager().getPlugin(this.name);
        return plugin != null && plugin.isEnabled();
    }
}
