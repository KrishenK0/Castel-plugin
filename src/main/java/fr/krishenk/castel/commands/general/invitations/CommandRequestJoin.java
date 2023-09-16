package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.inviterequests.JoinRequests;
import fr.krishenk.castel.utils.CastelBukkitExtensions;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandRequestJoin extends CastelCommand {
    public CommandRequestJoin() {
        super("requestJoin", true);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (context.assertPlayer() || context.requireArgs(1)) return CommandResult.FAILED;
        CastelPlayer cp = context.getCastelPlayer();
        if (cp.hasGuild()) return context.fail(Lang.COMMAND_JOIN_ALREADY_IN_GUILD);
        Guild requestedGuild = context.getGuild(0);
        if (requestedGuild == null) return CommandResult.FAILED;
        if (JoinRequests.getJoinRequests(requestedGuild).containsKey(cp.getUUID()))
            return context.fail(Lang.COMMAND_REQUESTJOIN_ALREADY_SENT);

        List<Player> onlineMembers = requestedGuild.getOnlineMembers();
        List<Player> inviteablePlayers = new ArrayList<>();

        for (Player player : onlineMembers) {
            CastelBukkitExtensions extensions = CastelBukkitExtensions.INSTANCE;
            if (extensions.asCastelPlayer(player).hasPermission(StandardGuildPermission.INVITE)) {
                inviteablePlayers.add(player);
            }
        }

        for (Player player : inviteablePlayers) {
            context.sendMessage(player, Lang.COMMAND_REQUESTJOIN_SENT_ANNOUNCE);
        }

        context.getSettings().withContext(requestedGuild);
        context.sendMessage(Lang.COMMAND_REQUESTJOIN_SENT);

        JoinRequests.sendJoinRequestTo(cp, requestedGuild);

        return CommandResult.SUCCESS;
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.getGuilds(0);
    }
}
