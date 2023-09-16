package fr.krishenk.castel.commands.admin.debugging.debug;

import fr.krishenk.castel.commands.CastelParentCommand;

public class CommandAdminDebugStacktrace extends CastelParentCommand {
    public CommandAdminDebugStacktrace(CastelParentCommand parent) {
        super("stacktrace", parent);
        new CommandAdminDebugStacktraceList(this);
        new CommandAdminDebugStacktraceToggle(this);
    }
}
