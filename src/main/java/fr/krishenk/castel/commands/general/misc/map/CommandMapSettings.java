package fr.krishenk.castel.commands.general.misc.map;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionDefault;

public class CommandMapSettings extends CastelCommand {
    public CommandMapSettings(CastelParentCommand parent) {
        super("settings", parent, PermissionDefault.TRUE);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = context.getCastelPlayer();
            // TODO : Implement settings
        }
    }
}
