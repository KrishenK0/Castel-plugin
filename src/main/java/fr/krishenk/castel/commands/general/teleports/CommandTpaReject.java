package fr.krishenk.castel.commands.general.teleports;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.teleportation.TeleportRequest;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandTpaReject extends CastelCommand {
    public CommandTpaReject() {
        super("tpareject", true);
    }

    @Override
    public void execute(CommandContext context) {
        TeleportRequest request = CommandTpaAccept.getRequest(context, true);
        if (request != null) {
            context.sendMessage(Lang.COMMAND_TPAREJECT_REJECTED);
            context.sendMessage(request.getTeleporter(), Lang.COMMAND_TPAREJECT_REJECTED_NOTIFICATION);
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return CommandTpaAccept.getRequestedPlayers(context);
    }
}
