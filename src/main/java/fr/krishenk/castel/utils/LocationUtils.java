package fr.krishenk.castel.utils;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.libs.xseries.XBlock;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.compiler.builders.LanguageEntryWithContext;
import fr.krishenk.castel.locale.messenger.DefaultedMessenger;
import fr.krishenk.castel.locale.messenger.LanguageEntryMessenger;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.locale.messenger.StaticMessenger;
import fr.krishenk.castel.locale.provider.MessageBuilder;
import fr.krishenk.castel.utils.string.StringUtils;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Function;
import java.util.function.Predicate;

public class LocationUtils {
    private static final BlockFace[] AXIS = new BlockFace[]{BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    private static final BlockFace[] AXIS2 = new BlockFace[]{BlockFace.UP, BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST, BlockFace.DOWN};
    private static final BlockFace[] RADIAL = new BlockFace[]{BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    public static void faceOther(LivingEntity entity, LivingEntity other) {
        entity.getLocation().setDirection(entity.getLocation().toVector().subtract(other.getLocation().toVector()));
    }

    public static BlockFace[] getAxis() {
        return AXIS;
    }

    public static BlockFace[] getAxis2() {
        return AXIS2;
    }

    public static BlockFace[] getRadial() {
        return RADIAL;
    }

    public static boolean equalsIgnoreWorld(Block block, Block other) {
        return block.getX() == other.getX() && block.getY() == other.getY() && block.getZ() == other.getZ();
    }

    public static Location roundLocationPrecision(Location location) {
        Location loc = location.clone();
        loc.setX(MathUtils.roundToDigits(loc.getX(), 1));
        loc.setY(MathUtils.roundToDigits(loc.getY(), 1));
        loc.setZ(MathUtils.roundToDigits(loc.getZ(), 1));
        loc.setYaw((float) MathUtils.roundToDigits(loc.getYaw(), 1));
        loc.setPitch((float) MathUtils.roundToDigits(loc.getPitch(), 1));
        return loc;
    }

    public static String toReadableLocation(Location location) {
        World world = location.getWorld();
        return (world == null ? "~~Unknown~~" : world.getName()) + ", " + location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    public static void knockback(Vector center, Entity target, double force, double upwardsForce) {
        if (force == 0.0) {
            return;
        }
        Vector vector = target.getLocation().toVector().subtract(center).normalize().multiply(force);
        if (upwardsForce > 0.0 && vector.getY() <= 5.0) {
            vector.setY(upwardsForce);
        }
        target.setVelocity(vector);
    }

    public static String toReadableLoc(Location location) {
        return location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
    }

    public static Block getRelative(Block block, BlockFace... facings) {
        int modX = 0;
        int modY = 0;
        int modZ = 0;
        for (BlockFace facing : facings) {
            modX += facing.getModX();
            modY += facing.getModY();
            modZ += facing.getModZ();
        }
        return block.getRelative(modX, modY, modZ);
    }

    public static Object[] getChunkEdits(SimpleChunkLocation chunk) {
        return new Object[]{"world", chunk.getWorld(), "translated-world", LocationUtils.translateWorld(chunk.getWorld()), "x", chunk.getX(), "z", chunk.getZ()};
    }

    public static MessageBuilder getChunkEdits(MessageBuilder builder, SimpleChunkLocation chunk, String prefix) {
        return builder.raws(prefix + "world", chunk.getWorld(), prefix + "translated-world", LocationUtils.translateWorld(chunk.getWorld()), prefix + 'x', chunk.getX(), prefix + 'z', chunk.getZ());
    }
//
    public static MessageBuilder getLocationEdits(SimpleLocation loc, String prefix) {
        return LocationUtils.getLocationEdits(new MessageBuilder(), loc, prefix);
    }
//
    public static String parseLocation(SimpleLocation location) {
        return Lang.LOCATIONS_NORMAL.parse(LocationUtils.getLocationEdits(location, ""));
    }

    public static String parseLocation(Location loc) {
        MessageBuilder builder = new MessageBuilder().raws("world", loc.getWorld(), "translated-world", LocationUtils.translateWorld(Objects.requireNonNull(loc.getWorld()).getName()), 'x', loc.getX(), 'y', loc.getY(), 'z', loc.getZ());
        return Lang.LOCATIONS_NORMAL.parse(builder);
    }

    public static String parseChunk(SimpleChunkLocation location) {
        return Lang.LOCATIONS_CHUNK.parse(LocationUtils.getChunkEdits(location));
    }

    public static MessageBuilder getLocationEdits(MessageBuilder builder, SimpleLocation loc, String prefix) {
        return builder.raws(prefix + "world", loc.getWorld(), prefix + "translated-world", LocationUtils.translateWorld(loc.getWorld()), prefix + 'x', loc.getX(), prefix + 'y', loc.getY(), prefix + 'z', loc.getZ());
    }

    public static LanguageEntryWithContext locationMessenger(SimpleLocation loc) {
        return new LanguageEntryWithContext(Lang.LOCATIONS_NORMAL, LocationUtils.getLocationEdits(loc, ""));
    }

    public static Messenger translateWorld(String world) {
        return new DefaultedMessenger(new LanguageEntryMessenger("worlds", world), new StaticMessenger(world));
    }

    public static Location centerAxis(Location location) {
        Location loc = LocationUtils.cloneLocation(location);
        loc.setX((double) loc.getBlockX() + 0.5);
        loc.setY(loc.getBlockY());
        loc.setZ((double) loc.getBlockZ() + 0.5);
        return loc;
    }

    public static double distanceSquared(Location start, Location end) {
        return Math.sqrt(NumberConversions.square(start.getX() - end.getX()) + NumberConversions.square(start.getY() - end.getY()) + NumberConversions.square(start.getZ() - end.getZ()));
    }

    public static double averageDistanceBetween(SimpleChunkLocation main, Collection<SimpleChunkLocation> chunks) {
        int filtered = 0;
        double sum = 0.0;
        for (SimpleChunkLocation chunk : chunks) {
            if (!main.getWorld().equals(chunk.getWorld()) || !main.equalsIgnoreWorld(chunk)) continue;
            ++filtered;
            sum += main.distanceIgnoreWorld(chunk);
        }
        return sum / (double) filtered;
    }

    public static boolean areFurtherThan(Location start, Location end, double distance) {
        return LocationUtils.distanceSquared(start, end) > distance;
    }

    public static Location centerView(Location location) {
        Location loc = location.clone();
        loc.setYaw(LocationUtils.centerYaw(loc.getYaw()));
        loc.setPitch(0.0f);
        return loc;
    }

    public static boolean exceedsBuildLimit(Location location) {
        return location.getY() >= (double) location.getWorld().getMaxHeight();
    }

    public static boolean exceedsBuildLimit(PlayerInteractEvent event) {
        return event.getBlockFace() == BlockFace.UP && event.getClickedBlock().getY() + 1 >= event.getClickedBlock().getWorld().getMaxHeight();
    }

    private static double getPlayerFacingPI(Vector direction) {
        double result;
        double x = direction.getX();
        double z = direction.getZ();
        double halfPI = 1.5707963267948966;
        if (x > 0.0 && z > 0.0) {
            result = halfPI * x;
        } else if (x > 0.0 && z < 0.0) {
            result = halfPI * -z + halfPI;
        } else if (x < 0.0 && z < 0.0) {
            result = halfPI * -x + Math.PI;
        } else if (x < 0.0 && z > 0.0) {
            result = halfPI * z + halfPI * 3.0;
        } else {
            throw new AssertionError();
        }
        return result;
    }

    public static Location cleanLocation(Location location) {
        Location loc = LocationUtils.cloneLocation(location);
        loc = LocationUtils.centerAxis(loc);
        loc = LocationUtils.centerView(loc);
        return loc;
    }

    public static Block findEmptyBlock(Block block) {
        if (LocationUtils.blockCanBeReplaced(block)) {
            return block;
        }
        int radius = 3;
        int minY = Math.min(block.getY(), radius);
        int maxY = block.getY() <= radius ? radius + block.getY() : radius;
        SimpleChunkLocation insideChunk = SimpleChunkLocation.of(block);
        for (int x = -radius; x < radius; ++x) {
            for (int y = minY; y < maxY; ++y) {
                for (int z = -radius; z < radius; ++z) {
                    Block relative;
                    if (!insideChunk.isInChunk(block.getLocation()) || !LocationUtils.blockCanBeReplaced(relative = block.getRelative(x, y, z)))
                        continue;
                    return relative;
                }
            }
        }
        return null;
    }

    public static Block findBlock(Block center, int radius, Predicate<Block> predicate) {
        for (int x = -radius; x < radius; ++x) {
            for (int y = -radius; y < radius; ++y) {
                for (int z = -radius; z < radius; ++z) {
                    Block current = center.getRelative(x, y, z);
                    if (!predicate.test(current)) continue;
                    return current;
                }
            }
        }
        return null;
    }

    @Nonnull
    private static Location cloneLocation(@Nonnull Location location) {
        return new Location(location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
    }

    public static Location fromString(String location) {
        Validate.notEmpty(location, "Location string cannot be null or empty");
        List<String> split = StringUtils.cleanSplit(location, ',');
        Validate.isTrue(split.size() >= 4, "Invalid location format (Less than 4 elements): " + location);
        String world = split.get(0);
        double x = Double.parseDouble(split.get(1));
        double y = Double.parseDouble(split.get(2));
        double z = Double.parseDouble(split.get(3));
        float yaw = Float.parseFloat(split.get(4));
        float pitch = Float.parseFloat(split.get(5));
        return new Location(Bukkit.getWorld(world), x, y, z, yaw, pitch);
    }

    public static String toString(Location location) {
        World world = location.getWorld();
        return (world == null ? "" : world.getName()) + ", " + location.getX() + ", " + location.getY() + ", " + location.getZ() + ", " + location.getYaw() + ", " + location.getPitch();
    }

    public static Location getPreciseLocation(Location location) {
        Location loc = location.clone();
        loc = LocationUtils.centerAxis(loc);
        loc.setYaw((float) MathUtils.roundToDigits(loc.getYaw(), 1));
        loc.setPitch((float) MathUtils.roundToDigits(loc.getPitch(), 1));
        return loc;
    }

    public static @Nullable Location getSafeLocation(Location loc) {
        return LocationUtils.getSafeLocation(loc, 6);
    }

    public static @Nullable Location getSafeLocation(Location loc, int maxRange) {
        Location clone = loc.clone().add(0.0, 1.0, 0.0);
        Block block = clone.getBlock();
        if (LocationUtils.isPassableAndNotDangerous(block) && LocationUtils.isPassableAndNotDangerous(block.getRelative(BlockFace.UP))) {
            return clone;
        }
        for (int radius = 1; radius < maxRange; ++radius) {
            for (int x = -radius; x < radius; ++x) {
                for (int y = -radius; y < radius; ++y) {
                    for (int z = -radius; z < radius; ++z) {
                        Block relative = block.getRelative(x, y, z);
                        if (!LocationUtils.isPassableAndNotDangerous(relative) || !LocationUtils.isPassableAndNotDangerous(relative.getRelative(BlockFace.UP)))
                            continue;
                        return relative.getLocation().add(0.5, 0.0, 0.5);
                    }
                }
            }
        }
        return null;
    }

    public static boolean isPassableAndNotDangerous(Block block) {
        if (!LocationUtils.blockCanBeReplaced(block)) {
            return false;
        }
        return !XBlock.isDangerous(XMaterial.matchXMaterial(block.getType()));
    }

    public static void attemptSafeLocation(int minRadius, int yRadius, int maxRadius, int attempts, Location around, Function<Location, Boolean> callback) {
        Bukkit.getScheduler().runTaskAsynchronously(CastelPlugin.getInstance(), () -> {
            Location loc;
            int attempt;
            boolean acceptsMore;
            ThreadLocalRandom random = ThreadLocalRandom.current();
            do {
                attempt = attempts;
                while (!LocationUtils.isSafeLocation(loc = around.clone().add(random.nextInt(minRadius, maxRadius) * (random.nextBoolean() ? 1 : -1), 0.0, random.nextInt(minRadius, maxRadius) * (random.nextBoolean() ? 1 : -1))) && attempt-- > 0) {
                }
            } while (acceptsMore = callback.apply(attempt <= 0 ? null : loc).booleanValue());
        });
    }

    public static boolean isSafeLocation(Location location) {
        Block feet = location.getBlock();
        if (!feet.getType().isTransparent() && !feet.getLocation().add(0.0, 1.0, 0.0).getBlock().getType().isTransparent()) {
            return false;
        }
        Block head = feet.getRelative(BlockFace.UP);
        if (!head.getType().isTransparent()) {
            return false;
        }
        Block ground = feet.getRelative(BlockFace.DOWN);
        return ground.getType().isSolid();
    }

    public static boolean blockCanBeReplaced(@NonNull Block block) {
        String name = block.getType().name();
        if (name.endsWith("DOOR")) {
            return false;
        }
        if (ReflectionUtils.supports(13)) {
            return block.isPassable();
        }
        return block.getType() == Material.AIR || name.endsWith("WATER") || name.endsWith("LAVA") || name.equals("GRASS");
    }

    public static boolean hasMoved(Location from, Location to) {
        return from.getBlockX() != to.getBlockX() || from.getBlockY() != to.getBlockY() || from.getBlockZ() != to.getBlockZ();
    }

    public static BlockFace yawToFaceRadial(float yaw) {
        return RADIAL[Math.round(yaw / 45.0f) & 7];
    }

    public static BlockFace yawToFace(float yaw) {
        return AXIS[Math.round(yaw / 90.0f) & 3];
    }

    public static float centerYaw(float yaw) {
        return LocationUtils.getFaceToYaw(LocationUtils.yawToFace(yaw));
    }

    public static float getFaceToYaw(BlockFace face) {
        switch (face) {
            case NORTH_EAST: {
                return 45.0f;
            }
            case EAST: {
                return 90.0f;
            }
            case SOUTH_EAST: {
                return 135.0f;
            }
            case SOUTH: {
                return 180.0f;
            }
            case SOUTH_WEST: {
                return 225.0f;
            }
            case WEST: {
                return 270.0f;
            }
            case NORTH_WEST: {
                return 315.0f;
            }
        }
        return 0.0f;
    }

    public static BlockFace getPitchToFace(float pitch) {
        return pitch < -25.0f ? BlockFace.UP : (pitch > 25.0f ? BlockFace.DOWN : null);
    }

    public static float compareDirection(Location first, Location second) {
        return (float) Math.toDegrees(Math.atan2((double) first.getBlockX() - second.getX(), second.getZ() - (double) first.getBlockZ()));
    }

    public static BlockFace getPlayerBlockFace(Player player) {
        List lastTwoTargetBlocks = player.getLastTwoTargetBlocks(null, 5);
        if (lastTwoTargetBlocks.size() != 2) {
            return null;
        }
        Block targetBlock = (Block) lastTwoTargetBlocks.get(1);
        Block adjacentBlock = (Block) lastTwoTargetBlocks.get(0);
        return targetBlock.getFace(adjacentBlock);
    }

    public static BlockFace getFace(Location location) {
        float yaw = (float) Math.toDegrees(Math.atan2((double) location.getBlockX() - location.getX(), location.getZ() - (double) location.getBlockZ()));
        if ((yaw %= 360.0f) < 0.0f) {
            yaw += 360.0f;
        }
        yaw = Math.round(yaw / 45.0f);
        switch ((int) yaw) {
            case 1:
            case 2:
            case 3: {
                return BlockFace.NORTH;
            }
            case 4: {
                return BlockFace.EAST;
            }
            case 5:
            case 6:
            case 7: {
                return BlockFace.SOUTH;
            }
        }
        return BlockFace.WEST;
    }

    public static BlockFace getYawToFace(float yaw) {
        if ((yaw %= 360.0f) < 0.0f) {
            yaw += 360.0f;
        }
        yaw = Math.round(yaw / 45.0f);
        switch ((int) yaw) {
            case 1: {
                return BlockFace.NORTH_WEST;
            }
            case 2: {
                return BlockFace.NORTH;
            }
            case 3: {
                return BlockFace.NORTH_EAST;
            }
            case 4: {
                return BlockFace.EAST;
            }
            case 5: {
                return BlockFace.SOUTH_EAST;
            }
            case 6: {
                return BlockFace.SOUTH;
            }
            case 7: {
                return BlockFace.SOUTH_WEST;
            }
        }
        return BlockFace.WEST;
    }

    public boolean worldEquals(World world, World other) {
        return Objects.equals(world.getUID(), other.getUID());
    }
}


