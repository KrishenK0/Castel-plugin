package fr.krishenk.castel.commands.admin.debugging.debug;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.debugging.DebugNS;
import fr.krishenk.castel.utils.debugging.StacktraceSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class CommandAdminDebugStacktraceToggle extends CastelCommand {
    public CommandAdminDebugStacktraceToggle(CastelParentCommand parent) {
        super("toggle", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        if (CommandAdminDebug.warnDebugNotEnabled(context)) return CommandResult.FAILED;
        for (String arg : context.args) {
            DebugNS ns = DebugNS.fromString(arg);
            context.var("debug", arg);
            if (ns == null) context.sendError(Lang.COMMAND_ADMIN_DEBUG_UNKNOWN_DEBUG);
            else if (StacktraceSettings.list.add(ns)) context.sendMessage(Lang.COMMAND_ADMIN_DEBUG_STACKTRACE_ADDED);
            else {
                StacktraceSettings.list.remove(ns);
                context.sendMessage(Lang.COMMAND_ADMIN_DEBUG_STACKTRACE_REMOVED);
            }
        }
        return CommandResult.SUCCESS;
    }
}
