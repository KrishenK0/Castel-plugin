package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.CastelPluginPermission;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.commands.TabCompleteManager;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.lands.UnclaimLandEvent;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.claiming.UnclaimProcessor;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;

public class CommandUnclaim extends CastelParentCommand {
    public CommandUnclaim() {
        super("unclaim", true);
        if (!this.isDisabled()) {
            new CommandUnclaimAll(this);
            new CommandUnclaimAuto(this);
        }
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = context.getCastelPlayer();
            SimpleChunkLocation loc;
            if (context.assertArgs(2)) {
                if (!context.hasPermission(CastelPluginPermission.COMMAND_UNCLAIM_CHUNK, true)) {
                    Lang.COMMAND_CLAIM_CHUNK_PERMISSION.sendError(player);
                    return;
                }

                if (!context.isNumber(0)) {
                    context.sendError(Lang.INVALID_NUMBER, "arg", context.arg(0));
                    return;
                }

                if (!context.isNumber(1)) {
                    context.sendError(Lang.INVALID_NUMBER, "arg", context.arg(1));
                    return;
                }

                SimpleChunkLocation.WorldlessWrapper coords = CommandClaim.getChunkCoords(player, context.arg(0), context.arg(1));
                if (coords == null) return;
                loc = coords.inWorld(player.getWorld());
            } else {
                loc = SimpleChunkLocation.of(player.getLocation());
            }

            boolean confirmed = false;
            if (context.argsLengthEquals(1) || context.assertArgs(3)) {
                if (context.argsLengthEquals(1)) confirmed = context.arg(0).equalsIgnoreCase("confirm");
                else confirmed = context.arg(2).equalsIgnoreCase("confirm");
            }

            Guild guild = cp.getGuild();
            UnclaimProcessor processor = (new UnclaimProcessor(loc, cp, guild, confirmed)).process();
            if (!processor.isSuccessful()) {
                processor.sendIssue(context.getSender());
            } else {
                Land land = loc.getLand();
                if (!land.unclaim(cp, UnclaimLandEvent.Reason.UNCLAIMED).isCancelled()) {
                    processor.finalizeRequest();
                    (new LandVisualizer()).forPlayer(player, cp).forLand(land, loc.toChunk()).displayIndicators();
                    Lang.COMMAND_UNCLAIM_SUCCESS.sendMessage(player, "x", loc.getX(), "z", loc.getZ());
                }
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(CommandTabContext context) {
        if (context.isAtArg(0)) {
            List<String> cmds = TabCompleteManager.getSubCommand(context.getSender(), this, context.getArgs());
            cmds.add("confirm");
            if (context.hasPermission(CastelPluginPermission.COMMAND_CLAIM_CHUNK) && cmds.isEmpty() && context.isNumber(0)) {
                cmds.addAll(tabComplete("&2<x>"));
            }
            return cmds;
        } else if (context.isAtArg(1) && !context.arg(0).equals("confirm")) {
            return tabComplete("&2<z>");
        } else {
            return context.isAtArg(2) ? tabComplete("confirm") : emptyTab();
        }
    }
}
