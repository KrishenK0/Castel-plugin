package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.config.managers.NewConfigManager;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.FSUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;

public class CommandAdminResetConfigs extends CastelCommand implements Listener {
    private static long ACCEPT_COOLDOWN;
    private static boolean RESETTING;
    private static boolean DONE;

    public CommandAdminResetConfigs(CastelParentCommand parent) {
        super("resetConfigs", parent);
    }

    @EventHandler(priority = EventPriority.LOW)
    public final void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if (RESETTING) {
            event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.RED + " Server is currently whitelisted by Castel");
        }
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (!context.assertPlayer()) {
            context.sendError(Lang.COMMAND_ADMIN_RESETCONFIGS_CONSOLE_ONLY);
            return CommandResult.FAILED;
        } else if (RESETTING) {
            context.sendError(Lang.COMMAND_ADMIN_RESETCONFIGS_ALREADY);
            return CommandResult.FAILED;
        } else if (ACCEPT_COOLDOWN != 0L && !Duration.ofSeconds(5L).minus(Duration.ofMillis(System.currentTimeMillis() - ACCEPT_COOLDOWN)).isNegative()) {
            NewConfigManager.getGlobals().set("reset-configs-on-next-start", true);
//            NewConfigManager.getGlobalsAdapter().saveConfig();
            context.sendMessage(Lang.COMMAND_ADMIN_RESETCONFIGS_REQUESTED);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                CLogger.info("Shutting down...");
                Bukkit.shutdown();
            }, 200L);
            return CommandResult.SUCCESS;
        } else {
            ACCEPT_COOLDOWN = System.currentTimeMillis();
            context.sendError(Lang.COMMAND_ADMIN_RESETCONFIGS_CONFIRM);
            return CommandResult.FAILED;
        }
    }

    public static void resetConfig() throws InterruptedException {
        CommandAdminResetConfigs.RESETTING = true;
        CLogger.info("----------------------------------------------------------------");
        CLogger.info("Resetting configs...");
        NewConfigManager.getGlobals().set("reset-configs-on-next-start", null);
//        NewConfigManager.getGlobalsAdapter().saveConfig();
        CLogger.info("Deleting GUIs...");
        FSUtil.deleteFolder(CastelPlugin.getPath("guis"));
        CLogger.info("Deleting languages...");
        FSUtil.deleteFolder(CastelPlugin.getPath("languages"));
        CLogger.info("Deleting the repository...");
        FSUtil.deleteFolder(CastelPlugin.getPath("repository"));
        CLogger.info("Deleting structure configs...");
        FSUtil.deleteFolder(CastelPlugin.getPath("Structures"));
        CLogger.info("Deleting turrets configs...");
        FSUtil.deleteFolder(CastelPlugin.getPath("Turrets"));
        CLogger.info("Deleting the main config files...");
        FSUtil.deleteAllFileTypes(CastelPlugin.getFolder(), ".yml");
        CLogger.info("Deleting maps folder...");
        FSUtil.deleteFolder(CastelPlugin.getPath("maps"));
        CommandAdminResetConfigs.DONE = true;
        Thread.sleep(10000L);
        CLogger.info("Config resets are done.");
        CLogger.info("----------------------------------------------------------------");
    }
}
