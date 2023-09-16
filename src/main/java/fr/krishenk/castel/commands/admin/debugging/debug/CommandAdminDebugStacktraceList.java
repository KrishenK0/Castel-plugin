package fr.krishenk.castel.commands.admin.debugging.debug;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.debugging.StacktraceSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class CommandAdminDebugStacktraceList extends CastelCommand {
    public CommandAdminDebugStacktraceList(CastelParentCommand parent) {
        super("list", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (CommandAdminDebug.warnDebugNotEnabled(context)) return CommandResult.FAILED;
        StacktraceSettings.isWhitelist = !StacktraceSettings.isWhitelist;
        context.sendMessage(StacktraceSettings.isWhitelist ? Lang.COMMAND_ADMIN_DEBUG_STACKTRACE_ENABLED : Lang.COMMAND_ADMIN_DEBUG_STACKTRACE_DISABLED);
        return CommandResult.SUCCESS;
    }
}
