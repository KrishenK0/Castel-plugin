package fr.krishenk.castel.commands.admin.debugging.debug;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.debugging.DebugSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class CommandAdminDebugList extends CastelCommand {
    public CommandAdminDebugList(CastelParentCommand parent) {
        super("list", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (CommandAdminDebug.warnDebugNotEnabled(context)) return CommandResult.FAILED;
        DebugSettings settings = DebugSettings.getSettings(context);
        settings.setWhitelist(!settings.isWhitelist());
        context.sendMessage(settings.isWhitelist() ? Lang.COMMAND_ADMIN_DEBUG_LIST_WHITELIST : Lang.COMMAND_ADMIN_DEBUG_LIST_BLACKLIST);
        return CommandResult.SUCCESS;
    }
}
