package fr.krishenk.castel.utils.versionsupport;

import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.utils.Compass;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.Nonnull;

public class VersionSupport {
    private static final boolean NEW;
    private static final boolean SUPPORTS_MIN_HEIGHT;

    public static void rotate(Block block, Material material, Player player) {
        if (NEW) {
            NewVersionSupport.rotate(block, material, player);
        } else {
            OldVersionSupport.rotate(block, material, player);
        }
    }

    public static void putSign(Block sign, BlockFace facing) {
        if (NEW) {
            NewVersionSupport.putSign(sign, facing);
        } else {
            OldVersionSupport.putSign(sign, facing);
        }
    }

    public static void addDurability(@NonNull ItemStack item, int durability) {
        if (NEW) {
            NewVersionSupport.addDurability(item, durability);
        } else {
            OldVersionSupport.addDurability(item, durability);
        }
    }

    public static @Nullable Block getAttachedBlock(@Nonnull Block sign) {
        if (NEW) {
            return NewVersionSupport.getAttachedBlock(sign);
        }
        return OldVersionSupport.getAttachedBlock(sign);
    }

    public static BlockFace getPlayerFacing(Player player) {
        if (NEW) {
            return player.getFacing();
        }
        return Compass.getCardinalDirection((Entity)player).toBlockFace();
    }

    public static Block getOtherHalfIfDoor(BlockState state) {
        if (NEW) {
            return NewVersionSupport.getOtherHalfIfDoor(state);
        }
        return OldVersionSupport.getOtherHalfIfDoor(state);
    }

    public static int getMinWorldHeight(World world) {
        if (SUPPORTS_MIN_HEIGHT) {
            return world.getMinHeight();
        }
        return 0;
    }

    static {
        boolean minSupport;
        NEW = ReflectionUtils.supports(13);
        try {
            Class.forName("org.bukkit.World").getMethod("getMinHeight", new Class[0]);
            minSupport = true;
        }
        catch (ClassNotFoundException | NoSuchMethodException e) {
            minSupport = false;
        }
        SUPPORTS_MIN_HEIGHT = minSupport;
    }
}


