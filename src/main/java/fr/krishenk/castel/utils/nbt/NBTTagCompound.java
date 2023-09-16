package fr.krishenk.castel.utils.nbt;

public interface NBTTagCompound {
    <T> void set(String var1, NBTType<T> var2, T var3);

    <T> T get(String var1, NBTType<T> var2);

    <T> boolean has(String var1, NBTType<T> var2);

    Object getContainer();
}
