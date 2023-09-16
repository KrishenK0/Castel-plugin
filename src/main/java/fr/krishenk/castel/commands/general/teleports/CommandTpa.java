package fr.krishenk.castel.commands.general.teleports;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.StandardRelationAttribute;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.teleportation.TeleportRequest;
import fr.krishenk.castel.managers.teleportation.TpManager;
import fr.krishenk.castel.utils.time.TimeUtils;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;

public class CommandTpa extends CastelCommand {
    protected static final Map<UUID, TeleportRequest> SENT_REQUESTS = new HashMap<>();
    protected static final Map<UUID, Set<UUID>> RECEIVED_REQUESTS = new HashMap<>();

    public CommandTpa() {
        super("tpa", true);
    }

    static void sendRequest(CommandContext context, Player teleporter, Player target, long expireTicks) {
        BukkitTask task = (new BukkitRunnable() {
            @Override
            public void run() {
                CommandTpa.SENT_REQUESTS.remove(teleporter.getUniqueId());
                Set<UUID> requests = CommandTpa.RECEIVED_REQUESTS.get(target.getUniqueId());
                if (requests != null) {
                    requests.remove(teleporter.getUniqueId());
                    if (requests.isEmpty()) {
                        CommandTpa.RECEIVED_REQUESTS.remove(target.getUniqueId());
                    }
                }

                Lang.COMMAND_TPA_EXPIRED_TELEPORTER.sendError(teleporter, context.getSettings());
                Lang.COMMAND_TPA_EXPIRED_TARGET.sendError(target, context.getSettings());
            }
        }).runTaskLaterAsynchronously(plugin, expireTicks);
        TeleportRequest request = new TeleportRequest(teleporter, target, task);
        SENT_REQUESTS.put(teleporter.getUniqueId(), request);
        RECEIVED_REQUESTS.compute(target.getUniqueId(), (k, v) -> {
            if (v == null) v = new HashSet<>(2);
            v.add(teleporter.getUniqueId());
            return v;
        });
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.assertHasGuild() && !context.requireArgs(1)) {
            Player teleporter = context.senderAsPlayer();
            if (!TpManager.alreadyTping(teleporter)) {
                Player target = context.getPlayer(0);
                if (target != null) {
                    context.var("target", target.getName());
                    context.var("teleporter", teleporter.getName());
                    if (teleporter.getUniqueId().equals(target.getUniqueId())) {
                        context.sendMessage(Lang.COMMAND_TPA_YOURSELF);
                        teleporter.teleport(teleporter);
                    } else {
                        CastelPlayer targetCp = CastelPlayer.getCastelPlayer(target);
                        Guild targetGuild = targetCp.getGuild();
                        if (targetGuild == null) {
                            context.sendError(Lang.NOT_FOUND_PLAYER_NO_GUILD);
                        } else {
                            Guild guild = context.getGuild();
                            if (!canRequest(guild, targetGuild)) {
                                context.sendError(Lang.COMMAND_TPA_NOT_FRIENDLY);
                            } else {
                                TeleportRequest previousRequest = SENT_REQUESTS.get(teleporter.getUniqueId());
                                if (previousRequest != null) {
                                    context.var("previous-request-player", previousRequest.getTarget().getName());
                                    context.var("previous-request-time", TimeUtils.getTime(previousRequest.getSenTime()));
                                    context.sendError(Lang.COMMAND_TPA_PENDING_REQUEST);
                                } else if (Config.DISABLED_WORLDS.isInDisabledWorld(teleporter)) {
                                    context.sendError(Lang.COMMAND_TPA_DISABLED_WORLD_TELEPORTER);
                                } else if (Config.DISABLED_WORLDS.isInDisabledWorld(target)) {
                                    context.sendError(Lang.COMMAND_TPA_DISABLED_WORLD_TARGET);
                                } else {
                                    CastelPlayer cp = context.getCastelPlayer();
                                    if (cp.hasPermission(StandardGuildPermission.INSTANT_TELEPORT) && guild.getId().equals(targetGuild.getId())) {
                                        teleporter.teleport(target);
                                        context.sendMessage(Lang.COMMAND_TPA_INSTANT_TELEPORTER);
                                        context.sendMessage(target, Lang.COMMAND_TPA_TARGET_NOTIFICATION);
                                    } else {
                                        sendRequest(context, teleporter, target, TimeUtils.millisToTicks(Config.TPA_DEFAULT_TIMER.getManager().getTimeMillis()));
                                        context.sendMessage(Lang.COMMAND_TPA_REQUESTED_TELEPORTER);
                                        context.sendMessage(target, Lang.COMMAND_TPA_REQUESTED_TARGET);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    protected static boolean canRequest(Guild guild, Guild targetGuild) {
        if (!Config.TPA_ALLOW_FROM_OTHER_GUILDS.getManager().getBoolean()) {
            return guild.getId().equals(targetGuild.getId());
        } else {
            return guild.hasAttribute(targetGuild, StandardRelationAttribute.CEASEFIRE);
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isPlayer() && context.isAtArg(0)) {
            Player teleporter = context.senderAsPlayer();
            return context.getPlayers(0, (player) -> {
                if (teleporter.getUniqueId().equals(player.getUniqueId())) return false;
                else {
                    CastelPlayer targetCp = CastelPlayer.getCastelPlayer(player);
                    Guild targetGuild = targetCp.getGuild();
                    if (targetGuild == null) return false;
                    else {
                        Guild guild = context.getGuild();
                        return canRequest(guild, targetGuild);
                    }
                }
            });
        } else return emptyTab();
    }
}