package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.GuildInvite;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class CommandAccept extends CastelCommand {
    public CommandAccept() {
        super("accept", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (cp.hasGuild()) {
                Lang.COMMAND_INVITE_ALREADY_IN_GUILD.sendMessage(player);
            } else {
                Map<UUID, GuildInvite> invites = cp.getInvites();
                if (invites.isEmpty()) {
                    Lang.COMMAND_ACCEPT_NO_INVITES.sendError(player);
                } else {
                    Guild guild;
                    if (!context.assertArgs(1)) {
                        if (invites.size() != 1) {
                            Lang.COMMAND_ACCEPT_MULTIPLE_INVITES.sendError(player);
                            return;
                        }

                        Map.Entry<UUID, GuildInvite> inviteEntry = invites.entrySet().iterator().next();
                        guild = Guild.getGuild(inviteEntry.getKey());
                    } else {
                        guild = context.getGuild(0);
                        if (guild == null) return;
                    }

                    acceptInvite(player, guild);
                }
            }
        }
    }

    public static void acceptInvite(Player player, Guild guild) {
        if (guild == null) {
            Lang.COMMAND_ACCEPT_NO_LONGER_EXISTS.sendError(player);
        } else {
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            GuildInvite invite = cp.getInvites().remove(guild.getId());
            if (invite == null) {
                Lang.COMMAND_ACCEPT_NOT_INVITED.sendError(player, "guild", guild.getName());
            } else if (!invite.canAccept()) {
                Lang.COMMAND_ACCEPT_EXPIRED.sendError(player, "guild", guild.getName());
            } else {
                CastelPlayer inviter = invite.getCastelPlayer();
                OfflinePlayer inviterPlayer = inviter.getOfflinePlayer();
                Lang requirement = checkRequirementsToJoin(player, guild);
                if (requirement != null) requirement.sendError(player, (new MessageBuilder()).withContext(guild));

                if (!cp.joinGuild(guild, (event) -> event.getMetadata().put(GuildInvite.NAMESPACE, invite)).isCancelled()) {
                    Lang.COMMAND_ACCEPT_ACCEPTED.sendMessage(player, "inviter", inviterPlayer.getName(), "guild", guild.getName());
                    if (inviterPlayer.isOnline()) Lang.COMMAND_ACCEPT_NOTIFY.sendMessage(inviterPlayer.getPlayer(), "name", player.getName());

                    for (Player member : guild.getOnlineMembers()) {
                        Lang.COMMAND_ACCEPT_JOINED.sendMessage(member, player);
                    }
                }
            }
        }
    }

    public static Lang checkRequirementsToJoin(Player player, Guild guild) {
        return guild.isFull() ? Lang.COMMAND_ACCEPT_MAX_MEMBERS : null;
    }
}
