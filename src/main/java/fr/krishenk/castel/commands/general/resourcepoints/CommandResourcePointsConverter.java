package fr.krishenk.castel.commands.general.resourcepoints;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

public class CommandResourcePointsConverter extends CastelCommand {
    public CommandResourcePointsConverter(CastelParentCommand parent) {
        super("converter", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            if (guild == null) context.sendError(Lang.NO_GUILD_DEFAULT);
            else {
                // TODO : Implement converter
            }
        }
    }
}
