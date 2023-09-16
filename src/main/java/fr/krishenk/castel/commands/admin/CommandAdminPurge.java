package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.commands.*;
import fr.krishenk.castel.config.managers.ConfigWatcher;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.ProtectionSign;
import fr.krishenk.castel.data.CastelDataCenter;
import fr.krishenk.castel.data.DataManager;
import fr.krishenk.castel.data.StartupCache;
import fr.krishenk.castel.events.general.GroupDisband;
import fr.krishenk.castel.events.items.CastelItemRemoveContext;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.chat.ChatInputManager;
import fr.krishenk.castel.utils.FSUtil;
import fr.krishenk.castel.utils.string.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.server.ServerCommandEvent;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;
import java.util.Objects;

public class CommandAdminPurge extends CastelCommand implements Listener {
    public static long ACCEPT_COOLDOWN;
    private static boolean PURGING;
    private static boolean DONE;
    private static boolean FULLY_LOADED;

    public CommandAdminPurge(CastelParentCommand parent) {
        super("purge", parent);
    }

    @EventHandler(priority = EventPriority.LOW)
    public void onPlayerJoin(AsyncPlayerPreLoginEvent event) {
        if (PURGING) event.disallow(AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST, ChatColor.DARK_RED + " Server is currently whitelisted by Castel.");
    }

    @EventHandler
    public void onCommand(ServerCommandEvent event) {
        if (PURGING) {
            if (Objects.equals(event.getCommand(), "stop")) {
                if (DONE) Bukkit.shutdown();
                else Lang.COMMAND_ADMIN_PURGE_STOP.sendError(event.getSender());
            } else {
                Lang.COMMAND_ADMIN_PURGE_COMMAND.sendError(event.getSender());
                event.setCancelled(true);
            }
        }
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (!context.assertPlayer()) {
            context.sendError(Lang.COMMAND_ADMIN_PURGE_CONSOLE_ONLY);
            return CommandResult.FAILED;
        }
        if (PURGING) {
            context.sendError(Lang.COMMAND_ADMIN_PURGE_ALREADY);
            return CommandResult.FAILED;
        }
        if (!FULLY_LOADED) {
            context.sendError(Lang.COMMAND_ADMIN_PURGE_NOT_LOADED);
            return CommandResult.FAILED;
        }

        if (ACCEPT_COOLDOWN != 0L && !Duration.ofSeconds(5L).minus(Duration.ofMillis(System.currentTimeMillis() - ACCEPT_COOLDOWN)).isNegative()) {
            PURGING = true;
            ConfigWatcher.setAccepting(false);
            List<Throwable> errors = new ArrayList<>();

            try {
                ChatInputManager.endAllConversations();
                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    player.kickPlayer(ChatColor.DARK_RED + "Castel purging process has started.");
                }
            } catch (Throwable e) {
                errors.add(e);
            }

            context.sendMessage(Lang.COMMAND_ADMIN_PURGE_PURGING);
            final CastelDataCenter dataCenter = CastelDataCenter.get();
            CastelItemRemoveContext itemRemoveContext = new CastelItemRemoveContext();
            itemRemoveContext.setDropsItem(false);
            for (Land land : dataCenter.getLandManager().peekAllData()) {
                try {
                    for (ProtectionSign sign : land.getProtectedBlocks().values()) {
                        Block block = sign.getSign().getBlock();
                        block.setType(Material.AIR);
                    }
                } catch (ConcurrentModificationException e) {
                    CLogger.warn("ConcurrentModificationException for protectedBlocks");
                }
            }

            for (Guild guild : dataCenter.getGuildManager().getGuilds()) {
                guild.disband(GroupDisband.Reason.ADMIN);
            }

            // TODO : DISBAND GROUP

            try {
                dataCenter.getCastelPlayerManager().clear();
                dataCenter.getLandManager().clear();
                dataCenter.getGuildManager().clear();
                dataCenter.getMTG().clear();
            } catch (Throwable e) {
                errors.add(e);
            }

            if (Config.DATABASE_USE_DATA_FOLDER.getManager().getBoolean()) {
                try {
                    FSUtil.deleteFolder(CastelDataCenter.DATA_FOLDER);
                } catch (Throwable e) {
                    errors.add(e);
                }
            } else {
                for (DataManager<?, ?> dataManager : dataCenter.getAllDataManagers()) {
                    try {
                        dataManager.deleteAllData();
                    } catch (Throwable e) {
                        errors.add(e);
                    }
                }
            }

            if (errors.isEmpty()) context.sendMessage(Lang.COMMAND_ADMIN_PURGE_DONE);
            else {
                context.sendMessage(Lang.COMMAND_ADMIN_PURGE_DONE_WITH_ERRORS);

                List<String> sanitizedErrors = new ArrayList<>(errors.size());
                for (Throwable throwable : errors) {
                    sanitizedErrors.add(sanitizeStackTrace(throwable));
                }
                CLogger.error(StringUtils.join(sanitizedErrors.toArray(), "---------------------------------------------------------------\n"));
            }

            DONE = true;
            CLogger.info("Purging is done. Stopping the server in 5 seconds...");
            Bukkit.getScheduler().runTaskLater(plugin,  () -> {
                CLogger.info("Shutting done...");
                Bukkit.shutdown();
            },100L);
            return CommandResult.SUCCESS;
        } else {
            ACCEPT_COOLDOWN = System.currentTimeMillis();
            context.sendError(Lang.COMMAND_ADMIN_PURGE_CONFIRM);
            return CommandResult.FAILED;
        }
    }

    public static String sanitizeStackTrace(Throwable throwable) {
        Validate.notNull(throwable);
        StringBuilder builder = new StringBuilder(1000);
        Throwable err;
        for (err = throwable; err.getCause() != null; err = err.getCause()) {
            Validate.notNull(err.getCause());
        }
        builder.append(err.getMessage()).append('\n');
        for (StackTraceElement stack : err.getStackTrace()) {
            String clazz = stack.getClassName();
            if (!StringUtils.containsAny(clazz, "fr.krishenk.castel.commands.admin.CommandAdminPurge") || !StringUtils.contains(clazz, '$')) {
                if (clazz.equals(CastelCommandHandler.class.getName())) break;

                builder.append("    ").append(clazz).append("->").append(stack.getMethodName()).append(':').append(stack.getLineNumber()).append('\n');
            }
        }
        return builder.toString();
    }

    static {
        StartupCache.whenLoaded((x) -> CastelPlugin.taskScheduler().asyncLater(Duration.ofSeconds(5L), () -> FULLY_LOADED = true));
    }
}
