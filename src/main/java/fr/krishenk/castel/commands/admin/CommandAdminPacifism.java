package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.lang.Lang;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminPacifism extends CastelCommand {
    public CommandAdminPacifism(CastelParentCommand parent) {
        super("pacifism", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.requireArgs(1)) {
            Guild guild = context.getGuild(0);
            if (guild != null) {
                boolean pacifist;
                if (context.assertArgs(2)) {
                    Boolean bool = context.parseBool(1);
                    if (bool == null) return;
                    pacifist = bool;
                } else pacifist = !guild.isPacifist();

                guild.setPacifist(pacifist);
                context.sendMessage(pacifist ? Lang.COMMAND_ADMIN_PACIFISM_ENABLED : Lang.COMMAND_ADMIN_PACIFISM_DISABLED, "guild", guild.getName());
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? context.getGuilds(0) : emptyTab();
    }
}
