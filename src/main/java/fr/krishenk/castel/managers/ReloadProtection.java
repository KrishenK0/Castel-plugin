package fr.krishenk.castel.managers;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

import java.net.URI;
import java.nio.file.FileSystemNotFoundException;
import java.nio.file.FileSystems;
import java.util.*;

public class ReloadProtection implements Listener {
    private static final Set<String> KNOWN_RELOAD_COMMANDS = new HashSet<>(40);

    public static void ensureLoadOnce() {
        URI uri = null;
//        try {
//            // TODO : SKIP THIS PART CAUSE NULL POINTER EXCEPTION
//            uri = CastelPlugin.class.getResource("/config.yml").toURI();
//        } catch (URISyntaxException ex) {
//            ex.printStackTrace();
//        }
        try {
            FileSystems.getFileSystem(uri);
            ReloadProtection.warn();
        } catch (FileSystemNotFoundException | NullPointerException ex) {
            Class<?> CraftServerClass = ReflectionUtils.getCraftClass("CraftServer");
            if (CraftServerClass != null) {
                int reloadCount = 0;
                try {
                    reloadCount = (int) CraftServerClass.getDeclaredField("reloadCount").get(Bukkit.getServer());
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    //
                }
                if (reloadCount > 0) ReloadProtection.warn();
            }
        }
    }

    private static void warn() {
        CastelPlugin.getInstance().getLogger().info("You've reloaded the plugin using an external service. In order to enable the plugin again, you need to restart your server. During this period all guilds will remain unprotected.");
        new Timer((true)).scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                CastelPlugin.getInstance().getLogger().info("The plugin is disabled and all your guilds are unprotected. Restart the server to fix this issue.");
            }
        }, 0L, 5000L);
        throw new IllegalStateException("Cannot enable the plugin again.");
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onReloadCommand(PlayerCommandPreprocessEvent event) {
        if (ReloadProtection.handleReloadCommand(event.getPlayer(), event.getMessage().substring(1)))
            event.setCancelled(true);
    }

    private static boolean handleReloadCommand(CommandSender player, String cmd) {
        if (!(cmd = cmd.toLowerCase()).startsWith("reload") && !KNOWN_RELOAD_COMMANDS.contains(cmd))
            return false;
        CastelPlugin.getInstance().getLogger().info("\n&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n\n &4&lDo not reload plugins with&e /reload &4&lor external plugins.&r\n &4&lUse their respective commands instead. E.g. &2 &l/c reload&4 &linstead.&r\n &2If you want to reload CastelPlugin, you won't even need to reload in most cases since the plugin\n automatically reloads most configs.\n This action has been blocked by Castel plugin.\n\n&8-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-\n");
        return true;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onReloadCommand(ServerCommandEvent event) {
        if (handleReloadCommand(event.getSender(), event.getCommand()))
            event.setCancelled(true);
    }

    static {
        for (String mainCMD : Arrays.asList("plugman", "plugmanx")) {
            for (String subCMD : Arrays.asList("reload", "enable", "unload", "load", "disable")) {
                for (String name : Arrays.asList("castel", "castelplugin", "castel-plugin", "castelplugins", "castel-plugins")) {
                    KNOWN_RELOAD_COMMANDS.add(mainCMD + ' ' + subCMD + ' ' + name);
                }
            }
        }
    }
}
