package fr.krishenk.castel.commands.admin.debugging.debug;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandResult;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.utils.debugging.DebugNS;
import fr.krishenk.castel.utils.debugging.DebugSettings;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

public class CommandAdminDebugToggle extends CastelCommand {
    public CommandAdminDebugToggle(CastelParentCommand parent) {
        super("toggle", parent);
    }

    @Override
    public @NotNull CommandResult executeX(@NonNull CommandContext context) {
        DebugSettings settings = DebugSettings.getSettings(context);
        if (CommandAdminDebug.warnDebugNotEnabled(context)) return CommandResult.FAILED;
        for (String arg : context.args) {
            DebugNS ns = DebugNS.fromString(arg);
            context.var("debug", arg);
            if (ns == null) context.sendError(Lang.COMMAND_ADMIN_DEBUG_UNKNOWN_DEBUG);
            else if (settings.getList().add(ns)) context.sendMessage(Lang.COMMAND_ADMIN_DEBUG_TOGGLE_ADDED);
            else {
                settings.getList().remove(ns);
                context.sendMessage(Lang.COMMAND_ADMIN_DEBUG_TOGGLE_REMOVED);
            }
        }
        return CommandResult.SUCCESS;
    }
}
