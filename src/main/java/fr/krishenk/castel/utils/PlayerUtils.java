package fr.krishenk.castel.utils;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.constants.player.CastelPlayer;
import fr.krishenk.castel.events.members.LeaveReason;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.libs.xseries.XItemStack;
import fr.krishenk.castel.libs.xseries.XMaterial;
import fr.krishenk.castel.locale.CastelLang;
import fr.krishenk.castel.utils.string.StringUtils;
import fr.krishenk.castel.utils.versionsupport.VersionSupport;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;

public class PlayerUtils {
    private static final Map<String, fr.krishenk.castel.abstraction.OfflinePlayer> PLAYER_BY_NAME;

    public static void cachePlayer(Player player) {
        PLAYER_BY_NAME.put(player.getName(), new fr.krishenk.castel.abstraction.OfflinePlayer(player.getUniqueId(), player.getName()));
    }

    public static double getMaxPlayerHealth(@NonNull LivingEntity entity) {
        return entity.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    public static void damageArmor(LivingEntity entity, int armorDamage) {
        ItemStack[] armors;
        if (armorDamage == 0) {
            return;
        }
        armorDamage = -armorDamage;
        for (ItemStack armor : armors = entity.getEquipment().getArmorContents()) {
            if (armor == null || armor.getType() == Material.AIR) continue;
            VersionSupport.addDurability(armor, armorDamage);
        }
        entity.getEquipment().setArmorContents(armors);
    }

    public static void sendBlockChange(@NonNull Player player, @NonNull Location loc, @NonNull XMaterial xMat) {
        Material mat = xMat.parseMaterial();
        if (XMaterial.supports(13)) {
            BlockData blockData = mat.createBlockData();
            if (blockData instanceof Directional) {
                Directional direction = (Directional)blockData;
                direction.setFacing(player.getFacing().getOppositeFace());
            }
            player.sendBlockChange(loc, blockData);
        } else {
            player.sendBlockChange(loc, mat, xMat.getData());
        }
    }

    public static void sendBlockChange(@NonNull Player player, @NonNull Block block) {
        Location loc = block.getLocation();
        if (XMaterial.supports(13)) {
            player.sendBlockChange(loc, block.getBlockData());
        } else {
            player.sendBlockChange(loc, block.getType(), block.getData());
        }
    }

    public static boolean invulnerableGameMode(@NonNull Player player) {
        GameMode gameMode = player.getGameMode();
        return gameMode == GameMode.CREATIVE || gameMode == GameMode.SPECTATOR;
    }

    private static boolean removeElytra(Player player) {
        PlayerInventory inv = player.getInventory();
        ItemStack chestplate = inv.getChestplate();
        if (chestplate == null || !XMaterial.ELYTRA.isSimilar(chestplate)) {
            return false;
        }
        inv.setChestplate(null);
        XItemStack.giveOrDrop(player, chestplate);
        return true;
    }

    public static int findEmptyHotbarSlot(Player player) {
        PlayerInventory inv = player.getInventory();
        for (int slot = 0; slot < 8; ++slot) {
            if (inv.getItem(slot) != null) continue;
            return slot;
        }
        return -1;
    }

    public static ItemStack getHotbarItem(Player player, int slot) {
        if ((slot < 0 || slot > 8) && slot != -1) {
            throw new IllegalArgumentException("Unknown hotbar slot: " + slot + " (from " + player.getName() + ')');
        }
        PlayerInventory inv = player.getInventory();
        if (slot == -1) {
            return inv.getItemInOffHand();
        }
        return inv.getItem(slot);
    }

    public static OfflinePlayer getFirstPlayerThat(Predicate<OfflinePlayer> predicate) {
        Objects.requireNonNull(predicate);
        for (OfflinePlayer offlinePlayer : PLAYER_BY_NAME.values()) {
            if (!predicate.test(offlinePlayer)) continue;
            return offlinePlayer;
        }
        return null;
    }

    public static String getLocale(Player player) {
        if (ReflectionUtils.supports(12)) {
            return player.getLocale();
        }
        return null;
    }

    public static @Nullable OfflinePlayer getOfflinePlayer(@NonNull String name) {
        int len = name.length();
        if (len <= 16) {
            return PLAYER_BY_NAME.get(name);
        }
        return null;
    }

    public static OfflinePlayer getOfflinePlayerWarn(Player player, String name) {
        OfflinePlayer offline = PlayerUtils.getOfflinePlayer(name);
        if (offline == null) {
            CastelLang.NOT_FOUND_PLAYER.sendError((CommandSender)player, "name", name);
        }
        return offline;
    }

    public static @Nullable Player getPlayer(@NonNull String name, boolean exact) {
        Player player = Bukkit.getPlayerExact((String)name);
        if (exact || player != null) {
            return player;
        }
        String lowerName = StringUtils.toLatinLowerCase(name);
        int len = lowerName.length();
        if (len > 16) {
            return null;
        }
        int delta = Integer.MAX_VALUE;
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!StringUtils.toLatinLowerCase(online.getName()).startsWith(lowerName)) continue;
            int difference = Math.abs(online.getName().length() - len);
            if (difference < delta) {
                player = online;
                delta = difference;
            }
            if (difference != 0) continue;
            break;
        }
        return player;
    }

    public static boolean hasFullHealth(@NonNull LivingEntity entity) {
        return entity.getHealth() == PlayerUtils.getMaxPlayerHealth(entity);
    }

    public static boolean isEffectivelyInvisible(Player player) {
        if (!player.hasPotionEffect(PotionEffectType.INVISIBILITY)) {
            return false;
        }
        for (ItemStack armor : player.getInventory().getArmorContents()) {
            if (armor == null) continue;
            return false;
        }
        return true;
    }

    public static String validateOfflineName(OfflinePlayer player) {
        String name = player.getName();
        if (name == null) {
            CLogger.error("Unknown offline player " + player.getUniqueId() + " (" + player.getClass().getName() + ") due to corrupted data. This is not a kingdoms bug. Player with this UUID was never seen in the server before. Probably because a data reset. Attempting to fix...");
            CastelPlayer kp = CastelPlayer.getCastelPlayer(player);
            if (kp.hasGuild()) {
                kp.leaveGuild(LeaveReason.CUSTOM);
            } else {
                CLogger.error(player.getUniqueId() + " failed. Player was not in a kingdom.");
            }
            throw new NullPointerException();
        }
        return name;
    }

    public static LivingEntity getTargetedEntity(Player player, double minRange) {
        double range = 20.0;
        LivingEntity target = null;
        BlockIterator bi = new BlockIterator((LivingEntity)player, (int)range);
        while (true) {
            double d = minRange;
            minRange = d - 1.0;
            if (!(d > 0.0) || !bi.hasNext()) break;
            bi.next();
        }
        while (bi.hasNext()) {
            Block b = bi.next();
            int bx = b.getX();
            int by = b.getY();
            int bz = b.getZ();
            for (Entity e : player.getNearbyEntities(range, range, range)) {
                if (!(e instanceof LivingEntity)) continue;
                Location l = e.getLocation();
                double ex = l.getX();
                double ey = l.getY();
                double ez = l.getZ();
                if (!((double)bx - 0.75 <= ex) || !(ex <= (double)bx + 1.75) || !((double)bz - 0.75 <= ez) || !(ez <= (double)bz + 1.75) || !((double)(by - 1) <= ey) || !(ey <= (double)by + 2.5)) continue;
                return target;
            }
        }
        return null;
    }

    static {
        OfflinePlayer[] offlinePlayers = Bukkit.getOfflinePlayers();
        PLAYER_BY_NAME = new HashMap<String, fr.krishenk.castel.abstraction.OfflinePlayer>(offlinePlayers.length);
        for (OfflinePlayer player : offlinePlayers) {
            if (player.getName() == null) continue;
            PLAYER_BY_NAME.put(player.getName(), new fr.krishenk.castel.abstraction.OfflinePlayer(player.getUniqueId(), player.getName()));
        }
    }
}


