package fr.krishenk.castel.commands.general.visualizer;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import org.bukkit.entity.Player;

public class CommandVisualizeToggle extends CastelCommand {
    public CommandVisualizeToggle(CastelParentCommand parent) {
        super("toggle", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = context.getCastelPlayer();
            boolean using = cp.isUsingMarkers();
            cp.setUsingMarkers(!using);
            if (using) {
                context.sendMessage(Lang.COMMAND_VISUALIZE_TOGGLE_DISABLED);
                LandVisualizer.removeVisualizers(player, true);
            } else {
                context.sendMessage(Lang.COMMAND_VISUALIZE_TOGGLE_ENABLED);
                (new LandVisualizer()).forPlayer(player, cp).forLand(Land.getLand(player.getLocation()), player.getLocation().getChunk()).display(true);
            }
        }
    }
}
