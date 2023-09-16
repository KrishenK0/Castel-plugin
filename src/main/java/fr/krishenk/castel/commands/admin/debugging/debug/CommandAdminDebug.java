package fr.krishenk.castel.commands.admin.debugging.debug;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.lang.Lang;

public class CommandAdminDebug extends CastelParentCommand {
    public CommandAdminDebug(CastelParentCommand parent) {
        super("debug", parent);
      new CommandAdminDebugToggle(this);
      new CommandAdminDebugList(this);
      new CommandAdminDebugStacktrace(this);
      new CommandAdminDebugSpecial(this);
    }

    public static boolean warnDebugNotEnabled(CommandContext context) {
        if (CLogger.isDebugging()) return false;
        context.sendError(Lang.COMMAND_ADMIN_DEBUG_NOT_ENABLED);
        return true;
    }
}
