package fr.krishenk.castel.commands.general.home;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.StandardGuildPermission;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CommandUnsetHome extends CastelCommand {
    public CommandUnsetHome() {
        super("unsethome", true);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (!cp.hasPermission(StandardGuildPermission.SET_HOME)) {
                StandardGuildPermission.SET_HOME.sendDeniedMessage(player);
            } else {
                Guild guild = cp.getGuild();
                Location home = guild.getHome();
                if (home == null) {
                    Lang.COMMAND_UNSETHOME_NOT_SET.sendMessage(player);
                } else if (!guild.setHome(null, cp).isCancelled()) {
                    for (Player member : guild.getOnlineMembers()) {
                        Lang.COMMAND_UNSETHOME_SUCCESS.sendMessage(member, "x", home.getBlockX(), "y", home.getBlockY(), "z", home.getBlockZ());
                    }
                }
            }
        }
    }
}
