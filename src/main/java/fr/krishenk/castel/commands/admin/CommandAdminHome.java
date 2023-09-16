package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminHome extends CastelCommand {
    public CommandAdminHome(CastelParentCommand parent) {
        super("home", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            if (!context.assertArgs(1)) context.sendError(Lang.COMMAND_ADMIN_HOME_USAGE);
            else {
                Guild guild = context.getGuild(0);
                if (guild != null) {
                    Location home = guild.getHome();
                    if (home == null)
                        context.sendError(Lang.COMMAND_ADMIN_HOME_HOMELESS, "guild", guild.getName());
                    else {
                        Player player = context.senderAsPlayer();
                        player.teleport(home);
                        context.sendMessage(Lang.COMMAND_ADMIN_HOME_TELEPORTED, "guild", guild.getName());
                    }
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? context.getGuilds(0) : emptyTab();
    }
}
