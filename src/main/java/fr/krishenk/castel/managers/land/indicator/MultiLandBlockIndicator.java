package fr.krishenk.castel.managers.land.indicator;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.utils.PlayerUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MultiLandBlockIndicator extends LandIndicator {
    private final Collection<Collection<Block>> blocks = new ArrayList<>();

    public MultiLandBlockIndicator(Player player, BukkitTask task) {
        super(player, task);
    }

    public Collection<Collection<Block>> getBlocks() {
        return Collections.unmodifiableCollection(this.blocks);
    }

    public MultiLandBlockIndicator append(Collection<Block> blocks) {
        Objects.requireNonNull(blocks, "Cannot add null blocks collection");
        this.blocks.add(blocks);
        return this;
    }

    public MultiLandBlockIndicator append(SimpleChunkLocation chunk, String relation) {
        Collection<Block> blocks = LandVisualizer.build(this.player, chunk.toChunk(), relation);
        this.append(blocks);
        return this;
    }

    @Override
    public void end() {
        super.end();
        for (Collection<Block> blocks : this.blocks) {
            for (Block block : blocks) {
                if (block == null) break;
                PlayerUtils.sendBlockChange(this.player, block);
            }
        }
    }
}
