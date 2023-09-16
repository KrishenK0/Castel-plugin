package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

public class CommandAdminToggle extends CastelCommand {
    public CommandAdminToggle(CastelParentCommand parent) {
        super("toggle", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            boolean toggle = cp.isAdmin();
            cp.setAdmin(!toggle);
            if (toggle) Lang.COMMAND_ADMIN_TOGGLE_OFF.sendMessage(player);
            else Lang.COMMAND_ADMIN_TOGGLE_ON.sendMessage(player);
        }
    }
}
