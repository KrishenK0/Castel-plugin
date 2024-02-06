package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.GuildInvite;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class CommandDecline extends CastelCommand {
    public CommandDecline() {
        super("decline", true);
    }

    @Override
    public void execute(CommandContext context) {
        Guild guild;
        if (context.assertPlayer()) return;

        Player player = context.senderAsPlayer();
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        Map<UUID, GuildInvite> invites = cp.getInvites();
        if (invites.isEmpty()) {
            Lang.COMMAND_DECLINE_NO_INVITES.sendMessage(player);
            return;
        }
        if (!context.assertArgs(1)) {
            if (invites.size() != 1) {
                Lang.COMMAND_DECLINE_MULTIPLE_INVITES.sendMessage(player);
                return;
            }
            Map.Entry<UUID, GuildInvite> inviteEntry = invites.entrySet().iterator().next();
            guild = Guild.getGuild(inviteEntry.getKey());
        } else {
            if (context.argEquals(0, "*")) {
                CommandDecline.declineAll(player);
                return;
            }
            guild = context.getGuild(0);
            if (guild == null) return;
        }
        CommandDecline.decline(player, guild);
    }

    public static void declineAll(Player player) {
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        Map<UUID, GuildInvite> invites = cp.getInvites();
        Lang.COMMAND_DECLINE_ALL.sendMessage(player, "invites", invites.size());
        for (UUID guildId : invites.keySet()) {
            decline(player, Guild.getGuild(guildId));
        }
        invites.clear();
    }

    public static void decline(Player player, Guild guild) {
        if (guild == null) {
            Lang.COMMAND_DECLINE_NO_LONGER_EXISTS.sendMessage(player);
        } else {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            GuildInvite invite = cp.getInvites().remove(guild.getId());
            if (invite == null) {
                Lang.COMMAND_DECLINE_NOT_INVITED.sendMessage(player, "guild", guild.getName());
            } else if (!invite.canAccept()) {
                Lang.COMMAND_DECLINE_EXPIRED.sendMessage(player, "guild", guild.getName());
            } else {
                CastelPlayer inviter = invite.getCastelPlayer();
                OfflinePlayer inviterPlayer = inviter.getOfflinePlayer();
                Lang.COMMAND_DECLINE_DECLINED.sendMessage(player, "inviter", inviterPlayer.getName(), "guild", guild.getName());
                if (inviterPlayer.isOnline()) Lang.COMMAND_DECLINE_NOTIFY.sendMessage(inviterPlayer.getPlayer(), "name", player.getName());
            }
        }
    }
}
