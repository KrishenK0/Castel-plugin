package fr.krishenk.castel.utils.versionsupport;

import org.bukkit.Material;
import org.bukkit.SkullType;
import org.bukkit.block.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.material.Door;
import org.bukkit.material.MaterialData;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import javax.annotation.Nonnull;

public class OldVersionSupport {
    OldVersionSupport() {
    }

    protected static void rotate(Block block, Material material, Player player) {
        if (material.name().contains("SKULL")) {
            block.setType(Material.getMaterial((String)"SKULL"));
            BlockState state = block.getState();
            Skull skull = (Skull)state;
            skull.setSkullType(SkullType.PLAYER);
            skull.setRawData((byte)1);
            state.update(true);
        }
    }

    protected static Block getOtherHalfIfDoor(BlockState state) {
        MaterialData data = state.getData();
        if (!(data instanceof Door)) {
            return null;
        }
        Door door = (Door)data;
        return state.getBlock().getRelative(door.isTopHalf() ? BlockFace.DOWN : BlockFace.UP);
    }

    protected static void putSign(Block block, BlockFace facing) {
        byte data;
        switch (facing) {
            case NORTH: {
                data = 2;
                break;
            }
            case EAST: {
                data = 5;
                break;
            }
            case WEST: {
                data = 4;
                break;
            }
            case SOUTH: {
                data = 3;
                break;
            }
            default: {
                throw new IllegalArgumentException("Sign facing cannot be: " + (Object)facing);
            }
        }
        Sign sign = (Sign)block.getState();
        sign.setRawData(data);
        sign.update(true);
    }

    protected static @Nullable Block getAttachedBlock(@Nonnull Block sign) {
        MaterialData data = sign.getState().getData();
        if (!(data instanceof org.bukkit.material.Sign)) {
            return null;
        }
        org.bukkit.material.Sign signData = (org.bukkit.material.Sign)data;
        BlockFace facing = signData.getAttachedFace();
        return sign.getRelative(facing);
    }

    protected static void addDurability(@NonNull ItemStack item, int durability) {
        item.setDurability((short)(item.getDurability() + durability));
    }
}


