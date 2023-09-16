package fr.krishenk.castel.services;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.services.vanish.ServiceVanish;
import fr.krishenk.castel.services.worldguard.ServiceWorldGuard;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.MetadataValue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

public class ServiceHandler {
    private static final List<BiConsumer<Player, String>> GLOBAL_MESSAGE_HANDLERS = new ArrayList<>();

    public static boolean bankServiceAvailable() {
        return SoftService.VAULT.isAvailable() && ServiceVault.isAvailable(ServiceVault.Component.ECO);
    }

    // public static void announce();

    public static boolean bankServiceNotAvailable(CommandSender player) {
        if (ServiceHandler.bankServiceAvailable()) return false;
        player.sendMessage("&4Bank services are currently unavailable.\nThis command can only be used with an economy plugin present.");
        return true;
    }

    public static void addGlobalMessageHanlder(BiConsumer<Player, String> handler) {
        GLOBAL_MESSAGE_HANDLERS.add(handler);
    }

    public static boolean isVanished(Player player) {
        for (MetadataValue metadata : player.getMetadata("vanished")) {
            if (!metadata.asBoolean()) continue;
            return true;
        }
        return Stream.of(SoftService.ESSENTIALS).anyMatch(x -> x.isAvailable() && ((ServiceVanish)x.getService()).isVanished(player));
    }

    public static boolean isInGodMode(Player p) {
        return Stream.of(SoftService.ESSENTIALS).anyMatch(x -> x.isAvailable() && ((ServiceVanish)x.getService()).isInGodMode(p));
    }

    public static boolean isInRegion(SimpleChunkLocation chunk) {
        return isInRegion(chunk, 0);
    }

    public static boolean isInRegion(SimpleChunkLocation chunk, int radius) {
        return SoftService.WORLD_GUARD.isAvailable() && ((ServiceWorldGuard)SoftService.WORLD_GUARD.getService()).isChunkInRegion(chunk.getBukkitWorld(), chunk.getX(), chunk.getZ(), radius);
    }

    public static boolean isInRegion(Location location, String region) {
        return SoftService.WORLD_GUARD.isAvailable() && ((ServiceWorldGuard)SoftService.WORLD_GUARD.getService()).isLocationInRegion(location, region);
    }

    public static void sendGlobalMessage(Player player, String message) {
        for (BiConsumer<Player, String> handler : GLOBAL_MESSAGE_HANDLERS) {
            handler.accept(player, message);
        }
    }

    public static String getGroup(Player p) {
        return SoftService.VAULT.isAvailable() && ServiceVault.isAvailable(ServiceVault.Component.PERM) ? ServiceVault.getGroup(p) : null;
    }
}
