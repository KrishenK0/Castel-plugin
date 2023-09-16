package fr.krishenk.castel.commands.general.resourcepoints;

import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.general.resourcepoints.transfer.CommandResourcePointsTransfer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.services.ServiceHandler;
import fr.krishenk.castel.utils.string.StringUtils;

import java.util.List;

public class CommandResourcePoints extends CastelParentCommand {
    public CommandResourcePoints() {
        super("resourcepoints", true);
        if (!this.isDisabled()) {
            new CommandResourcePointsConverter(this);
            new CommandResourcePointsTransfer(this);
            new CommandResourcePointsWithdraw(this);
            new CommandResourcePointsDeposit(this);
        }
    }

    public static Long validateAmount(boolean needsBankServices, CommandContext context, Lang usage, int index) {
        if (needsBankServices && ServiceHandler.bankServiceNotAvailable(context.getSender())) return null;
        if (!context.assertArgs(index + 1)) {
            if (usage != null) context.sendError(usage);
            return null;
        }
        String amountStr = context.arg(index);
        long amount;
        try {
            amount = Long.parseLong(amountStr);
        } catch (NumberFormatException e) {
            context.sendError(Lang.INVALID_NUMBER, "arg", amountStr, "needed", "rp");
            return null;
        }

        if (amount == 0L) {
            context.sendError(Lang.COMMAND_RESOURCEPOINTS_ZERO);
            return null;
        } else if (amount < 0L) {
            context.sendError(Lang.COMMAND_RESOURCEPOINTS_NEGATIVE);
            return null;
        } else return amount;
    }

    protected static List<String> processTabComplete(CommandTabContext context) {
        if (context.isAtArg(0)) {
            if (context.arg(0).isEmpty()) {
                return tabComplete("<amount>");
            } else {
                try {
                    double amount = Long.parseLong(context.arg(0));
                    amount *= Config.ECONOMY_RESOURCE_POINTS_WORTH.getDouble();
                    return tabComplete('$' + StringUtils.toFancyNumber(amount));
                } catch (NumberFormatException e) {
                    return tabComplete("invalid");
                }
            }
        } else return emptyTab();
    }
}
