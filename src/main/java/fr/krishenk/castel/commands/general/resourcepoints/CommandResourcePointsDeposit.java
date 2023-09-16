package fr.krishenk.castel.commands.general.resourcepoints;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.general.GroupResourcePointConvertEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.services.ServiceVault;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandResourcePointsDeposit extends CastelCommand {
    public CommandResourcePointsDeposit(CastelParentCommand parent) {
        super("deposit", parent);
    }

    public static boolean handleDeposit(CastelPlayer cp, Player player, Guild guild, long rp) {
        double worth = rp * Config.ECONOMY_RESOURCE_POINTS_WORTH.getDouble();
        String fancyRp = StringUtils.toFancyNumber(rp);
        String fancyWorth = StringUtils.toFancyNumber(worth);
        String fancyBalance;
        if (!ServiceVault.hasMoney(player, worth)) {
            fancyBalance = StringUtils.toFancyNumber(ServiceVault.getMoney(player));
            Lang.COMMAND_RESOURCEPOINTS_NOT_ENOUGH_MONEY.sendError(player, "rp", fancyRp, "worth", fancyWorth, "balance", fancyBalance);
            return false;
        } else {
            guild.addResourcePoints(rp);
            if (cp.hasGuild()) {
                GroupResourcePointConvertEvent event = cp.donate(rp);
                if (event.isCancelled()) return false;
                rp = event.getAmount();
            }

            ServiceVault.withdraw(player, worth);
            fancyBalance = StringUtils.toFancyNumber(ServiceVault.getMoney(player));
            Lang.COMMAND_RESOURCEPOINTS_DEPOSIT_SUCCESS.sendMessage(player, "rp", fancyRp, "worth", fancyWorth, "balance", fancyBalance);
            return true;
        }
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            if (guild == null) {
                Lang.NO_GUILD_DEFAULT.sendMessage(player);
            } else {
                Long amount = CommandResourcePoints.validateAmount(true, context, Lang.COMMAND_RESOURCEPOINTS_DEPOSIT_USAGE, 0);
                if (amount != null) handleDeposit(cp, player, guild, amount);
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return CommandResourcePoints.processTabComplete(context);
    }
}
