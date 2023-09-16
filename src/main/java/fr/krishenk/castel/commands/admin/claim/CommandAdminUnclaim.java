package fr.krishenk.castel.commands.admin.claim;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.entity.Player;

public class CommandAdminUnclaim extends CastelCommand {
    public CommandAdminUnclaim(CastelParentCommand parent) {
        super("unclaim", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            Land land = Land.getLand(player.getLocation());
            if (land != null && land.isClaimed()) {
                Guild guild = land.getGuild();
                if (!land.unclaim(CastelPlayer.getCastelPlayer(player), UnclaimLandEvent.Reason.ADMIN).isCancelled()) {
                    SimpleChunkLocation chunk = land.getLocation();
                    Lang.COMMAND_ADMIN_UNCLAIM_SUCCESS.sendMessage(player, "x", chunk.getX(), "z", chunk.getZ(), "guild", guild.getName());
                }
            } else Lang.COMMAND_ADMIN_UNCLAIM_NOT_CLAIMED.sendMessage(player);
        }
    }
}
