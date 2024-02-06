package fr.krishenk.castel.commands.general.invitations;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.TabCompleteManager;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandInviteCancel extends CastelCommand {
    public CommandInviteCancel() {
        super("cancel", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1) && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            if (!cp.hasPermission(StandardGuildPermission.INVITE)) {
                StandardGuildPermission.INVITE.sendDeniedMessage(player);
                return;
            }

            OfflinePlayer receiver = context.getOfflinePlayer(0);
            if (receiver != null) {
                CastelPlayer receiverCp = CastelPlayer.getCastelPlayer(receiver);
                if (!receiverCp.getInvites().containsKey(guild.getId())) {
                    Lang.COMMAND_CANCEL_NO_INVITE.sendMessage(player);
                    return;
                }

                receiverCp.getInvites().remove(guild.getId());
                Lang.COMMAND_CANCEL_INVITATION_CANCELLED.sendMessage(player, "receiver", receiver.getName());
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        CastelPlayer cp = context.getCastelPlayer();
        return(cp.hasPermission(StandardGuildPermission.INVITE) && context.isAtArg(0)) ? TabCompleteManager.getPlayers(context.arg(0)) : emptyTab();
    }
}
