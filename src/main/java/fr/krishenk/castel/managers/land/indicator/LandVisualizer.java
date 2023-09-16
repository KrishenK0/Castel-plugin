package fr.krishenk.castel.managers.land.indicator;

import com.google.common.base.Strings;
import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.GuildRelation;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.libs.xseries.particles.ParticleDisplay;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.internal.integer.IntHashMap;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.NumberConversions;

import java.util.*;

public class LandVisualizer {
    private static final Set<UUID> PERMANENT = new HashSet<>();
    private static final IntHashMap<LandIndicator> VISUALIZER = new IntHashMap<>();
    private static final int MAX_PLAYER_ACCESSIBILITY_VIEW_DISTANCE = 160;
    private Player player;
    private CastelPlayer cp;
    private Guild landGuild;
    private Chunk chunk;
    private String relation;

    public LandVisualizer() {}

    public static Set<UUID> getPermanent() {
        return PERMANENT;
    }

    public static IntHashMap<LandIndicator> getVisualizer() {
        return VISUALIZER;
    }

    private static boolean canSeeBlockVertically(World world, int y) {
        int maxHeight = world.getMaxHeight();
        int minHeight = world.getMinHeight();
        int adjustedY;
        if (y > maxHeight) adjustedY = maxHeight;
        else {
            if (y >= minHeight) return true;
            adjustedY = minHeight;
        }

        return Math.abs(Math.abs(adjustedY) - Math.abs(y)) <= 160;
    }

    public static Collection<Block> build(Player player, Chunk chunk, String relation) {
        int y = NumberConversions.floor(player.getLocation().getY()) - 1;
        if (!canSeeBlockVertically(player.getWorld(), y)) return null;
        int max = Config.Claims.INDICATOR_VISUALIZER_FLOOR_CHECK_HEIGHT.getManager().getInt();
        String cornerBlockStr = Config.Claims.INDICATOR_CORNER_BLOCK.getManager().withOption("relation", relation).getString();
        String twoBlockStr = Config.Claims.INDICATOR_TWO_BLOCK.getManager().withOption("relation", relation).getString();
        XMaterial cornerBlock = Strings.isNullOrEmpty(cornerBlockStr) ? null : XMaterial.matchXMaterial(cornerBlockStr).orElse(null);
        XMaterial twoBlock = Strings.isNullOrEmpty(twoBlockStr) ? null : XMaterial.matchXMaterial(twoBlockStr).orElse(null);
        BlockVisualizerBuilder blockVisualizer = new BlockVisualizerBuilder(player, chunk, y, max, cornerBlock, twoBlock);
        if (cornerBlock != null) {
            blockVisualizer.visualize(0, 0, true);
            blockVisualizer.visualize(0, 15, true);
            blockVisualizer.visualize(15, 15, true);
            blockVisualizer.visualize(0, 15, true);
        }

        if (twoBlock != null) {
            if (Config.Claims.INDICATOR_VISUALIZER_BORDER.getManager().getBoolean()) {
                for (int i = 0; i < 15; i++) {
                    blockVisualizer.visualize(i, 0, false);
                    blockVisualizer.visualize(i, 15, false);
                    blockVisualizer.visualize(0, i, false);
                    blockVisualizer.visualize(15, i, false);
                }
            } else {
                blockVisualizer.visualize(1, 0, false);
                blockVisualizer.visualize(0, 1, false);
                blockVisualizer.visualize(0, 14, false);
                blockVisualizer.visualize(1, 15, false);
                blockVisualizer.visualize(15, 14, false);
                blockVisualizer.visualize(14, 15, false);
                blockVisualizer.visualize(14, 0, false);
                blockVisualizer.visualize(15, 1, false);
            }
        }

        return blockVisualizer.blocks;
    }

    public static void removeVisualizers(Player player, boolean updateBlocks) {
        LandIndicator previousBlocks = VISUALIZER.remove(player.getEntityId());
        if (updateBlocks && previousBlocks != null) previousBlocks.end();
    }

    public LandVisualizer forPlayer(Player player, CastelPlayer cp) {
        this.player = Objects.requireNonNull(player, "Cannot show land visualizers for null player");
        this.cp = cp;
        return this;
    }

    public LandVisualizer forGuild(Guild guild) {
        this.landGuild = guild;
        return this;
    }

    public LandVisualizer forLand(SimpleChunkLocation chunk) {
        return this.forLand(chunk.getLand(), chunk.toChunk());
    }

    public LandVisualizer forLand(Land land, Chunk chunk) {
        this.landGuild = land == null ? null : land.getGuild();
        this.chunk = chunk;
        return this;
    }

    public LandVisualizer display(boolean force) {
        if (force || this.cp.isUsingMarkers()) {
            this.displayIndicators();
        }

        return this.displayMessages();
    }

    private void setRelation() {
        this.relation = getRelationOf(SimpleChunkLocation.of(this.chunk), this.landGuild, this.cp.getGuild());
    }

    public static String getRelationOf(SimpleChunkLocation of, Guild landGuild, Guild guild) {
        if (landGuild != null) {
            GuildRelation relation = guild == null ? GuildRelation.NEUTRAL : guild.getRelationWith(landGuild);
            return StringUtils.configOption(relation);
        }
        return "wilderness";
    }

    public LandVisualizer displayIndicators() {
        if (this.relation == null)
            this.setRelation();

        String markers = this.cp.getMarkersType();
        if (markers == null)
            markers = Config.Claims.INDICATOR_DEFAULT_METHOD.getManager().getString();

        if (markers.equalsIgnoreCase("blocks")) this.visualizeBlocks();
        else this.visualizeParticle(markers);

        return this;
    }

    public LandVisualizer displayMessages() {
        if (this.relation == null) this.setRelation();
        Lang.valueOf("LANDS_VISUALIZER_" + StringUtils.configOptionToEnum(this.relation)).sendMessage(this.player, (new MessageBuilder()).withContext(this.player).other(this.landGuild));
        return this;
    }

    public void visualizeBlocks() {
        (new BukkitRunnable() {
            @Override
            public void run() {
                LandVisualizer.removeVisualizers(LandVisualizer.this.player, true);
                Collection<Block> blocks = LandVisualizer.build(LandVisualizer.this.player, LandVisualizer.this.chunk, LandVisualizer.this.relation);
                if (!blocks.isEmpty()) {
                    BukkitTask task = null;
                    if (!LandVisualizer.PERMANENT.contains(LandVisualizer.this.player.getUniqueId())) {
                        task = (new BukkitRunnable() {
                            @Override
                            public void run() {
                                LandIndicator blocks = LandVisualizer.VISUALIZER.remove(LandVisualizer.this.player.getEntityId());
                                if (blocks != null) blocks.end();
                            }
                        }).runTaskLater(CastelPlugin.getInstance(), Config.Claims.INDICATOR_VISUALIZER_STAY.getManager().getInt() * 20L);
                    }

                    LandVisualizer.VISUALIZER.put(LandVisualizer.this.player.getEntityId(), new SingularBlockIndicator(LandVisualizer.this.player, blocks, task));
                }
            }
        }).runTaskAsynchronously(CastelPlugin.getInstance());
    }

    public void visualizeParticle(String marker) {
        if (Strings.isNullOrEmpty(marker)) throw new IllegalArgumentException("Marker is null or empty: " + marker);

        ConfigurationSection config = Config.Claims.INDICATOR_PARTICLES.getManager().withProperty(marker).getSection();
        ConfigurationSection editMarkerSection = Config.Claims.INDICATOR_PARTICLES.getManager().withOption("relation", this.relation).withProperty(marker).getSection();
        BukkitTask task = (new BukkitRunnable() {
            final ParticleDisplay display = ParticleDisplay.edit(ParticleDisplay.fromConfig(config), editMarkerSection);
            final World world = LandVisualizer.this.chunk.getWorld();
            final int cornerX = LandVisualizer.this.chunk.getX() << 4;
            final int cornerZ = LandVisualizer.this.chunk.getZ() << 4;
            final double height = config.getDouble("height");
            final double verticalRate = this.height / config.getDouble("rates.vertical");
            final double horizontalRate = 16 / config.getDouble("rates.horizontal");
            int duration = LandVisualizer.PERMANENT.contains(LandVisualizer.this.player.getUniqueId()) ? Integer.MAX_VALUE : config.getInt("duration");

            @Override
            public void run() {
                int startHeight = LandVisualizer.this.player.getLocation().getBlockY();
                double maxY = startHeight + this.height;
                for(double y = startHeight; y < maxY; y += this.verticalRate) {
                    double z;
                    for(z = this.cornerX; z <= (double)(this.cornerX + 16); z += this.horizontalRate) {
                        this.display.spawn(new Location(this.world, z, y, this.cornerZ), LandVisualizer.this.player);
                        this.display.spawn(new Location(this.world, z, y, this.cornerZ + 16), LandVisualizer.this.player);
                    }

                    for(z = this.cornerZ; z <= (double)(this.cornerZ + 16); z += this.horizontalRate) {
                        this.display.spawn(new Location(this.world, this.cornerX, y, z), LandVisualizer.this.player);
                        this.display.spawn(new Location(this.world, this.cornerX + 16, y, z), LandVisualizer.this.player);
                    }
                }

                if (--this.duration <= 0) this.cancel();
            }
        }).runTaskTimerAsynchronously(CastelPlugin.getInstance(), 0L, config.getLong("delay"));
        LandIndicator previous = VISUALIZER.put(this.player.getEntityId(), new LandIndicator(this.player, task));
        if (previous != null) previous.end();
    }
}
