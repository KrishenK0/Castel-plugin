package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.lang.Lang;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminRename extends CastelCommand {
    public CommandAdminRename(CastelParentCommand parent) {
        super("rename", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertArgs(2)) context.sendError(Lang.COMMAND_ADMIN_RENAME_USAGE);
        else {
            Guild guild = context.getGuild(0);
            if (guild != null) {
                String name = context.arg(1);
                Guild renameGuild = Guild.getGuild(name);
                if (renameGuild != null) {
                    context.sendError(Lang.COMMAND_ADMIN_RENAME_ALREADY_EXISTS);
                } else if (!guild.rename(name, null).isCancelled()) {
                    context.sendMessage(Lang.COMMAND_ADMIN_RENAME_RENAMED, "guild", guild.getName(), "name", name);
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        if (context.isAtArg(0)) return context.getGuilds(0);
        return context.isAtArg(1) ? tabComplete("&2<name>") : emptyTab();
    }
}
