package fr.krishenk.castel.commands.admin.debugging;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminHologram extends CastelCommand {
    public CommandAdminHologram(CastelParentCommand parent) {
        super("hologram", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1)) {
            int removed = 0;
            Double radius = context.getDouble(0);
            if (radius != null) {
                Player player = context.senderAsPlayer();
                for (Entity entity : player.getNearbyEntities(radius, radius, radius)) {
                    if (entity.getType() == EntityType.ARMOR_STAND) {
                        ArmorStand armorStand = (ArmorStand) entity;
                        if (!armorStand.isVisible() && armorStand.isMarker()) {
                            armorStand.remove();
                            ++removed;
                        }
                    }
                }

                Lang.COMMAND_ADMIN_HOLOGRAM_REMOVED.sendMessage(player, "removed", removed);
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? tabComplete("<radius>") : emptyTab();
    }
}
