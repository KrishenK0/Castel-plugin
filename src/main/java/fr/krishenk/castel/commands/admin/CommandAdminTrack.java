package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.JoinAndLeaveManager;
import fr.krishenk.castel.utils.internal.integer.IntHashSet;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CommandAdminTrack extends CastelCommand {
    private static final IntHashSet TRACKERS = new IntHashSet();

    @Override
    @NotNull
    public CommandResult executeX(@NotNull CommandContext context) {
        int id = context.isPlayer() ? context.senderAsPlayer().getEntityId() : 0;
        if (TRACKERS.contains(id)) {
            TRACKERS.remove(id);
            context.sendMessage(Lang.COMMAND_ADMIN_TRACK_DISABLED);
        } else {
            context.sendMessage(Lang.COMMAND_ADMIN_TRACK_ENABLED);
            TRACKERS.add(id);
        }
        return CommandResult.SUCCESS;
    }

    public CommandAdminTrack(@NotNull CastelParentCommand parent) {
        super("track", parent);
    }

    public static boolean isTracking(@NotNull CommandSender sender) {
        return TRACKERS.contains(sender instanceof Player ? ((Player)sender).getEntityId() : 0);
    }

    static {
        JoinAndLeaveManager.LEAVE_HANDLERS.add(player -> TRACKERS.remove(player.getEntityId()));
    }
}

