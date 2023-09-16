package fr.krishenk.castel.services.worldguard;

import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import fr.krishenk.castel.services.Service;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashSet;
import java.util.Set;

public abstract class ServiceWorldGuard implements Service {
    protected static final String CHECK_REGION_ID = "ChunkRegion";

    public final boolean isChunkInRegion(World world, int x, int z, int radius) {
        if (radius < 0) {
            throw new IllegalArgumentException("Cannot check chunk in regions with radius: " + radius);
        }
        int chunkMX = x - radius;
        int minX = chunkMX << 4;
        int chunkMZ = z - radius;
        int minZ = chunkMZ << 4;
        int chunkPX = x + radius;
        int maxX = (chunkPX << 4) + 15;
        int chunkPZ = z + radius;
        int maxZ = (chunkPZ << 4) + 15;
        CuboidRegionProperties properties = new CuboidRegionProperties(minX, minZ, maxX, maxZ);
        ProtectedRegion region = this.isLocationInRegion(world, properties);
        return region != null && !this.isClaimable(region);
    }

    public boolean hasRegion(@NonNull World world, String region) {
        RegionManager regionManager = this.getRegionManager(world);
        return regionManager != null && regionManager.hasRegion(region);
    }

    protected abstract @Nullable RegionManager getRegionManager(@NonNull World var1);

    public @NonNull Set<String> getRegions(World world) {
        RegionManager manager = this.getRegionManager(world);
        return manager == null ? new HashSet() : manager.getRegions().keySet();
    }

    public abstract boolean hasFlag(Player var1, Location var2, StateFlag var3);

    public StateFlag getFriendlyFireFlag() {
        return null;
    }

    public StateFlag getDamageChampionFlag() {
        return null;
    }

    public final boolean hasFriendlyFireFlag(Player player) {
        return this.hasFlag(player, this.getFriendlyFireFlag());
    }

    private final boolean hasFlag(Player player, StateFlag flag) {
        return this.hasFlag(player, player.getLocation(), flag);
    }

    public final boolean canDamageChampion(Player player) {
        return this.hasFlag(player, this.getDamageChampionFlag());
    }

    public abstract boolean isLocationInRegion(Location var1, String var2);

    public abstract ProtectedRegion isLocationInRegion(World var1, CuboidRegionProperties var2);

    public abstract boolean isClaimable(ProtectedRegion var1);
}
