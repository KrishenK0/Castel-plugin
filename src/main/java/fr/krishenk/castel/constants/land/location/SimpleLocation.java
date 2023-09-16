package fr.krishenk.castel.constants.land.location;

import com.google.common.base.Strings;
import fr.krishenk.castel.utils.MathUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import lombok.NonNull;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class SimpleLocation implements Cloneable {
    private final String world;
    private final int x;
    private final int y;
    private final int z;

    public SimpleLocation(String world, int x, int y, int z) {
        this.world = Objects.requireNonNull(world, "World name cannot be null");
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static SimpleLocation of(@NonNull Location location) {
        Objects.requireNonNull(location, "Cannot get simple location of a null location");
        World world = Objects.requireNonNull(location.getWorld(), "World of location is null - Either an issue with your world management plugin or the world was deleted.");
        return new SimpleLocation(world.getName(), location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    public static SimpleLocation of(@NonNull Block block) {
        Objects.requireNonNull(block, "Cannot get simple location of a null block");
        return new SimpleLocation(block.getWorld().getName(), block.getX(), block.getY(), block.getZ());
    }

    public static SimpleLocation fromString(@NonNull String location) {
        Validate.notEmpty(location, "Location string cannot be null or empty");
        String[] split = StringUtils.splitLocation(location, 4);
        String world = split[0];
        int x = MathUtils.parseIntUnchecked(split[1], true);
        int y = MathUtils.parseIntUnchecked(split[2], true);
        int z = MathUtils.parseIntUnchecked(split[3], true);
        return new SimpleLocation(world, x, y, z);
    }

    private static double square(int num) {
        return (double)num * (double)num;
    }

    public static boolean equalsIgnoreWorld(@Nonnull Location location, @Nonnull Location other) {
        if (location == other) {
            return true;
        }
        return location.getX() == other.getX() && location.getY() == other.getY() && location.getZ() == other.getY();
    }

    public static boolean equalsIgnoreWorld(@Nonnull Block block, @Nonnull Block other) {
        if (block == other) {
            return true;
        }
        return block.getX() == other.getX() && block.getY() == other.getY() && block.getZ() == other.getY();
    }

    public static Supplier<SimpleLocation> resolve(Block block) {
        return () -> SimpleLocation.of(block);
    }

    public static Supplier<SimpleLocation> resolve(Entity entity) {
        return () -> SimpleLocation.of(entity.getLocation());
    }

    public @NonNull String getCompressedData() {
        return this.world + this.x + this.y + this.z;
    }

    public @NonNull SimpleChunkLocation toSimpleChunkLocation() {
        return new SimpleChunkLocation(this.world, this.x >> 4, this.z >> 4);
    }

    public @NonNull Block getBlock() {
        return this.getBukkitWorld().getBlockAt(this.x, this.y, this.z);
    }

    public @NonNull String getWorld() {
        return this.world;
    }

    public @Nullable World getBukkitWorld() {
        return Objects.requireNonNull(Bukkit.getWorld((String)this.world), () -> "Unknown/Deleted world: '" + this.world + '\'');
    }

    public int getX() {
        return this.x;
    }

    public int getZ() {
        return this.z;
    }

    public int getY() {
        return this.y;
    }

    public @NonNull SimpleLocation clone() {
        try {
            return (SimpleLocation)super.clone();
        }
        catch (CloneNotSupportedException e) {
            throw new AssertionError((Object)("SimpleLocation clone failed: " + e.getLocalizedMessage()));
        }
    }

    public int hashCode() {
        int prime = 31;
        int result = 14;
        result = prime * result + this.world.hashCode();
        result = prime * result + this.x;
        result = prime * result + this.y;
        result = prime * result + this.z;
        return result;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof SimpleLocation) {
            SimpleLocation loc = (SimpleLocation)obj;
            return this.x == loc.x && this.y == loc.y && this.z == loc.z && Objects.equals(this.world, loc.world);
        }
        return false;
    }

    public boolean equalsIgnoreWorld(SimpleLocation loc) {
        return this.x == loc.x && this.y == loc.y && this.z == loc.z;
    }

    public String toString() {
        return this.world + ", " + this.x + ", " + this.y + ", " + this.z;
    }

    public @NonNull Vector toVector() {
        return new Vector(this.x, this.y, this.z);
    }

    public @NonNull SimpleLocation getRelative(int x, int y, int z) {
        return new SimpleLocation(this.world, this.x + x, this.y + y, this.z + z);
    }

    public boolean validateWorld() {
        return !Strings.isNullOrEmpty((String)this.world) && this.getBukkitWorld() != null;
    }

    public double distanceSquared(@NonNull SimpleLocation location) {
        return Math.sqrt(this.distance(location));
    }

    public double distanceSquared(@NonNull Location location) {
        return Math.sqrt(this.distance(location));
    }

    public double distance(@NonNull SimpleLocation location) {
        Objects.requireNonNull(location, "Cannot check distance between a null location");
        if (!Objects.equals(this.world, location.world)) {
            throw new IllegalArgumentException("Cannot measure distance between " + this.world + " and " + location.world);
        }
        return this.distanceIgnoreWorld(location);
    }

    public double distanceIgnoreWorld(@NonNull SimpleLocation location) {
        Objects.requireNonNull(location, "Cannot check distance between a null location");
        return SimpleLocation.square(this.x - location.x) + SimpleLocation.square(this.y - location.y) + SimpleLocation.square(this.z - location.z);
    }

    public double distanceSquaredIgnoreWorld(@NonNull SimpleLocation location) {
        Objects.requireNonNull(location, "Cannot check distance between a null location");
        return Math.sqrt(this.distanceIgnoreWorld(location));
    }

    public double distance(@NonNull Location location) {
        Objects.requireNonNull(location, "Cannot check distance between a null location");
        World locWorld = location.getWorld();
        if (locWorld != null) {
            if (!locWorld.getName().equals(this.world)) {
                throw new IllegalArgumentException("Cannot measure distance between " + this.world + " and " + locWorld.getName());
            }
            return NumberConversions.square((double)((double)this.x - location.getX())) + NumberConversions.square((double)((double)this.y - location.getY())) + NumberConversions.square((double)((double)this.z - location.getZ()));
        }
        throw new IllegalArgumentException("Cannot measure distance to a null world");
    }

    public @NonNull Location toBukkitLocation(World world) {
        return new Location(world, (double)this.x, (double)this.y, (double)this.z);
    }

    public @NonNull Location toBukkitLocation() {
        return this.toBukkitLocation(this.getBukkitWorld());
    }

    public WorldlessWrapper toWorldLessWrapper() {
        return new WorldlessWrapper(this.x, this.y, this.z);
    }

    public static class WorldlessWrapper {
        private final int x;
        private final int y;
        private final int z;

        public WorldlessWrapper(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public int getX() {
            return this.x;
        }

        public int getY() {
            return this.y;
        }

        public int getZ() {
            return this.z;
        }

        public int hashCode() {
            return this.x + this.z;
        }

        public SimpleLocation inWorld(String world) {
            return new SimpleLocation(world, this.x, this.y, this.z);
        }

        public SimpleLocation inWorld(World world) {
            return this.inWorld(world.getName());
        }

        public String toString() {
            return "WorldlessSimpleLocation{" + this.x + ", " + this.y + ", " + this.z + '}';
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof WorldlessWrapper)) {
                return false;
            }
            WorldlessWrapper location = (WorldlessWrapper)obj;
            return this.x == location.x && this.y == location.y && this.z == location.z;
        }
    }
}
