package fr.krishenk.castel.utils;

import fr.krishenk.castel.constants.group.Guild;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.utils.internal.FastUUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import javax.annotation.Nullable;
import java.util.UUID;

public class CastelBukkitExtensions {
    public static final CastelBukkitExtensions INSTANCE = new CastelBukkitExtensions();

    private CastelBukkitExtensions() { }

    
    public final SimpleLocation getSimpleLocation(Entity entity) {
        SimpleLocation simpleLocation = SimpleLocation.of(entity.getLocation());
        return simpleLocation;
    }

    /*
    public final SimpleChunkLocation getSimpleChunkLocation(Entity $this$getSimpleChunkLocation) {
        SimpleChunkLocation simpleChunkLocation = SimpleChunkLocation.of($this$getSimpleChunkLocation.getLocation());
        return simpleChunkLocation;
    }*/

    
    public final SimpleLocation toSimpleLocation(Block block) {
        SimpleLocation simpleLocation = SimpleLocation.of(block);
        return simpleLocation;
    }

    /*
    public final SimpleChunkLocation toSimpleChunkLocation(Block $this$toSimpleChunkLocation) {
        SimpleChunkLocation simpleChunkLocation = SimpleChunkLocation.of($this$toSimpleChunkLocation);
        return simpleChunkLocation;
    }*/

    
    public final SimpleLocation toSimpleLocation(Location location) {
        SimpleLocation simpleLocation = SimpleLocation.of(location);
        return simpleLocation;
    }

    /*
    public final SimpleChunkLocation toSimpleChunkLocation(Location $this$toSimpleChunkLocation) {
        SimpleChunkLocation simpleChunkLocation = SimpleChunkLocation.of($this$toSimpleChunkLocation);
        return simpleChunkLocation;
    }*/

    /*
    public final SimpleChunkLocation toSimpleChunkLocation(Chunk $this$toSimpleChunkLocation) {
        SimpleChunkLocation simpleChunkLocation = SimpleChunkLocation.of($this$toSimpleChunkLocation);
        return simpleChunkLocation;
    }*/

    
    public final CastelPlayer asCastelPlayer(OfflinePlayer offlinePlayer) {
        CastelPlayer castelPlayer = CastelPlayer.getCastelPlayer(offlinePlayer);
        return castelPlayer;
    }

    @Nullable
    public final Guild getGuild(OfflinePlayer offlinePlayer) {
        return this.asCastelPlayer(offlinePlayer).getGuild();
    }

    @Nullable
    public final Guild asGuild(UUID uuid) {
        
        return Guild.getGuild(uuid);
    }

    @Nullable
    public final Player asPlayer(UUID uuid) {
        return Bukkit.getPlayer(uuid);
    }

    
    public final OfflinePlayer asOfflinePlayer(UUID uuid) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
        return offlinePlayer;
    }

    
    public final CastelPlayer asCastelPlayer(UUID uuid) {
        CastelPlayer castelPlayer = CastelPlayer.getCastelPlayer(uuid);
        return castelPlayer;
    }

    
    public final String toFastString(UUID uuid) {
        String string = FastUUID.toString(uuid);
        return string;
    }
}
