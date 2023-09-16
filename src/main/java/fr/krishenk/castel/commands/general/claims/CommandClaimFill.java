package fr.krishenk.castel.commands.general.claims;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.managers.land.claiming.AbstractClaimProcessor;
import fr.krishenk.castel.managers.land.claiming.ClaimClipboard;
import fr.krishenk.castel.utils.internal.Fn;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

public class CommandClaimFill extends CastelCommand {
    public CommandClaimFill(CastelParentCommand parent) {
        super("fill", parent);
    }

    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            if (!context.assertHasGuild()) {
                Player player = context.senderAsPlayer();
                CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
                Guild guild = cp.getGuild();
                SimpleChunkLocation chunk = SimpleChunkLocation.of(player.getLocation());
                Land masterLand = chunk.getLand();
                if (masterLand != null && masterLand.isClaimed()) {
                    Lang.COMMAND_CLAIM_FILL_IN_CLAIMED_LAND.sendMessage(player);
                } else {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                        CommandClaimFill.FloodFill ff = new CommandClaimFill.FloodFill(guild, cp, chunk);
                        Messenger error = ff.fill();
                        ClaimClipboard.addClipboard(player, new CommandClaimFill.Clipboard(player.getWorld(), (Map) Fn.cast(ff.chunks)));
                        cp.buildMap().clipboardMode().display();
                        if (error != null) {
                            context.sendError(error, "lands", ff.maxClaims);
                        } else {
                            context.sendMessage(Lang.COMMAND_CLAIM_FILL_DONE);
                        }

                    });
                }
            }
        }
    }

    private static final class FloodFill {
        private final Guild guild;
        private final CastelPlayer cp;
        private final int chunksLimit;
        private final SimpleChunkLocation mainChunk;
        private final Map<SimpleChunkLocation.WorldlessWrapper, ClaimClipboard.ClaimProcessor> chunks;
        private final Queue<SimpleChunkLocation> groups = new LinkedList<>();
        private final int maxClaims;

        public FloodFill(Guild guild, CastelPlayer cp, SimpleChunkLocation mainChunk) {
            this.guild = guild;
            this.cp = cp;
            this.mainChunk = mainChunk;
            this.chunksLimit = Config.Claims.FILL_MAX_CLAIMS.getManager().getInt();
            this.chunks = new HashMap<>(this.chunksLimit);
            this.chunks.put(mainChunk.worldlessWrapper(), (ClaimClipboard.ClaimProcessor)(new ClaimClipboard.ClaimProcessor(mainChunk, cp, guild)).dontCheckConnections());
            if (cp.isAdmin()) {
                this.maxClaims = Integer.MAX_VALUE;
            } else {
                int rankMaxClaims = cp.getRank().getMaxClaims();
                int guildMaxClaims = guild.getMaxClaims(mainChunk.getWorld());
                int leftKingdomClaims = Math.max(0, guildMaxClaims - guild.getLandLocations().size());
                if (rankMaxClaims > 0) {
                    this.maxClaims = Math.min(Math.max(0, rankMaxClaims - cp.getClaims().size()), leftKingdomClaims);
                } else {
                    this.maxClaims = leftKingdomClaims;
                }
            }

        }

        private Messenger fill() {
            if (this.maxClaims <= 0) {
                return Lang.COMMAND_CLAIM_FILL_MAX_CLAIMS;
            } else {
                double maxIterations = this.cp.isAdmin() ? 500.0 : Config.Claims.FILL_MAX_ITERATIONS.getManager().getDouble();
                int iterationCount = -10;
                if (!this.computeChunk(this.mainChunk)) {
                    return Lang.COMMAND_CLAIM_FILL_MAX_CLAIMS;
                } else {
                    while(!this.groups.isEmpty()) {
                        if ((double)iterationCount > maxIterations) {
                            return Lang.COMMAND_CLAIM_FILL_MAX_ITERATIONS;
                        }

                        SimpleChunkLocation next = this.groups.poll();
                        if (next == null) {
                            return null;
                        }

                        if (!this.computeChunk(next)) {
                            return Lang.COMMAND_CLAIM_FILL_MAX_CLAIMS;
                        }

                        ++iterationCount;
                    }

                    return null;
                }
            }
        }

        private boolean computeChunk(SimpleChunkLocation around) {
            SimpleChunkLocation mono = around.getRelative(1, 0);
            SimpleChunkLocation di = around.getRelative(-1, 0);
            SimpleChunkLocation tri = around.getRelative(0, 1);
            SimpleChunkLocation tetra = around.getRelative(0, -1);
            SimpleChunkLocation[] locations = new SimpleChunkLocation[]{mono, di, tri, tetra};

            for (SimpleChunkLocation chunk : locations) {
                if (!this.cp.isAdmin() && this.chunks.size() >= this.maxClaims) {
                    return false;
                }

                if (!this.chunks.containsKey(chunk.worldlessWrapper())) {
                    Land land = chunk.getLand();
                    if (land == null || !land.isClaimed()) {
                        ClaimClipboard.ClaimProcessor processor = (ClaimClipboard.ClaimProcessor) (new ClaimClipboard.ClaimProcessor(chunk, this.cp, this.guild)).dontCheckConnections();
                        if (this.chunks.put(chunk.worldlessWrapper(), processor) == null) {
                            this.groups.add(chunk);
                        }
                    }
                }
            }

            return true;
        }
    }

    public static final class Clipboard extends ClaimClipboard {
        public Clipboard(World world, Map<SimpleChunkLocation.WorldlessWrapper, AbstractClaimProcessor> claims) {
            super(claims, world);
        }
    }
}
