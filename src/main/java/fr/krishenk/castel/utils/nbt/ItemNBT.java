package fr.krishenk.castel.utils.nbt;

import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.libs.xseries.XMaterial;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;

public class ItemNBT {
    public static final boolean CAN_ACCESS_UNBREAKABLE = ReflectionUtils.supports(11);
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle SET_TAG;
    private static final MethodHandle GET_TAG;

    private ItemNBT() {
    }

    private static Object asNMSCopy(ItemStack item) {
        try {
            return AS_NMS_COPY.invoke(item);
        } catch (Throwable var2) {
            var2.printStackTrace();
            return null;
        }
    }

    private static ItemStack asBukkitCopy(Object nmsItem) {
        try {
            return (ItemStack) AS_BUKKIT_COPY.invoke(nmsItem);
        } catch (Throwable var2) {
            var2.printStackTrace();
            return null;
        }
    }

    public static ItemStack setTag(ItemStack item, NBTWrappers.NBTTagCompound tag) {
        Object nbtTag = tag.toNBT();
        Object nmsItem = asNMSCopy(item);

        try {
            SET_TAG.invoke(nmsItem, nbtTag);
        } catch (Throwable var5) {
            var5.printStackTrace();
        }

        return asBukkitCopy(nmsItem);
    }

    public static NBTWrappers.NBTTagCompound getTag(ItemStack item) {
        Object nmsItem = asNMSCopy(item);
        Object tag = null;

        try {
            tag = GET_TAG.invoke(nmsItem);
        } catch (Throwable var4) {
            var4.printStackTrace();
        }

        if (tag == null) {
            return new NBTWrappers.NBTTagCompound();
        } else {
            NBTWrappers.NBTTagCompound base = NBTWrappers.NBTTagCompound.fromNBT(tag);
            return base == null ? new NBTWrappers.NBTTagCompound() : base;
        }
    }

    public static ItemStack addSimpleTag(ItemStack item, String tag, String value) {
        NBTWrappers.NBTTagCompound compound = getTag(item);
        compound.setString(tag, value);
        return setTag(item, compound);
    }

    public static ItemStack setUnbreakable(ItemStack item, boolean unbreakable) {
        if (CAN_ACCESS_UNBREAKABLE) {
            ItemMeta meta = item.getItemMeta();
            meta.setUnbreakable(unbreakable);
            item.setItemMeta(meta);
            return item;
        } else {
            NBTWrappers.NBTTagCompound tag = getTag(item);
            tag.set("Unbreakable", NBTType.BOOLEAN, unbreakable);
            return setTag(item, tag);
        }
    }

    public static void setCanPlaceOnAndDestroy(NBTWrappers.NBTTagCompound tag, Collection<XMaterial> canPlaceOn, Collection<XMaterial> canDestroy) {
        NBTWrappers.NBTTagList<String> canPlaceOnNBT = new NBTWrappers.NBTTagList<>();

        for (XMaterial material : canPlaceOn) {
            if (material.isSupported()) {
                canPlaceOnNBT.add(new NBTWrappers.NBTTagString("minecraft:" + material.parseMaterial().name().toLowerCase(Locale.ENGLISH)));
            }
        }

        NBTWrappers.NBTTagList<String> canDestroyNBT = new NBTWrappers.NBTTagList<>();

        for (XMaterial material : canDestroy) {
            if (material.isSupported()) {
                canDestroyNBT.add(new NBTWrappers.NBTTagString("minecraft:" + material.parseMaterial().name().toLowerCase(Locale.ENGLISH)));
            }
        }

        tag.set("CanDestroy", canDestroyNBT);
        tag.set("CanPlaceOn", canPlaceOnNBT);
    }

    public static boolean isCastelItem(ItemStack item) {
        Object nmsItem = asNMSCopy(item);
        Object tag = null;

        try {
            tag = GET_TAG.invoke(nmsItem);
        } catch (Throwable var4) {
            var4.printStackTrace();
        }

        if (tag == null) {
            return false;
        } else {
            Map<String, Object> components = NBTWrappers.NBTTagCompound.getRawMap(tag);
            return components.containsKey("Castel");
        }
    }

    static {
        MethodHandle asNmsCopy = null;
        MethodHandle asBukkitCopy = null;
        MethodHandle setTag = null;
        MethodHandle getTag = null;
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Class<?> crafItemStack = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
        Class<?> nmsItemStack = ReflectionUtils.getNMSClass("world.item", "ItemStack");
        Class<?> nbtTagCompound = ReflectionUtils.getNMSClass("nbt", "NBTTagCompound");

        try {
            asNmsCopy = lookup.findStatic(crafItemStack, "asNMSCopy", MethodType.methodType(nmsItemStack, ItemStack.class));
            asBukkitCopy = lookup.findStatic(crafItemStack, "asBukkitCopy", MethodType.methodType(ItemStack.class, nmsItemStack));
            setTag = lookup.findVirtual(nmsItemStack, (String)ReflectionUtils.v(18, "c").orElse("setTag"), MethodType.methodType(Void.TYPE, nbtTagCompound));
            getTag = lookup.findVirtual(nmsItemStack, (String)ReflectionUtils.v(19, "v").v(18, "t").orElse("getTag"), MethodType.methodType(nbtTagCompound));
        } catch (IllegalAccessException | NoSuchMethodException var9) {
            var9.printStackTrace();
        }

        AS_NMS_COPY = asNmsCopy;
        AS_BUKKIT_COPY = asBukkitCopy;
        SET_TAG = setTag;
        GET_TAG = getTag;
    }
}
