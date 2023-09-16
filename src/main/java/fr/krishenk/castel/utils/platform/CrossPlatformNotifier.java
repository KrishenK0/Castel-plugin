package fr.krishenk.castel.utils.platform;

import fr.krishenk.castel.CLogger;

import java.util.stream.Stream;

public class CrossPlatformNotifier {
    protected static boolean isRunningForge() {
        String fml = "net.minecraftforge.fml.";
        return Stream.of("common.Mod", "common.Loader", "common.FMLContainer", "ModLoader", "client.FMLClientHandler", "server.ServerMain").anyMatch((x) -> {
            return classExists(fml + x);
        });
    }

    protected static boolean isRunningGeyser() {
        String geyser = "org.geysermc";
        return Stream.of("geyser.GeyserMain", "geyser.Constants", "connector.GeyserConnector", "connector.network.session.GeyserSession", "api.Geyser", "api.connection.Connection").anyMatch((x) -> {
            return classExists(geyser + x);
        });
    }

//    public static boolean isFloodgatePlayer(Player player) {
//        return Platform.BEDROCK.isAvailable() && FloodgateApi.getInstance().isFloodgatePlayer(player.getUniqueId());
//    }

    protected static boolean isRunningBukkit() {
        return classExists("org.bukkit.entity.Player") && classExists("org.bukkit.Bukkit");
    }

    protected static boolean isRunningPaper() {
        return classExists("com.destroystokyo.paper.PaperConfig") || classExists("io.papermc.paper.configuration.Configuration");
    }

    public static boolean isRunningSpigot() {
        return classExists("org.spigotmc.SpigotConfig");
    }

    public static void warn() {
        if (Platform.FORGE.isAvailable()) {
            CLogger.warn("-----------------------------------------------------------------------------------------------");
            CLogger.warn("Your server is running on a platform that supports Forge. The plugin may not function properly.");
            CLogger.warn("-----------------------------------------------------------------------------------------------");
        }

        if (Platform.BEDROCK.isAvailable()) {
            CLogger.warn("-----------------------------------------------------------------------------------------------");
            CLogger.warn("Your server is running on a platform that supports Bedrock Edition. The plugin may not function properly.");
            CLogger.warn("-----------------------------------------------------------------------------------------------");
        }

    }

    private static boolean classExists(String clazz) {
        try {
            Class.forName(clazz, false, CrossPlatformNotifier.class.getClassLoader());
            return true;
        } catch (NoClassDefFoundError | ClassNotFoundException var2) {
            return false;
        } catch (Throwable var3) {
            var3.printStackTrace();
            return true;
        }
    }
}
