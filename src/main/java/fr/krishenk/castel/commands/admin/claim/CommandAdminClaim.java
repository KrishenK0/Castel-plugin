package fr.krishenk.castel.commands.admin.claim;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.lands.ClaimLandEvent;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandAdminClaim extends CastelCommand {
    public CommandAdminClaim(CastelParentCommand parent) {
        super("claim", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1)) {
            Guild guild = context.getGuild(0);
            if (guild != null) {
                Player player = context.senderAsPlayer();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                SimpleChunkLocation chunk = SimpleChunkLocation.of(player.getLocation());
                Land land = chunk.getLand();
                if (land != null) {
                    Guild landOwner = land.getGuild();
                    if (landOwner != null) {
                        context.getSettings().withContext(landOwner);
                        context.fail(Lang.COMMAND_ADMIN_CLAIM_ALREADY_CLAIMED);
                        return;
                    }
                }

                if (!guild.claim(chunk, cp, ClaimLandEvent.Reason.ADMIN).isCancelled()) {
                    Lang.COMMAND_ADMIN_CLAIM_SUCCESS.sendMessage(player, "x", chunk.getX(), "z", chunk.getZ(), "guild", guild.getName());
                    (new LandVisualizer()).forPlayer(player, cp).forLand(land, chunk.toChunk()).display(true);
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isPlayer() && context.argsLengthEquals(1) ? context.getGuilds(0) : emptyTab();
    }
}
