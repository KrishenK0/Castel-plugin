package fr.krishenk.castel.utils;

import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import lombok.SneakyThrows;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Base64;

public class JsonItemStack {
    private static final MethodHandle MOJANG_PARSER;
    private static final MethodHandle NBT_TAG_COMPOUND;
    private static final MethodHandle LOAD_FROM_NBT;
    private static final MethodHandle AS_BUKKIT_COPY;
    private static final MethodHandle AS_NMS_COPY;
    private static final MethodHandle SAVE;

    public static ItemStack deserialize(String json) {
        try {
            Object nbtTagCompound = MOJANG_PARSER.invoke(json);
            Object itemStack = LOAD_FROM_NBT.invoke(nbtTagCompound);
            return (ItemStack) AS_BUKKIT_COPY.invoke(itemStack);
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    public static String serialize(ItemStack item) {
        try {
            Object nms = AS_NMS_COPY.invoke(item);
            Object tag = NBT_TAG_COMPOUND.invoke();
            SAVE.invoke(nms, tag);
            return tag.toString();
        }
        catch (Throwable throwable) {
            throwable.printStackTrace();
            return null;
        }
    }

    @SneakyThrows
    public static String encodeInventory(Inventory inventory) {
        String string;
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        BukkitObjectOutputStream data = new BukkitObjectOutputStream((OutputStream)output);
        try {
            data.writeInt(inventory.getSize());
            data.writeChars(((HumanEntity)inventory.getViewers().iterator().next()).getName());
            for (int i = 0; i < inventory.getSize(); ++i) {
                data.writeObject((Object)inventory.getItem(i));
            }
            string = Base64.getEncoder().encodeToString(output.toByteArray());
        }
        catch (Throwable throwable) {
            try {
                try {
                    data.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException ex) {
                ex.printStackTrace();
                return "";
            }
        }
        data.close();
        return string;
    }

    @SneakyThrows
    public static Inventory decodeInventory(String encoded) {
        Inventory inventory;
        ByteArrayInputStream input = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
        BukkitObjectInputStream data = new BukkitObjectInputStream((InputStream)input);
        try {
            Inventory inventory2 = Bukkit.createInventory(null, (int)data.readInt(), (String)data.readObject().toString());
            for (int i = 0; i < inventory2.getSize(); ++i) {
                inventory2.setItem(i, (ItemStack)data.readObject());
            }
            inventory = inventory2;
        }
        catch (Throwable throwable) {
            try {
                try {
                    data.close();
                }
                catch (Throwable throwable2) {
                    throwable.addSuppressed(throwable2);
                }
                throw throwable;
            }
            catch (IOException | ClassNotFoundException ex) {
                ex.printStackTrace();
                return null;
            }
        }
        data.close();
        return inventory;
    }

    static {
        Class<?> nbtCompound = ReflectionUtils.getNMSClass("nbt", "NBTTagCompound");
        Class<?> itemStack = ReflectionUtils.getNMSClass("world.item", "ItemStack");
        Class<?> craftItemStack = ReflectionUtils.getCraftClass("inventory.CraftItemStack");
        MethodHandles.Lookup lookup = MethodHandles.lookup();
        MethodHandle mojangParser = null;
        MethodHandle NBTTagCompound2 = null;
        MethodHandle loadFromNBT = null;
        MethodHandle asBukkitCopy = null;
        MethodHandle asNMSCopy = null;
        MethodHandle save2 = null;
        try {
            mojangParser = lookup.findStatic(ReflectionUtils.getNMSClass("nbt", "MojangsonParser"), ReflectionUtils.v(18, "a").orElse("parse"), MethodType.methodType(nbtCompound, String.class));
            loadFromNBT = ReflectionUtils.supports(13) ? lookup.findStatic(itemStack, "a", MethodType.methodType(itemStack, nbtCompound)) : lookup.findConstructor(itemStack, MethodType.methodType(Void.TYPE, nbtCompound));
            asBukkitCopy = lookup.findStatic(craftItemStack, "asBukkitCopy", MethodType.methodType(ItemStack.class, itemStack));
            asNMSCopy = lookup.findStatic(craftItemStack, "asNMSCopy", MethodType.methodType(itemStack, ItemStack.class));
            NBTTagCompound2 = lookup.findConstructor(nbtCompound, MethodType.methodType(Void.TYPE));
            save2 = lookup.findVirtual(itemStack, ReflectionUtils.v(18, "b").orElse("save"), MethodType.methodType(nbtCompound, nbtCompound));
        }
        catch (IllegalAccessException | NoSuchMethodException e) {
            e.printStackTrace();
        }
        MOJANG_PARSER = mojangParser;
        NBT_TAG_COMPOUND = NBTTagCompound2;
        LOAD_FROM_NBT = loadFromNBT;
        AS_BUKKIT_COPY = asBukkitCopy;
        AS_NMS_COPY = asNMSCopy;
        SAVE = save2;
    }
}
