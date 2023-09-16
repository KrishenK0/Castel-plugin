package fr.krishenk.castel.commands.general.misc.map;

import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

public class CommandMap extends CastelParentCommand {
    public CommandMap() {
        super("map", true);
        new CommandMapSettings(this);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            if (Config.Map.GUILD_PLAYER_ONLY.getManager().getBoolean() && !cp.hasGuild()) {
                Lang.COMMAND_MAP_GUILD_PLAYER_ONLY.sendMessage(player);
            } else cp.buildMap().display();
        }
    }
}
