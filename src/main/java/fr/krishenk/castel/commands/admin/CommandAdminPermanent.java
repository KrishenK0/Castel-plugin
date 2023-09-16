package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.lang.Lang;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminPermanent extends CastelCommand {
    public CommandAdminPermanent(CastelParentCommand parent) {
        super("permanent", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(1)) {
            Guild guild = context.getGuild(0);
            if (guild != null) {
                guild.setPermanent(!guild.isPermanent());
                if (guild.isPermanent())
                    context.sendMessage(Lang.COMMAND_ADMIN_PERMANENT_ON, "guild", guild.getName());
                else
                    context.sendMessage(Lang.COMMAND_ADMIN_PERMANENT_OFF, "guild", guild.getName());
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? context.getGuilds(0) : emptyTab();
    }
}
