package fr.krishenk.castel.utils;

import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.lang.Lang;
import fr.krishenk.castel.locale.messenger.Messenger;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Optional;

public enum Compass {
    NORTH,
    NORTH_EAST,
    EAST,
    SOUTH_EAST,
    SOUTH,
    WEST,
    SOUTH_WEST,
    NORTH_WEST;
    private final Lang language = Lang.valueOf("CARDINAL_DIRECTIONS_"+this.name());
    private final BlockFace blockFace = BlockFace.valueOf(this.name());
    public static final Compass[] CARDINAL_DIRECTIONS = Compass.values();


    public static Optional<Compass> getCardinalDirection(CastelPlayer cp, String direction) {
        return Arrays.stream(CARDINAL_DIRECTIONS).filter(x -> x.language.parse(cp.getOfflinePlayer()).toLowerCase().equals(direction)).findFirst();
    }

    public Lang getLanguage() {
        return this.language;
    }

    public static Compass getCardinalDirection(float degrees) {
        if ((degrees %= 360.0f) < 0.0f) {
            degrees += 360.0f;
        }
        if (0.0f <= degrees && degrees < 22.5f) {
            return SOUTH;
        }
        if (22.5f <= degrees && degrees < 67.5f) {
            return SOUTH_WEST;
        }
        if (67.5f <= degrees && degrees < 112.5f) {
            return WEST;
        }
        if (112.5f <= degrees && degrees < 157.5f) {
            return NORTH_WEST;
        }
        if (157.5f <= degrees && degrees < 202.5f) {
            return NORTH;
        }
        if (202.5f <= degrees && degrees < 247.5f) {
            return SOUTH_EAST;
        }
        if (247.5f <= degrees && degrees < 292.5f) {
            return EAST;
        }
        if (292.5f <= degrees && degrees < 337.5f) {
            return NORTH_EAST;
        }
        if (337.5f <= degrees && degrees < 360.0f) {
            return SOUTH;
        }
        throw new AssertionError("Unexpected degrees for cardinal direction: " + degrees);
    }

    public static Compass getCardinalDirection(Entity entity) {
        return Compass.getCardinalDirection(entity.getLocation());
    }

    public static Compass getCardinalDirection(Location location) {
        return Compass.getCardinalDirection(location.getYaw());
    }

    public static Messenger translateCardinalDirection(Player player) {
        return Compass.getCardinalDirection(player).language;
    }

    public BlockFace toBlockFace() {
        return this.blockFace;
    }

    public String toString() {
        return StringUtils.capitalize(this.name());
    }
}
