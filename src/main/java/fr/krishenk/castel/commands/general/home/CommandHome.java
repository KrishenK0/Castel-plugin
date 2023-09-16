package fr.krishenk.castel.commands.general.home;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Group;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.StandardRelationAttribute;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.general.GroupHomeTeleportEvent;
import fr.krishenk.castel.events.general.GuildSetHomeEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.teleportation.IsolatedTpManager;
import fr.krishenk.castel.managers.teleportation.TeleportTask;
import fr.krishenk.castel.managers.teleportation.TpManager;
import fr.krishenk.castel.utils.PlayerUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandHome extends CastelCommand implements Listener {
    private static final IsolatedTpManager TELEPORTS = new IsolatedTpManager();

    public CommandHome() {
        super("home", true);
    }

    public static Location callEvent(Group group, Player player, Location location, GroupHomeTeleportEvent.LocationType locationType) {
        GroupHomeTeleportEvent event = new GroupHomeTeleportEvent(group, player, location, locationType);
        Bukkit.getPluginManager().callEvent(event);
        return event.isCancelled() ? null : event.getLocation();
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (context.assertPlayer()) return CommandResult.FAILED;
        Player player = context.senderAsPlayer();
        if (TpManager.alreadyTping(player)) return CommandResult.FAILED;
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        final GroupHomeTeleportEvent.LocationType locType = GroupHomeTeleportEvent.LocationType.HOME;
        final Guild guild;
        final Location home;
        if (!context.assertArgs(1)) {
            if (context.assertHasGuild()) return CommandResult.FAILED;
            if (!cp.hasPermission(StandardGuildPermission.HOME)) {
                StandardGuildPermission.HOME.sendDeniedMessage(player);
                return CommandResult.FAILED;
            }

            guild = cp.getGuild();
            home = guild.getHome();
            if (home == null) {
                context.sendError(Lang.COMMAND_HOME_NOT_SET);
                return CommandResult.FAILED;
            }
        } else {
            if (!context.hasPermission(CastelPluginPermission.COMMAND_HOME_OTHERS, true)) {
                context.sendError(Lang.COMMAND_HOME_PERMISSION);
                return CommandResult.FAILED;
            }

            guild = context.getGuild(0);
            if (guild == null) return CommandResult.FAILED;

            home = guild.getHome();
            if (home == null) {
                context.sendError(Lang.COMMAND_HOME_NOT_SET_OTHERS, "guild", guild.getName());
                return CommandResult.FAILED;
            }

            if (!cp.isAdmin() && !guild.isMember(player)) {
                if (!guild.isHomePublic()) {
                    context.sendError(Lang.COMMAND_HOME_NOT_PUBLIC, "guild", guild.getName());
                    return CommandResult.FAILED;
                }

                if (!guild.hasAttribute(cp.getGuild(), StandardRelationAttribute.HOME)) {
                    context.sendError(Lang.COMMAND_HOME_CANT_USE_PUBLIC_HOME, "guild", guild.getName());
                    return CommandResult.FAILED;
                }
            }
        }

        int timer = Config.HOME_TELEPORT_DELAY.getInt();
        if (timer > 0 && !cp.isAdmin() && !context.hasPermission(CastelPluginPermission.COMMAND_HOME_BYPASS_TIMER) && !PlayerUtils.invulnerableGameMode(player)) {
            BukkitTask task;
            if (Config.HOME_USE_TIMER_MESSAGE.getBoolean()) {
                task = (new BukkitRunnable() {
                    int timed = timer;

                    @Override
                    public void run() {
                        if (this.timed <= 0) {
                            Location finalHome = CommandHome.callEvent(guild, player, home, locType);
                            if (finalHome != null) {
                                player.teleport(finalHome);
                                Lang.COMMAND_HOME_SUCCESS.sendMessage(player);
                            }

                            CommandHome.TELEPORTS.end(player);
                            this.cancel();
                        } else {
                            Lang.COMMAND_HOME_TELEPORTING.sendMessage(player, "countdown", this.timed);
                            --this.timed;
                        }
                    }
                }).runTaskTimer(plugin, 0L, 20L);
            } else {
                Lang.COMMAND_HOME_TELEPORTING.sendMessage(player, "countdown", timer);
                task = (new BukkitRunnable() {
                    @Override
                    public void run() {
                        Location finalHome = CommandHome.callEvent(guild, player, home, locType);
                        if (finalHome != null) {
                            player.teleport(home);
                            Lang.COMMAND_HOME_SUCCESS.sendMessage(player);
                        }
                        CommandHome.TELEPORTS.end(player);
                    }
                }).runTaskLater(plugin, timer*20L);
            }

            TeleportTask tpTask = new TeleportTask(player, task);
            tpTask.onAnyMove((event) -> {
                Lang.TELEPORTS_MOVED.sendError(player);
                return true;
            });
            TELEPORTS.put(tpTask);
            return CommandResult.SUCCESS;
        } else {
            Location homeLoc = callEvent(guild, player, home, locType);
            if (homeLoc == null) return CommandResult.FAILED;
            player.teleport(homeLoc);
            context.sendMessage(Lang.COMMAND_HOME_SUCCESS);
            return CommandResult.SUCCESS;
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onHomeSet(GuildSetHomeEvent event) {
        List<Player> putAnEnd = new ArrayList<>();
        for (TeleportTask task : TELEPORTS.getTasks()) {
            if (event.getGuild().isMember(task.player)) {
                Lang.COMMAND_HOME_CHANGED.sendError(task.player);
                putAnEnd.add(task.player);
            }
        }
        putAnEnd.forEach(TELEPORTS::end);
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) && context.hasPermission(CastelPluginPermission.COMMAND_HOME_OTHERS, true) ? context.getGuilds(0) : emptyTab();
    }
}
