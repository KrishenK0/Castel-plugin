package fr.krishenk.castel.services;

import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

public class ServiceVault implements Service {
    private static Economy getEconomy() {
        RegisteredServiceProvider service = Bukkit.getServicesManager().getRegistration(Economy.class);
        return service == null ? null : (Economy) service.getProvider();
    }

    private static Permission getPermission() {
        RegisteredServiceProvider service = Bukkit.getServicesManager().getRegistration(Permission.class);
        return service == null ? null : (Permission) service.getProvider();
    }

    private static Chat getChat() {
        RegisteredServiceProvider service = Bukkit.getServicesManager().getRegistration(Chat.class);
        return service == null ? null : (Chat) service.getProvider();
    }

    public static boolean isAvailable(Component component) {
        switch (component) {
            case ECO:
                return ServiceVault.getEconomy() != null;
            case PERM:
                return ServiceVault.getPermission() != null;
            case CHAT:
                return ServiceVault.getChat() != null;
        }
        throw new AssertionError("Unknown Vault component " + component);
    }

    public static double getMoney(OfflinePlayer player) {
        return ServiceVault.getEconomy() == null ? 0.0 : ServiceVault.getEconomy().getBalance(player);
    }

    public static void deposit(OfflinePlayer player, double amount) {
        ServiceVault.getEconomy().depositPlayer(player, amount);
    }

    public static boolean hasMoney(OfflinePlayer player, double amount) {
        return ServiceVault.getEconomy() != null && ServiceVault.getEconomy().has(player, amount);
    }

    public static void withdraw(OfflinePlayer player, double amount) {
        ServiceVault.getEconomy().withdrawPlayer(player, amount);
    }

    public static String getDisplayName(Player player) {
        if (ServiceVault.getChat() == null) return player.getDisplayName();
        String prefix = ServiceVault.getChat().getPlayerPrefix(player);
        String suffix = ServiceVault.getChat().getPlayerSuffix(player);
        return prefix + player.getDisplayName() + suffix;
    }

    public static String getGroup(Player player) {
        if (ServiceVault.getPermission() == null) return "default";
        return ServiceVault.getPermission().hasGroupSupport() ? ServiceVault.getPermission().getPrimaryGroup(player) : "default";
    }

    public enum Component {
        ECO,
        CHAT,
        PERM;
    }
}
