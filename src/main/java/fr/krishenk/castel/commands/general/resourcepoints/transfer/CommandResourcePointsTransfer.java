package fr.krishenk.castel.commands.general.resourcepoints.transfer;

import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.commands.general.resourcepoints.CommandResourcePoints;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.entity.Player;

public class CommandResourcePointsTransfer extends CastelParentCommand {
    public CommandResourcePointsTransfer(CastelParentCommand parent) {
        super("transfer", parent);
        if (!this.isDisabled()) {

        }
    }

    public static class ResourcePointsTransfer {
        protected long amount;

        protected CommandResult handleBasics(CommandContext context) {
            if (!context.assertPlayer() && !context.assertHasGuild() && !context.requireArgs(1)) {
                Player player = context.senderAsPlayer();
                CastelPlayer cp = context.getCastelPlayer();
                Guild guild = cp.getGuild();
                if (!cp.hasPermission(StandardGuildPermission.WITHDRAW)) {
                    StandardGuildPermission.WITHDRAW.sendDeniedMessage(player);
                    return CommandResult.FAILED;
                } else {
                    Long amount = CommandResourcePoints.validateAmount(false, context, null, 0);
                    if (amount == null) return CommandResult.FAILED;
                    this.amount = amount;
                    context.var("rp", StringUtils.toFancyNumber(amount));
                    context.var("amount", StringUtils.toFancyNumber(amount));
                    return CommandResult.SUCCESS;
                }
            }
            return CommandResult.FAILED;
        }
    }
}
