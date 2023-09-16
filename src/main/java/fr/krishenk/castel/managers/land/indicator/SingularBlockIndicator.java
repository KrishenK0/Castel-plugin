package fr.krishenk.castel.managers.land.indicator;

import fr.krishenk.castel.utils.PlayerUtils;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Collection;

public class SingularBlockIndicator extends LandIndicator {
    private final Collection<Block> blocks;

    protected SingularBlockIndicator(Player player, Collection<Block> blocks, BukkitTask task) {
        super(player, task);
        this.blocks = blocks;
    }

    @Override
    public void end() {
        super.end();
        if (this.blocks != null) {
            for (Block block : this.blocks) {
                if (block == null) break;
                PlayerUtils.sendBlockChange(this.player, block);
            }
        }
    }
}
