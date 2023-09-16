package fr.krishenk.castel.commands.general.visualizer;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import org.bukkit.entity.Player;

public class CommandVisualizePermanent extends CastelCommand {
    public CommandVisualizePermanent(CastelParentCommand parent) {
        super("permanent", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            if (LandVisualizer.getPermanent().remove(player.getUniqueId())) {
                Lang.COMMAND_VISUALIZE_PERMANENT_DISABLED.sendMessage(player);
                LandVisualizer.removeVisualizers(player, true);
            } else {
                Lang.COMMAND_VISUALIZE_PERMANENT_ENABLED.sendMessage(player);
                LandVisualizer.getPermanent().add(player.getUniqueId());
                (new LandVisualizer()).forPlayer(player, CastelPlayer.getCastelPlayer(player)).forLand(Land.getLand(player.getLocation()), player.getLocation().getChunk()).display(true);
            }
        }
    }
}
