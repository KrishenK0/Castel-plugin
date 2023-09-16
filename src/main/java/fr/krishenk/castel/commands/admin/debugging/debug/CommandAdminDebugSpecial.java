package fr.krishenk.castel.commands.admin.debugging.debug;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.locale.messenger.StaticMessenger;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class CommandAdminDebugSpecial extends CastelCommand {
    public CommandAdminDebugSpecial(CastelParentCommand parent) {
        super("special", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        context.sendMessage(new StaticMessenger("&2Initializing..."));
        // turret ??
        context.sendMessage(new StaticMessenger("&2Done!"));
        return CommandResult.SUCCESS;
    }
}
