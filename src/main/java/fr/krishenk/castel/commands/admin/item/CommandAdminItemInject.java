package fr.krishenk.castel.commands.admin.item;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.managers.ResourcePointManager;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminItemInject extends CastelCommand {
    public CommandAdminItemInject(CastelParentCommand parent) {
        super("inject", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1)) {
            if (!context.isNumber(0)) {
                context.sendError(Lang.INVALID_NUMBER, "arg", context.arg(0));
            } else {
                double worth = Double.parseDouble(context.arg(0));
                Player player = context.senderAsPlayer();
                ItemStack item = player.getInventory().getItemInMainHand();
                if (item.getType() == Material.AIR) {
                    context.sendError(Lang.COMMAND_ADMIN_ITEM_INJECT_NO_ITEM);
                } else {
                    item = ResourcePointManager.injectWorth(item, worth);
                    player.getInventory().setItemInMainHand(item);
                    String name = null;
                    if (item.hasItemMeta()) {
                        ItemMeta meta = item.getItemMeta();
                        if (meta.hasDisplayName()) name = meta.getDisplayName();
                    }

                    if (name == null) name = XMaterial.matchXMaterial(item).toString();

                    context.sendMessage(Lang.COMMAND_ADMIN_ITEM_INJECT_DONE, "rp", StringUtils.toFancyNumber(worth), "item", name);
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? tabComplete("&2<amount>") : emptyTab();
    }
}