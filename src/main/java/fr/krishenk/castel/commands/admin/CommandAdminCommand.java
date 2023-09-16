package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.general.misc.CommandHelp;
import fr.krishenk.castel.lang.Lang;

public class CommandAdminCommand extends CastelCommand {
    public CommandAdminCommand(CastelParentCommand parent) {
        super("command", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(1)) {
            CommandHelp.showInfoOf(context, context.arg(0), Lang.COMMAND_ADMIN_COMMAND_INFO);
        }
    }
}
