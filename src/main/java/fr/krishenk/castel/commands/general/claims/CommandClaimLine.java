package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.commands.*;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.claiming.AbstractClaimProcessor;
import fr.krishenk.castel.managers.land.claiming.ClaimClipboard;
import fr.krishenk.castel.managers.land.claiming.UnclaimProcessor;
import fr.krishenk.castel.utils.Compass;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CommandClaimLine extends CastelCommand {
    public CommandClaimLine(CastelParentCommand parent) {
        super("line", parent);
    }

    public static <T> Map<SimpleChunkLocation.WorldlessWrapper, T> claimLine(SimpleChunkLocation chunk, Compass direction, int distance, Function<SimpleChunkLocation, T> transformer) {
        Map<SimpleChunkLocation.WorldlessWrapper, T> claims = new HashMap<>(distance);
        BlockFace facing = direction.toBlockFace();

        for(int i = 0; i <= distance; ++i) {
            chunk = chunk.getRelative(facing.getModX(), facing.getModZ());
            claims.put(chunk.worldlessWrapper(), transformer.apply(chunk));
        }

        return claims;
    }

    public CommandResult executeX(CommandContext context) {
        return commons(context, true);
    }

    public static CommandResult commons(CommandContext context, boolean claiming) {
        if (context.assertPlayer() || context.requireArgs(1) || context.assertHasGuild()) {
            return CommandResult.FAILED;
        } else if (!context.isNumber(0)) {
            context.sendError(Lang.INVALID_NUMBER, "arg", context.arg(0));
            return CommandResult.FAILED;
        } else {
            int maxDistance = Config.Claims.LINE_MAX_DISTANCE.getManager().getInt();
            int distance = context.intArg(0);
            if (distance < 2) {
                context.sendError(Lang.COMMAND_CLAIM_LINE_MIN_DISTANCE, "min", 2, "distance", distance);
                return CommandResult.FAILED;
            } else if (distance > maxDistance) {
                context.sendError(Lang.COMMAND_CLAIM_LINE_MAX_DISTANCE, "max", maxDistance, "distance", distance);
                return CommandResult.FAILED;
            } else {
                Player player = context.senderAsPlayer();
                CastelPlayer cp = context.getCastelPlayer();
                Guild guild = cp.getGuild();
                Compass facing;
                if (context.assertArgs(2)) {
                    String direction = context.joinArgs("_", 1);
                    Optional<Compass> found = Compass.getCardinalDirection(cp, direction.toLowerCase());
                    if (!found.isPresent()) {
                        context.sendError(Lang.COMMAND_CLAIM_LINE_INVALID_DIRECTION, "direction", direction);
                        return CommandResult.FAILED;
                    }

                    facing = found.get();
                } else {
                    facing = Compass.getCardinalDirection(player);
                }

                Bukkit.getScheduler().runTaskAsynchronously(context.getPlugin(), () -> {
                    Map<SimpleChunkLocation.WorldlessWrapper, AbstractClaimProcessor> claims = claimLine(SimpleChunkLocation.of(player.getLocation()), facing, distance, (chunk) -> {
                        return claiming ? new ClaimClipboard.ClaimProcessor(chunk, cp, guild) : new UnclaimProcessor(chunk, cp, guild, true);
                    });
                    ClaimClipboard.addClipboard(player, new CommandClaimLine.Clipboard(player.getWorld(), claims, facing));
                    cp.buildMap().clipboardMode().display();
                    context.var("direction", facing.getLanguage());
                    context.var("lands", claims.size());
                    context.sendMessage(claiming ? Lang.COMMAND_CLAIM_LINE_DONE : Lang.COMMAND_UNCLAIM_LINE_DONE);
                });
                return CommandResult.SUCCESS;
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(CommandTabContext context) {
        if (context.isAtArg(0)) {
            return tabComplete("<distance>");
        } else if (context.isAtArg(1)) {
            List<String> directions = Arrays.stream(Compass.CARDINAL_DIRECTIONS).map((x) -> x.getLanguage().parse(context.getSender())).collect(Collectors.toList());
            return context.suggest(1, directions);
        } else {
            return emptyTab();
        }
    }

    public static final class Clipboard extends ClaimClipboard {
        private final Compass direction;

        public Clipboard(World world, Map<SimpleChunkLocation.WorldlessWrapper, AbstractClaimProcessor> claims, Compass direction) {
            super(claims, world);
            this.direction = direction;
        }

        public Compass getDirection() {
            return direction;
        }
    }
}
