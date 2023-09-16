package fr.krishenk.castel.commands.general.resourcepoints;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.events.general.GroupResourcePointConvertEvent;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.services.ServiceVault;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandResourcePointsWithdraw extends CastelCommand {
    public CommandResourcePointsWithdraw(CastelParentCommand parent) {
        super("withdraw", parent);
    }

    public static boolean handleWithdraw(CastelPlayer cp, Player player, Guild guild, long rp) {
        if (cp.hasGuild() && !cp.hasPermission(StandardGuildPermission.WITHDRAW)) {
            StandardGuildPermission.WITHDRAW.sendDeniedMessage(player);
            return false;
        } else if (!guild.hasResourcePoints(rp)) {
            String fancyRp = StringUtils.toFancyNumber(rp);
            Lang.COMMAND_RESOURCEPOINTS_NOT_ENOUGH_RESOURCE_POINTS.sendMessage(player, "rp", fancyRp);
            return false;
        } else {
            GroupResourcePointConvertEvent event = cp.donate(guild, -rp);
            if (event.isCancelled()) return false;
            rp = -event.getAmount();
            long min = Config.ECONOMY_RESOURCE_POINTS_MIN_WITHDRAW.getLong();
            if (rp < min) {
                String fancyMinRp = StringUtils.toFancyNumber(min);
                Lang.COMMAND_RESOURCEPOINTS_WITHDRAW_MIN.sendError(player, "min", fancyMinRp);
                return false;
            } else {
                guild.addResourcePoints(-rp);
                double worth = rp * Config.ECONOMY_RESOURCE_POINTS_WORTH.getDouble();
                ServiceVault.deposit(player, worth);
                String fancyRp = StringUtils.toFancyNumber(rp);
                String fancyWorth = StringUtils.toFancyNumber(worth);
                String fancyBalance = StringUtils.toFancyNumber(ServiceVault.getMoney(player));
                Lang.COMMAND_RESOURCEPOINTS_WITHDRAW_SUCCESS.sendMessage(player, "rp", fancyRp, "worth", fancyWorth, "balance", fancyBalance);
                return true;
            }
        }
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            if (guild == null) Lang.NO_GUILD_DEFAULT.sendMessage(player);
            else {
                Long amount = CommandResourcePoints.validateAmount(true, context, Lang.COMMAND_RESOURCEPOINTS_WITHDRAW_USAGE, 0);
                if (amount != null) handleWithdraw(cp, player, guild, amount);
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return CommandResourcePoints.processTabComplete(context);
    }
}
