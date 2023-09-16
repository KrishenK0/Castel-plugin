package fr.krishenk.castel.managers.land.indicator;

import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.PlayerUtils;
import fr.krishenk.castel.utils.versionsupport.VersionSupport;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BlockVisualizerBuilder {
    public final List<Block> blocks = new ArrayList<>();
    private final Player player;
    private final Chunk chunk;
    private final int y;
    private final int maxHeight;
    private final int minHeight;
    private final int maxAttempts;
    private final XMaterial cornerBlocks;
    private final XMaterial twoBlocks;
    private final boolean outOfWorldBuildingBounds;

    public BlockVisualizerBuilder(Player player, Chunk chunk, int y, int maxAttempts, XMaterial cornerBlocks, XMaterial twoBlocks) {
        World world = chunk.getWorld();
        this.player = player;
        this.chunk = chunk;
        this.y = y;
        this.maxAttempts = maxAttempts;
        this.cornerBlocks = cornerBlocks;
        this.twoBlocks = twoBlocks;
        this.maxHeight = world.getMaxHeight() - 1;
        this.minHeight = VersionSupport.getMinWorldHeight(world);
        if (y < this.minHeight) {
            this.outOfWorldBuildingBounds = true;
            y = this.minHeight;
        } else if (y > this.maxHeight) {
            this.outOfWorldBuildingBounds = true;
            y = this.maxHeight;
        } else {
            this.outOfWorldBuildingBounds = maxAttempts <= 0;
        }
    }

    public void visualize(int x, int z, boolean corner) {
        int y = this.y;
        if (!this.outOfWorldBuildingBounds) {
            int attemps = this.maxAttempts;
            boolean isPassable = LocationUtils.blockCanBeReplaced(this.chunk.getBlock(x, y + 1, z));
            boolean descending = y + 1 <= this.maxHeight && isPassable;
            if (!isPassable) y += 2;

            while (attemps-- > 0 && y > this.minHeight && y < this.maxHeight) {
                Block block = this.chunk.getBlock(x, y, z);
                if (descending == !LocationUtils.blockCanBeReplaced(block)) break;
                if (descending) y--;
                else y++;
            }
        }

        Block block = this.chunk.getBlock(x, y, z);
        Location location = block.getLocation();
        PlayerUtils.sendBlockChange(this.player, location, corner ? this.cornerBlocks : this.twoBlocks);
        this.blocks.add(block);
    }
}
