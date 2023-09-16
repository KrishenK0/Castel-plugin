package fr.krishenk.castel.services.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.managers.index.ConcurrentRegionIndex;
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.geom.Area;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Objects;

public class ServiceWorldGuardSeven extends ServiceWorldGuard {
    private static final StateFlag KINGDOMS_CLAIMABLE = ServiceWorldGuardSeven.registerFlag("kingdoms-claimable", false);
    private static final StateFlag KINGDOMS_FRIENDLY_FIRE = ServiceWorldGuardSeven.registerFlag("kingdoms-friendly-fire", false);
    private static final StateFlag KINGDOMS_DAMAGE_CHAMPION = ServiceWorldGuardSeven.registerFlag("kingdoms-damage-champion", true);
    private static final MethodHandle INDEX;

    @Override
    public boolean isAvailable() {
        try {
            WorldGuard.getInstance().getPlatform().getRegionContainer();
            return true;
        }
        catch (Throwable ex) {
            return false;
        }
    }

    private static StateFlag registerFlag(String name, boolean defaultState) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag(name, defaultState);
            registry.register(flag);
            return flag;
        }
        catch (FlagConflictException e) {
            e.printStackTrace();
            Flag<?> existing = registry.get(name);
            if (existing instanceof StateFlag) {
                return (StateFlag)existing;
            }
            return null;
        }
    }

    public static boolean init() {
        return KINGDOMS_CLAIMABLE != null;
    }

    private static Collection<ProtectedRegion> getRegions(RegionManager manager) {
        try {
            ConcurrentRegionIndex index = (ConcurrentRegionIndex) INDEX.invoke(manager);
            return index.values();
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    private static ProtectedRegion doesRegionIntersect(CuboidRegionProperties properties, Collection<ProtectedRegion> regions) {
        Area area = ServiceWorldGuardSeven.toArea(properties);
        for (ProtectedRegion region : regions) {
            if (!ServiceWorldGuardSeven.intersectsBoundingBox(properties, region)) continue;
            Area testArea = ServiceWorldGuardSeven.toArea(region);
            testArea.intersect(area);
            return !testArea.isEmpty() ? region : null;
        }
        return null;
    }

    private static Area toArea(CuboidRegionProperties properties) {
        int x = properties.minX;
        int z = properties.minZ;
        int width = properties.maxX - x + 1;
        int height = properties.maxZ - z + 1;
        return new Area(new Rectangle(x, z, width, height));
    }

    private static Area toArea(ProtectedRegion region) {
        int x = region.getMinimumPoint().getBlockX();
        int z = region.getMinimumPoint().getBlockZ();
        int width = region.getMaximumPoint().getBlockX() - x + 1;
        int height = region.getMaximumPoint().getBlockZ() - z + 1;
        return new Area(new Rectangle(x, z, width, height));
    }

    @Override
    public StateFlag getFriendlyFireFlag() {
        return KINGDOMS_FRIENDLY_FIRE;
    }

    @Override
    public StateFlag getDamageChampionFlag() {
        return KINGDOMS_DAMAGE_CHAMPION;
    }

    private static boolean intersectsBoundingBox(CuboidRegionProperties properties, ProtectedRegion region) {
        BlockVector3 rMaxPoint = region.getMaximumPoint();
        if (rMaxPoint.getBlockX() < properties.minX) {
            return false;
        }
        if (rMaxPoint.getBlockZ() < properties.minZ) {
            return false;
        }
        BlockVector3 rMinPoint = region.getMinimumPoint();
        if (rMinPoint.getBlockX() > properties.maxX) {
            return false;
        }
        return rMinPoint.getBlockZ() <= properties.maxZ;
    }

    @Override
    public boolean isClaimable(ProtectedRegion region) {
        return region.getFlag((Flag)KINGDOMS_CLAIMABLE) == StateFlag.State.ALLOW;
    }

    @Override
    protected RegionManager getRegionManager(World world) {
        Objects.requireNonNull(world, "Cannot get WorldGuard region manager from a null world");
        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        return container.get(BukkitAdapter.adapt(world));
    }

    private boolean isLocationInRegionOld(World world, CuboidRegionProperties properties) {
        BlockVector3 pos2;
        RegionManager manager = this.getRegionManager(world);
        if (manager == null) {
            return false;
        }
        BlockVector3 pos1 = BlockVector3.at(properties.minX, 0, properties.minZ);
        ProtectedCuboidRegion region = new ProtectedCuboidRegion("ChunkRegion", pos1, pos2 = BlockVector3.at(properties.maxX, world.getMaxHeight(), properties.maxZ));
        ApplicableRegionSet regions = manager.getApplicableRegions(region);
        return regions.size() != 0;
    }

    @Override
    public ProtectedRegion isLocationInRegion(World world, CuboidRegionProperties properties) {
        RegionManager manager = this.getRegionManager(world);
        if (manager == null) {
            return null;
        }
        return ServiceWorldGuardSeven.doesRegionIntersect(properties, ServiceWorldGuardSeven.getRegions(manager));
    }

    @Override
    public boolean isLocationInRegion(Location location, String regionName) {
        RegionManager manager = this.getRegionManager(location.getWorld());
        if (manager == null) {
            return false;
        }
        ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        for (ProtectedRegion region : regions.getRegions()) {
            if (!region.getId().equals(regionName)) continue;
            return true;
        }
        return false;
    }

    @Override
    public boolean hasFlag(Player player, Location location, StateFlag flag) {
        RegionManager manager = this.getRegionManager(location.getWorld());
        if (manager == null) {
            return false;
        }
        ApplicableRegionSet regions = manager.getApplicableRegions(BukkitAdapter.asBlockVector(location));
        return regions.queryState(WorldGuardPlugin.inst().wrapPlayer(player), flag) == StateFlag.State.ALLOW;
    }

    static {
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle index = null;
        try {
            Field field = RegionManager.class.getDeclaredField("index");
            field.setAccessible(true);
            index = lookup.unreflectGetter(field);
        }
        catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        INDEX = index;
    }
}
