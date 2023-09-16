package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.constants.player.ClaimingHistory;
import fr.krishenk.castel.events.lands.ClaimLandEvent;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.lang.Lang;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

public class CommandUndo extends CastelCommand {
    public CommandUndo() {
        super("undo", true);
    }

    protected static void landHistoryCommons(CommandContext context, boolean undo) {
        if (!context.assertPlayer() && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
            Guild guild = cp.getGuild();
            ClaimingHistory history = cp.claimingHistory(undo);
            if (history == null) {
                context.sendError(undo ? Lang.COMMAND_UNDO_NO_HISTORY : Lang.COMMAND_REDO_NO_HISTORY);
            } else {
                boolean wasClaimed = history.wasClaimed();
                Set<SimpleChunkLocation> finalLands = new HashSet<>(history.getClaims().size());
                for (SimpleChunkLocation loc : history.getClaims()) {
                    Land land = Land.getLand(loc);
                    boolean isClaimed = land != null && land.isClaimed();
                    context.var("x", loc.getX()).var("z", loc.getZ());
                    if (isClaimed) {
                        context.var("claimer", Bukkit.getOfflinePlayer(land.getClaimedBy()).getName());
                    }

                    if (undo) {
                        if (wasClaimed)  {
                            if (isClaimed) {
                                if (player.getUniqueId().equals(land.getClaimedBy())) {
                                    finalLands.add(loc);
                                    context.sendMessage(Lang.COMMAND_UNDO_UNCLAIMED);
                                } else context.sendError(Lang.COMMAND_UNDO_CLAIMED_NOT_OWNED);
                            } else context.sendError(Lang.COMMAND_UNDO_ALREADY_UNCLAIMED);
                        } else if (isClaimed) {
                            context.sendError(Lang.COMMAND_UNDO_ALREADY_CLAIMED);
                        } else {
                            context.sendMessage(Lang.COMMAND_UNDO_CLAIMED);
                        }
                    } else if (wasClaimed) {
                        if (isClaimed) {
                            context.sendError(Lang.COMMAND_REDO_ALREADY_CLAIMED);
                        } else {
                            finalLands.add(loc);
                            context.sendMessage(Lang.COMMAND_UNDO_CLAIMED);
                        }
                    } else if (isClaimed) {
                        context.sendMessage(Lang.COMMAND_UNDO_UNCLAIMED);
                    } else {
                        context.sendError(Lang.COMMAND_REDO_ALREADY_UNCLAIMED);
                    }
                }

                if (undo) guild.unclaim(finalLands, cp, UnclaimLandEvent.Reason.CLIPBOARD, false);
                else guild.claim(finalLands, cp, ClaimLandEvent.Reason.CLIPBOARD, false);
            }
        }
    }

    @Override
    public void execute(CommandContext context) {
        landHistoryCommons(context, true);
    }
}
