package fr.krishenk.castel.commands.general.teleports;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.teleportation.TeleportRequest;
import fr.krishenk.castel.managers.teleportation.TpManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class CommandTpaAccept extends CastelCommand {
    public CommandTpaAccept() {
        super("tpaaccept", true);
    }

    @Override
    public void execute(CommandContext context) {
        int timerSeconds = Config.TPA_TELEPORT_TIMER_SECONDS.getInt();
        context.var("timer", timerSeconds);
        TeleportRequest request = getRequest(context, false);
    }

    protected static TeleportRequest getRequest(CommandContext context, boolean reject) {
        if (context.assertPlayer() || context.assertHasGuild()) return null;
        Player target = context.senderAsPlayer();
        Set<UUID> requests = CommandTpa.RECEIVED_REQUESTS.get(target.getUniqueId());
        if (requests != null && !requests.isEmpty()) {
            context.var("target", target.getName());
            OfflinePlayer offlinePlayer;
            if (requests.size() > 1) {
                if (!context.assertArgs(1)) {
                    context.sendError(Lang.COMMAND_TPA_MULTIPLE_REQUESTS);
                    return null;
                }

                offlinePlayer = context.getOfflinePlayer(0);
                if (!requests.contains(offlinePlayer.getUniqueId())) {
                    context.var("teleporter", offlinePlayer.getName());
                    context.sendError(Lang.COMMAND_TPA_PLAYER_DIDNT_REQUEST);
                    return null;
                }
            } else offlinePlayer = Bukkit.getOfflinePlayer(requests.iterator().next());

            context.var("teleporter", offlinePlayer.getName());
            Player teleporter = offlinePlayer.getPlayer();
            if (!reject) {
                if (teleporter == null) {
                    context.sendError(Lang.COMMAND_TPAACCEPT_REQUESTER_OFFLINE);
                    return null;
                }
            } else if (teleporter != null && TpManager.isTeleporting(teleporter)) {
                context.sendError(Lang.COMMAND_TPAACCEPT_ALREADY_TELEPORTING_TARGET);
                context.sendMessage(teleporter, Lang.COMMAND_TPAACCEPT_ALREADY_TELEPORTING_TELEPORTER);
            }

            if (!CommandTpa.canRequest(CastelPlayer.getCastelPlayer(teleporter).getGuild(), context.getGuild())) {
                context.sendError(Lang.COMMAND_TPAACCEPT_CHANGED_RELATION);
                return null;
            } else if (Config.DISABLED_WORLDS.isInDisabledWorld(teleporter)) {
                context.sendError(Lang.COMMAND_TPAACCEPT_DISABLED_WORLD_TELEPORTER);
                Lang.COMMAND_TPAACCEPT_DISABLED_WORLD_TELEPORTER_NOTIFY.sendError(teleporter, context.getSettings());
                return null;
            } else {
                TeleportRequest request = CommandTpa.SENT_REQUESTS.remove(teleporter.getUniqueId());
                requests.remove(teleporter.getUniqueId());
                if (requests.isEmpty()) CommandTpa.RECEIVED_REQUESTS.remove(target.getUniqueId());
                request.cancel();
                return request;
            }
        } else {
            context.sendError(Lang.COMMAND_TPA_NO_REQUESTS);
            return null;
        }
    }

    protected static List<String> getRequestedPlayers(CommandTabContext context) {
        if (!context.isAtArg(0) || !context.isPlayer()) {
            return emptyTab();
        } else {
            Player player = context.senderAsPlayer();
            Set<UUID> requests = CommandTpa.RECEIVED_REQUESTS.get(player.getUniqueId());
            return requests != null && !requests.isEmpty() ? requests.stream().map(x -> Bukkit.getOfflinePlayer(x).getName()).collect(Collectors.toList()) : emptyTab();
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return getRequestedPlayers(context);
    }
}
