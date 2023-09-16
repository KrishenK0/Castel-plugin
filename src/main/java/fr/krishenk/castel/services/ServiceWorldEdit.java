package fr.krishenk.castel.services;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.event.extent.EditSessionEvent;
import com.sk89q.worldedit.extension.platform.Actor;
import com.sk89q.worldedit.extent.AbstractDelegateExtent;
import com.sk89q.worldedit.extent.Extent;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.util.eventbus.Subscribe;
import com.sk89q.worldedit.world.World;
import com.sk89q.worldedit.world.block.BlockStateHolder;
import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public class ServiceWorldEdit implements Service {
    @Override
    public void enable() {
        WorldEdit.getInstance().getEventBus().register(this);
    }

    private static void paste(File file, SimpleLocation location) throws WorldEditException, IOException {
        ClipboardFormat format = ClipboardFormats.findByFile(file);
        try (ClipboardReader reader = format.getReader(Files.newInputStream(file.toPath()))) {
            Clipboard clipboard = reader.read();
            World world = BukkitAdapter.adapt(location.getBukkitWorld());
            try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory().getEditSession(world, -1)) {
                Operation operation = new ClipboardHolder(clipboard).createPaste(editSession).to(BlockVector3.at(location.getX(), location.getY(), location.getZ())).ignoreAirBlocks(false).build();
                Operations.complete(operation);
            }
        }
    }

    @Subscribe
    public void onEdit(EditSessionEvent event) {
        Actor editor = event.getActor();
        if (editor == null || !editor.isPlayer()) return;
        Player player = Bukkit.getPlayer(editor.getUniqueId());
        Objects.requireNonNull(player, () -> "Actor " + editor + " for WorldEdit is a null player");
        if (player.isOp()) return;
        CastelPlayer cp = CastelPlayer.getCastelPlayer(player);
        if (!cp.hasGuild() || cp.isAdmin()) return;
        Guild guild = cp.getGuild();
        org.bukkit.World world = BukkitAdapter.adapt(event.getWorld());
        AtomicInteger excluded = new AtomicInteger();
        event.setExtent(new GuildProtectionExtent(event.getExtent(), touchedLocation -> {
            Location bukkitLocation = BukkitAdapter.adapt(world, touchedLocation);
            SimpleChunkLocation chunk = SimpleChunkLocation.of(bukkitLocation);
            if (!guild.isClaimed(chunk)) {
                excluded.incrementAndGet();
                return false;
            }
            return true;
        }));
        if (excluded.get() > 0) {
            player.sendMessage("A total of "+excluded.get()+" blocks were excluded from WorldEdit since they weren't in your land.");
        }
    }

    private static class GuildProtectionExtent extends AbstractDelegateExtent {
        private final Function<BlockVector3, Boolean> filterFunction;

        protected GuildProtectionExtent(Extent extent, Function<BlockVector3, Boolean> excludeFunction) {
            super(extent);
            this.filterFunction = excludeFunction;
        }

        public <T extends BlockStateHolder<T>> boolean setBlock(BlockVector3 location, T block) throws WorldEditException {
            if (!this.filterFunction.apply(location).booleanValue()) return false;
            return super.setBlock(location, block);
        }
    }
}
