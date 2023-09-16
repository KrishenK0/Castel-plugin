package fr.krishenk.castel.constants.land.location;

import fr.krishenk.castel.constants.land.Land;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class SimpleChunkLocation implements Cloneable {
    private final @NonNull String world;
    private final int x;
    private final int z;

    public SimpleChunkLocation(@NonNull String world, int x, int z) {
        this.world = Objects.requireNonNull(world, "Simple chunk location cannot have a null world");
        this.x = x;
        this.z = z;
    }

    public static SimpleChunkLocation of(@NonNull Chunk chunk) {
        Objects.requireNonNull(chunk, "Cannot get simple chunk location of a null chunk");
        return new SimpleChunkLocation(chunk.getWorld().getName(), chunk.getX(), chunk.getZ());
    }

    public static SimpleChunkLocation of(@NonNull Location location) {
        Objects.requireNonNull(location, "Cannot get simple chunk location of a null location");
        return new SimpleChunkLocation(Objects.requireNonNull(location.getWorld(), "Simple chunk location cannot have a null world").getName(), location.getBlockX() >> 4, location.getBlockZ() >> 4);
    }

    public static SimpleChunkLocation of(@NonNull Block block) {
        Objects.requireNonNull(block, "Cannot get simple chunk location of a null block");
        return new SimpleChunkLocation(block.getWorld().getName(), block.getX() >> 4, block.getZ() >> 4);
    }

    public static @NonNull SimpleChunkLocation fromString(@NonNull String chunk) {
        Validate.notEmpty(chunk, "Chunk string cannot be null or empty");
        String[] split = StringUtils.splitLocation(chunk, 3);
        String world = split[0];
        int x = MathUtils.parseIntUnchecked(split[1], true);
        int z = MathUtils.parseIntUnchecked(split[2], true);
        return new SimpleChunkLocation(world, x, z);
    }

    public static int calculateBorderSize(int n) {
        int radius = 2 * n + 1;
        return radius * radius;
    }

    private static void validateRadius(int radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Cannot get chunks around chunk with radius: " + radius);
        }
    }

    public static Supplier<SimpleChunkLocation> resolve(Block block) {
        return () -> SimpleChunkLocation.of(block);
    }

    public static Supplier<SimpleChunkLocation> resolve(Entity entity) {
        return () -> SimpleChunkLocation.of(entity.getLocation());
    }

    public boolean equalsIgnoreWorld(SimpleChunkLocation chunk) {
        return this.x == chunk.x && this.z == chunk.z;
    }

    public SimpleChunkLocation getRelative(int x, int z) {
        return new SimpleChunkLocation(this.world, this.x + x, this.z + z);
    }

    public boolean isInChunk(@NonNull SimpleLocation location) {
        SimpleChunkLocation chunk = location.toSimpleChunkLocation();
        return chunk.x == this.x && chunk.z == this.z && Objects.equals(this.world, chunk.world);
    }

    public boolean isInChunk(@NonNull Location location) {
        return this.equals(SimpleChunkLocation.of(location));
    }

    public double distance(@NonNull SimpleChunkLocation chunk) {
        if (chunk.world.equals(this.world)) {
            return this.distanceIgnoreWorld(chunk);
        }
        throw new IllegalArgumentException("Cannot measure distance between " + this.world + " and " + chunk.world);
    }

    public double distanceIgnoreWorld(@NonNull SimpleChunkLocation chunk) {
        return Math.sqrt(NumberConversions.square((double)((double)this.x - (double)chunk.x)) + NumberConversions.square((double)((double)this.z - (double)chunk.z)));
    }

    public @NonNull Location getCenterLocation() {
        World world = this.getBukkitWorld();
        int locX = (this.x << 4) + 8;
        int locZ = (this.z << 4) + 8;
        int locY = world.getHighestBlockYAt(locX, locZ) + 1;
        return new Location(world, (double)locX, (double)locY, (double)locZ);
    }

    public @NonNull SimpleLocation getSimpleLocation(int blockX, int blockY, int blockZ) {
        int locX = this.x << 4 | blockX;
        int locZ = this.z << 4 | blockZ;
        return new SimpleLocation(this.world, locX, blockY, locZ);
    }

    public @NonNull String getWorld() {
        return this.world;
    }

    public @NonNull World getBukkitWorld() {
        return Objects.requireNonNull(Bukkit.getWorld((String)this.world), () -> "Unknown/Deleted world: " + this.world);
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public @NonNull SimpleChunkLocation clone() {
        try {
            return (SimpleChunkLocation)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new AssertionError((Object)("SimpleChunkLocation clone failed: " + e.getLocalizedMessage()));
        }
    }

    public WorldlessWrapper worldlessWrapper() {
        return new WorldlessWrapper(this.x, this.z);
    }

    public int hashCode() {
        int prime = 31;
        int result = 19;
        result = prime * result + this.world.hashCode();
        result = prime * result + this.x;
        result = prime * result + this.z;
        return result;
    }

    public @Nullable Land getLand() {
        return Land.getLand(this);
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SimpleChunkLocation) {
            SimpleChunkLocation chunk = (SimpleChunkLocation)obj;
            return this.x == chunk.x && this.z == chunk.z && Objects.equals(this.world, chunk.world);
        }
        return false;
    }

    public List<Player> getPlayers() {
        ArrayList<Player> players = new ArrayList<Player>();
        Chunk chunk = this.toChunk();
        for (Entity entity : chunk.getEntities()) {
            if (!(entity instanceof Player)) continue;
            players.add((Player)entity);
        }
        return players;
    }

    public @Nullable SimpleChunkLocation findFarthestChunk(Collection<SimpleChunkLocation> locations, boolean ignoreWorld) {
        Objects.requireNonNull(locations);
        if (locations.isEmpty()) {
            return null;
        }
        SimpleChunkLocation anotherWorld = null;
        SimpleChunkLocation farthest = null;
        double farthestDistance = 0.0;
        for (SimpleChunkLocation chunk : locations) {
            if (!ignoreWorld && !chunk.world.equals(this.world)) {
                anotherWorld = chunk;
                continue;
            }
            double dist = this.distance(chunk);
            if (!(dist > farthestDistance)) continue;
            farthest = chunk;
            farthestDistance = dist;
        }
        if (farthest == null && anotherWorld != null) {
            return anotherWorld;
        }
        return farthest;
    }

    public SimpleChunkLocation[] getChunksAround(int radius, boolean includingSelf) {
        SimpleChunkLocation.validateRadius(radius);
        SimpleChunkLocation[] chunks = new SimpleChunkLocation[SimpleChunkLocation.calculateBorderSize(radius) - (includingSelf ? 0 : 1)];
        int index = 0;
        for (int x = -radius; x <= radius; ++x) {
            for (int z = -radius; z <= radius; ++z) {
                if (!includingSelf && x == 0 && z == 0) continue;
                chunks[index++] = this.getRelative(x, z);
            }
        }
        return chunks;
    }

    public SimpleChunkLocation[] getChunksAround(int radius) {
        return this.getChunksAround(radius, false);
    }

    public <T> T findFromSurroundingChunks(int radius, Function<SimpleChunkLocation, T> predicate) {
        return this.findFromSurroundingChunks(radius, null, predicate);
    }

    public boolean anySurroundingChunks(int radius, Predicate<SimpleChunkLocation> predicate) {
        return this.findFromSurroundingChunks(radius, false, x -> predicate.test((SimpleChunkLocation)x) ? Boolean.valueOf(true) : null);
    }

    public <T> T findFromSurroundingChunks(int radius, T defaultValue, Function<SimpleChunkLocation, T> predicate) {
        SimpleChunkLocation.validateRadius(radius);
        for (int x = -radius; x <= radius; ++x) {
            for (int z = -radius; z <= radius; ++z) {
                SimpleChunkLocation current;
                T result;
                if (x == 0 && z == 0 || (result = predicate.apply(current = this.getRelative(x, z))) == null) continue;
                return result;
            }
        }
        return defaultValue;
    }

    public String toString() {
        return this.world + ", " + this.x + ", " + this.z;
    }

    public @NonNull String getCompressedData() {
        return this.x + this.world + this.z;
    }

    public @NonNull Chunk toChunk() {
        return this.getBukkitWorld().getChunkAt(this.x, this.z);
    }

    public static class WorldlessWrapper {
        private final int x;
        private final int z;

        public WorldlessWrapper(int x, int z) {
            this.x = x;
            this.z = z;
        }

        public WorldlessWrapper getRelative(int x, int z) {
            return new WorldlessWrapper(this.x + x, this.z + z);
        }

        public int getX() {
            return this.x;
        }

        public int getZ() {
            return this.z;
        }

        public int hashCode() {
            int var2 = 1664525 * this.x + 1013904223;
            int var3 = 1664525 * (this.z ^ 0xDEADBEEF) + 1013904223;
            return var2 ^ var3;
        }

        public SimpleChunkLocation inWorld(String world) {
            return new SimpleChunkLocation(world, this.x, this.z);
        }

        public SimpleChunkLocation inWorld(World world) {
            return this.inWorld(world.getName());
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof WorldlessWrapper)) {
                return false;
            }
            WorldlessWrapper location = (WorldlessWrapper)obj;
            return this.x == location.x && this.z == location.z;
        }
    }
}


