package fr.krishenk.castel.commands.general.visualizer;

import fr.krishenk.castel.commands.CastelCommand;
import fr.krishenk.castel.commands.CastelParentCommand;
import fr.krishenk.castel.commands.CommandContext;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Config;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.managers.land.indicator.LandIndicator;
import fr.krishenk.castel.managers.land.indicator.LandVisualizer;
import fr.krishenk.castel.managers.land.indicator.MultiLandBlockIndicator;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

public class CommandVisualizeAll extends CastelCommand {
    public CommandVisualizeAll(CastelParentCommand parent) {
        super("all", parent);
    }

    @Override
    public void execute(CommandContext context) {
        if (!context.assertPlayer()) {
            Player player = context.senderAsPlayer();
            CastelPlayer cp = context.getCastelPlayer();
            Guild guild = cp.getGuild();
            SimpleChunkLocation masterChunk = SimpleChunkLocation.of(player.getLocation());
            final AtomicInteger chunks = new AtomicInteger();
            BukkitTask task = null;
            if (!LandVisualizer.getPermanent().contains(player.getUniqueId())) {
                task = (new BukkitRunnable() {
                    @Override
                    public void run() {
                        LandIndicator blocks = LandVisualizer.getVisualizer().remove(player.getEntityId());
                        if (blocks != null) blocks.end();
                    }
                }).runTaskLater(plugin, Config.Claims.INDICATOR_VISUALIZER_ALL_STAY.getManager().getInt() * 20L);
            }

            MultiLandBlockIndicator indicator = new MultiLandBlockIndicator(player, task);
            LandVisualizer.removeVisualizers(player, true);
            (new BukkitRunnable() {
                @Override
                public void run() {
                    SimpleChunkLocation[] chunksAround = masterChunk.getChunksAround(3, true);
                    for (SimpleChunkLocation chunk : chunksAround) {
                        Land land = chunk.getLand();
                        Guild landsGuild = land == null ? null : land.getGuild();
                        indicator.append(chunk, LandVisualizer.getRelationOf(chunk, landsGuild, guild));
                        chunks.incrementAndGet();
                    }
                }
            }).runTaskAsynchronously(plugin);
            LandVisualizer.getVisualizer().put(player.getEntityId(), indicator);
            context.sendMessage(Lang.COMMAND_VISUALIZE_ALL_SHOWING, "chunks", chunks.get());
        }
    }
}
