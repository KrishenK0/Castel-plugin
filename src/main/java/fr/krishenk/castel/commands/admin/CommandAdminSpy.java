package fr.krishenk.castel.commands.admin;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

public class CommandAdminSpy extends CastelCommand {
    public CommandAdminSpy(CastelParentCommand parent) {
        super("spy", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (cp.isSpy()) Lang.COMMAND_ADMIN_SPY_OFF.sendMessage(player);
            else Lang.COMMAND_ADMIN_SPY_ON.sendMessage(player);

            cp.toggleSpy();
        }
    }
}
