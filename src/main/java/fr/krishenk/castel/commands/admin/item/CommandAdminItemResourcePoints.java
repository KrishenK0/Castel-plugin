package fr.krishenk.castel.commands.admin.item;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XItemStack;
import fr.krishenk.castel.managers.ResourcePointManager;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminItemResourcePoints extends CastelCommand {
    public CommandAdminItemResourcePoints(CastelParentCommand parent) {
        super("resourcepoints", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(1)) {
            if (!Config.ResourcePoints.CUSTOM_ITEMS.getManager().getSectionKeys().contains(context.arg(0))) {
                context.sendError(Lang.COMMAND_ADMIN_ITEM_RESOURCEPOINTS_UNKNOWN_ITEM, "item", context.arg(0));
            } else {
                int amount = 1;
                if (context.assertArgs(2)) {
                    if (!context.isNumber(1)) {
                        context.sendError(Lang.INVALID_NUMBER, "arg", context.arg(1));
                        return;
                    }

                    amount = Integer.parseInt(context.arg(1));
                    if (amount < 1) {
                        context.sendError(Lang.COMMAND_ADMIN_ITEM_RESOURCEPOINTS_INVALID_AMOUNT, "amount", amount);
                        return;
                    }

                    Player target;
                    if (context.assertArgs(3)) {
                        target = context.getPlayer(2);
                        if (target == null) return;
                    } else {
                        if (!context.isPlayer()) {
                            context.sendError(Lang.COMMAND_ADMIN_ITEM_INJECT_USAGE);
                            return;
                        }

                        target = context.senderAsPlayer();
                    }

                    Pair<ItemStack, Double> item = ResourcePointManager.buildItem(context.arg(0));
                    item.getKey().setAmount(amount);
                    XItemStack.giveOrDrop(target, item.getKey());
                    context.sendMessage(Lang.COMMAND_ADMIN_ITEM_RESOURCEPOINTS_DONE, "amount", amount, "item", context.arg(0), "rp", item.getValue(), "target", target.getName());
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) return tabComplete(Config.ResourcePoints.CUSTOM_ITEMS.getManager().getSectionKeys());
        if (context.isAtArg(1)) return tabComplete("&9[amount]");
        return context.isAtArg(2) ? context.getPlayers(2) : emptyTab();
    }
}
