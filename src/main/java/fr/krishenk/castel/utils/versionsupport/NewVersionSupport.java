package fr.krishenk.castel.utils.versionsupport;

import fr.krishenk.castel.utils.LocationUtils;
import fr.krishenk.castel.utils.string.StringUtils;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Rotatable;
import org.bukkit.block.data.type.Door;
import org.bukkit.block.data.type.WallSign;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.Nonnull;

public class NewVersionSupport {
    NewVersionSupport() {
    }

    protected static void rotate(Block block, Material material, Player player) {
        block.setType(material);
        BlockData data = block.getBlockData();
        if (data instanceof Rotatable) {
            Rotatable skullRotation = (Rotatable)data;
            BlockFace facing = LocationUtils.getPlayerBlockFace(player);
            if (facing != null && !StringUtils.contains(facing.name(), '-') && facing != BlockFace.UP && facing != BlockFace.DOWN) {
                skullRotation.setRotation(facing.getOppositeFace());
                block.setBlockData((BlockData)skullRotation);
            }
        }
    }

    protected static Block getOtherHalfIfDoor(BlockState state) {
        BlockData data = state.getBlockData();
        if (!(data instanceof Door)) {
            return null;
        }
        Door door = (Door)data;
        return state.getBlock().getRelative(door.getHalf() == Bisected.Half.TOP ? BlockFace.DOWN : BlockFace.UP);
    }

    protected static @Nullable Block getAttachedBlock(@Nonnull Block sign) {
        BlockData data = sign.getBlockData();
        if (!(data instanceof WallSign)) {
            return null;
        }
        WallSign signData = (WallSign)data;
        BlockFace facing = signData.getFacing().getOppositeFace();
        return sign.getRelative(facing);
    }

    protected static void putSign(Block block, BlockFace facing) {
        WallSign sign = (WallSign)block.getBlockData();
        sign.setFacing(facing);
        block.setBlockData((BlockData)sign);
    }

    protected static void addDurability(@NonNull ItemStack item, int durability) {
        ItemMeta meta = item.getItemMeta();
        if (!(meta instanceof Damageable)) {
            throw new IllegalArgumentException("Cannot set durability of an item that is not damageable: " + (Object)item.getType());
        }
        Damageable damageable = (Damageable)meta;
        damageable.setDamage(damageable.getDamage() + durability);
    }
}


