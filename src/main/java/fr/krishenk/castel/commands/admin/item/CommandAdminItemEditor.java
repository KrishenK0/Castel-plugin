package fr.krishenk.castel.commands.admin.item;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.libs.xseries.XMaterial;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class CommandAdminItemEditor extends CastelCommand {
    public CommandAdminItemEditor(CastelParentCommand parent) {
        super("editor", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (context.assertPlayer()) return CommandResult.FAILED;
        Player player = context.senderAsPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        if (item.getType() == Material.AIR) item = XMaterial.STONE.parseItem();
        else player.getInventory().setItemInMainHand(null);
//        (new ItemEditor(player, item)).openGUI();
        return CommandResult.SUCCESS;
    }
}
