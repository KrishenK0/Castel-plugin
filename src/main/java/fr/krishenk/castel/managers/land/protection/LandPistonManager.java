package fr.krishenk.castel.managers.land.protection;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.group.model.relationships.StandardRelationAttribute;
import fr.krishenk.castel.constants.group.upgradable.MiscUpgrade;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.lang.Config;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPistonEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.ArrayList;
import java.util.List;

public class LandPistonManager implements Listener {

    private static void handlePiston(BlockPistonEvent event, List<Block> blocks) {
        if (!Config.DISABLED_WORLDS.isInDisabledWorld(event.getBlock())) {
            boolean disabled = !MiscUpgrade.ANTI_TRAMPLE.isEnabled();
            Block piston = event.getBlock();
            SimpleChunkLocation pistonChunk = SimpleChunkLocation.of(piston);
            Land pistonLand = pistonChunk.getLand();
            Guild pistonGuild = pistonLand == null ? null : pistonLand.getGuild();

            for (Block block : blocks) {
                SimpleChunkLocation chunk = SimpleChunkLocation.of(block);
                Land land = Land.getLand(chunk);
                if (!disabled && !pistonChunk.equalsIgnoreWorld(chunk)) {
                    Guild guild = land.getGuild();
                    if (guild != null && !StandardRelationAttribute.BUILD.hasAttribute(pistonGuild, guild) && guild.getUpgradeLevel(MiscUpgrade.ANTI_TRAMPLE) > 1) {
                        event.setCancelled(true);
                        break;
                    }
                }
            }
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonRetract(BlockPistonRetractEvent event) {
        handlePiston(event, event.getBlocks());
    }

    @EventHandler(ignoreCancelled = true)
    public void onPistonExtend(BlockPistonExtendEvent event) {
        List<Block> blocks = new ArrayList<>(event.getBlocks());
        blocks.add(event.getBlock().getRelative(event.getDirection(), blocks.size() + 1));
        handlePiston(event, blocks);
    }
}
