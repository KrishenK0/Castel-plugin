package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.claiming.AbstractClaimProcessor;
import fr.krishenk.castel.managers.land.claiming.ClaimClipboard;
import fr.krishenk.castel.managers.land.claiming.ClaimProcessor;
import fr.krishenk.castel.utils.internal.Fn;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandClaimSquare extends CastelCommand {
    public CommandClaimSquare(CastelParentCommand parent) {
        super("square", parent);
    }

    public static Pair<SimpleChunkLocation, SimpleChunkLocation> getCenteredSquareCorners(SimpleChunkLocation chunk, int radius) {
        int plus = radius % 2;
        radius /= 2;
        return Pair.of(chunk.getRelative(-radius, -radius), chunk.getRelative(radius + plus, radius + plus));
    }

    public static Map<SimpleChunkLocation.WorldlessWrapper, ClaimClipboard.ClaimProcessor> claimSquare(SimpleChunkLocation from, SimpleChunkLocation to, CastelPlayer cp, Guild guild, boolean edgesOnly) {
        if (!from.getWorld().equals(to.getWorld()))
            throw new IllegalArgumentException("Cannot claim a square in two unrelated worlds: " + from.getWorld() + " - " + to.getWorld());
        Map<SimpleChunkLocation.WorldlessWrapper, ClaimClipboard.ClaimProcessor> claims = new HashMap<>();
        boolean incrementX = from.getX() < to.getX();
        boolean incrementZ = from.getZ() < to.getZ();
        int minX = Math.min(from.getX(), to.getX());
        int maxX = Math.max(from.getX(), to.getX()) - 1;
        int minZ = Math.min(from.getZ(), to.getZ());
        int maxZ = Math.max(from.getZ(), to.getZ()) - 1;

        for(int x = from.getX(); x != to.getX(); x += incrementX ? 1 : -1) {
            for(int z = from.getZ(); z != to.getZ(); z += incrementZ ? 1 : -1) {
                if (!edgesOnly || x == minX || x == maxX || z == minZ || z == maxZ) {
                    SimpleChunkLocation current = new SimpleChunkLocation(from.getWorld(), x, z);
                    claims.put(current.worldlessWrapper(), new ClaimClipboard.ClaimProcessor(current, cp, guild));
                }
            }
        }

        return claims;
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1) && !context.assertHasGuild()) {
            int maxRadius = Config.Claims.SQUARE_MAX_RADIUS.getManager().getInt();
            context.var("max", maxRadius).var("radius", context.arg(0));
            if (!context.isNumber(0)) {
                context.sendError(Lang.COMMAND_CLAIM_SQUARE_RADIUS_INVALID);
            } else {
                Player player = context.senderAsPlayer();
                CastelPlayer cp = context.getCastelPlayer();
                int radius = context.intArg(0);
                if (cp.isAdmin() || radius >= 2 && radius <= maxRadius) {
                    boolean edgesOnly = context.assertArgs(2) && context.parseBool(1);
                    Guild guild = cp.getGuild();
                    Lang constantIssue = ClaimProcessor.checkWorldAndPermission(player.getWorld().getName(), cp);
                    if (constantIssue != null) {
                        context.sendError(constantIssue);
                    } else {
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                            SimpleChunkLocation chunk = SimpleChunkLocation.of(player.getLocation());
                            Pair<SimpleChunkLocation, SimpleChunkLocation> corners = getCenteredSquareCorners(chunk, radius);
                            Map<SimpleChunkLocation.WorldlessWrapper, ClaimClipboard.ClaimProcessor> result = claimSquare(corners.getKey(), corners.getValue(), cp, guild, edgesOnly);
                            ClaimClipboard.addClipboard(player, new Clipboard((Map)Fn.cast(result), player.getWorld(), radius));
                            cp.buildMap().clipboardMode().display();
                            Lang.COMMAND_CLAIM_SQUARE_DONE.sendMessage(player);
                        });
                    }
                } else context.sendError(Lang.COMMAND_CLAIM_SQUARE_RADIUS_DISALLOWED);
            }
        }
    }

    @Override
    public List<String> tabComplete(@NonNull CommandSender sender, @NotNull @NonNull String[] args) {
        return args.length == 1 ? tabComplete("&2<radius>") : emptyTab();
    }

    public static class Clipboard extends ClaimClipboard {
        private final int radius;

        public Clipboard(Map<SimpleChunkLocation.WorldlessWrapper, AbstractClaimProcessor> claims, World world, int radius) {
            super(claims, world);
            this.radius = radius;
        }

        public int getRadius() {
            return radius;
        }
    }
}
