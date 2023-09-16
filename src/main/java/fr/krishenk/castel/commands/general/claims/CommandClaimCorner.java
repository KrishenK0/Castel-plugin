package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.commands.CommandTabContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.claiming.AbstractClaimProcessor;
import fr.krishenk.castel.managers.land.claiming.ClaimClipboard;
import fr.krishenk.castel.utils.internal.Fn;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class CommandClaimCorner extends CastelCommand {
    private static final Map<UUID, Pair<SimpleLocation, SimpleLocation>> CORNERS = new HashMap<>();

    public CommandClaimCorner(CastelParentCommand parent) {
        super("corner", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer() && !context.requireArgs(1) && !context.assertHasGuild()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = context.getCastelPlayer();
            Guild guild = cp.getGuild();
            Pair<SimpleLocation, SimpleLocation> corners = CORNERS.get(player.getUniqueId());
            boolean exists = corners != null;
            if (!exists) {
                corners = Pair.empty();
            }

            String arg = context.arg(0);
            boolean first;
            if (arg.equalsIgnoreCase("pos1")) {
                first = true;
            } else {
                if (!arg.equalsIgnoreCase("pos2")) {
                    context.wrongUsage();
                    return;
                }

                first = false;
            }

            SimpleLocation currentLocation = SimpleLocation.of(player.getLocation());
            SimpleLocation pos1;
            SimpleLocation pos2;
            if (first) {
                pos1 = currentLocation;
                pos2 = corners.getValue();
            } else {
                pos1 =  corners.getKey();
                pos2 = currentLocation;
            }

            corners.setKey(pos1);
            corners.setValue(pos2);
            if (!exists) CORNERS.put(player.getUniqueId(), corners);

            SimpleChunkLocation pos2Chunk;
            if (pos1 != null && pos2 != null) {
                Map<SimpleChunkLocation.WorldlessWrapper, ClaimClipboard.ClaimProcessor> claims = CommandClaimSquare.claimSquare(pos1.toSimpleChunkLocation(), pos2.toSimpleChunkLocation(), cp, guild, false);
                ClaimClipboard.addClipboard(player, new Clipboard((Map)Fn.cast(claims), player.getWorld(), corners));
                cp.buildMap().clipboardMode().display();
                pos2Chunk = first ? pos1.toSimpleChunkLocation() : pos2.toSimpleChunkLocation();
                context.sendMessage(first ? Lang.COMMAND_CLAIM_CORNER_SET_POS1 : Lang.COMMAND_CLAIM_CORNER_SET_POS2, "x1", pos1.getX(), "y1", pos1.getY(), "z1", pos1.getZ(), "chunkX1", pos2Chunk.getX(), "chunkZ1", pos2Chunk.getZ(), "x2", pos2.getX(), "y2", pos2.getY(), "z2", pos2.getZ(), "chunkX2", pos2Chunk.getX(), "chunkZ2", pos2Chunk.getZ());
            } else {
                SimpleChunkLocation pos1Chunk = pos1 == null ? null : pos1.toSimpleChunkLocation();
                pos2Chunk = pos2 == null ? null : pos2.toSimpleChunkLocation();
                context.sendMessage(first ? Lang.COMMAND_CLAIM_CORNER_SET_POS1 : Lang.COMMAND_CLAIM_CORNER_SET_POS2, "x1", pos1 == null ? 126 : pos1.getX(), "y1", pos1 == null ? 126 : pos1.getY(), "z1", pos1 == null ? 126 : pos1.getZ(), "chunkX1", pos1Chunk == null ? 126 : pos1Chunk.getX(), "chunkZ1", pos1Chunk == null ? 126 : pos1Chunk.getZ(), "x2", pos2 == null ? 126 : pos2.getX(), "y2", pos2 == null ? 126 : pos2.getY(), "z2", pos2 == null ? 126 : pos2.getZ(), "chunkX2", pos2Chunk == null ? 126 : pos2Chunk.getX(), "chunkZ2", pos2Chunk == null ? 126 : pos2Chunk.getZ());
            }
        }
    }

    @Override
    public @NonNull List<String> tabComplete(@NonNull CommandTabContext context) {
        return context.isAtArg(0) ? tabComplete("post1", "pos2") : emptyTab();
    }

    public static class Clipboard extends ClaimClipboard {
        private final Pair<SimpleLocation, SimpleLocation> corners;

        public Clipboard(Map<SimpleChunkLocation.WorldlessWrapper, AbstractClaimProcessor> claims, World world, Pair<SimpleLocation, SimpleLocation> corners) {
            super(claims, world);
            this.corners = corners;
        }

        public Pair<SimpleLocation, SimpleLocation> getCorners() {
            return corners;
        }
    }
}
