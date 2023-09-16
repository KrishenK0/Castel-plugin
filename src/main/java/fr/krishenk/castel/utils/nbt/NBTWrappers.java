package fr.krishenk.castel.utils.nbt;

import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import fr.krishenk.castel.libs.xseries.XMaterial;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class NBTWrappers {
    private NBTWrappers() {
    }

    private static Class<?> getNBTClass(String clazz) {
        return ReflectionUtils.getNMSClass("nbt", clazz);
    }

    private static Field getDeclaredField(Class<?> clazz, String... names) {
        int i = 0;
        int j = 0;
        while(j < names.length) {
            String name = names[j];
            ++i;
            try {
                return clazz.getDeclaredField(name);
            } catch (NoSuchFieldException var8) {
                if (i == names.length) {
                    var8.printStackTrace();
                }
                ++j;
            }
        }
        return null;
    }

    public static final class NBTTagFloat extends NBTNumber<Float> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagFloat(float value) {
            super(value);
        }

        public static NBTTagFloat fromNBT(Object nbtObject) {
            try {
                return new NBTTagFloat((Float) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public double getAsDouble() {
            return (double) this.value;
        }

        public float getAsFloat() {
            return this.value;
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(this.getAsFloat());
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagFloat{" + this.value + '}';
        }

        static {
            Class<?> clazz = getNBTClass("NBTTagFloat");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, Float.TYPE));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, Float.TYPE));
                }

                Field field = getDeclaredField(clazz, "w", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public static final class NBTTagLong extends NBTNumber<Long> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagLong(long value) {
            super(value);
        }

        public static NBTTagLong fromNBT(Object nbtObject) {
            try {
                return new NBTTagLong((Long) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public double getAsDouble() {
            return (double) this.value;
        }

        public long getAsLong() {
            return this.value;
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(this.getAsLong());
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagLong{" + this.value + '}';
        }

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> clazz = getNBTClass("NBTTagLong");
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, Long.TYPE));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, Long.TYPE));
                }

                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public static final class NBTTagShort extends NBTNumber<Short> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagShort(short value) {
            super(value);
        }

        public static NBTTagShort fromNBT(Object nbtObject) {
            try {
                return new NBTTagShort((Short) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public short getAsShort() {
            return this.value;
        }

        public double getAsDouble() {
            return (double) this.value;
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(this.getAsShort());
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagShort{" + this.value + '}';
        }

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> clazz = getNBTClass("NBTTagShort");
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, Short.TYPE));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, Short.TYPE));
                }

                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public static final class NBTTagByteArray extends NBTArray<byte[]> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagByteArray(byte[] value) {
            super(value);
        }

        public static NBTTagByteArray fromNBT(Object nbtObject) {
            try {
                return nbtObject == null ? new NBTTagByteArray(new byte[0]) : new NBTTagByteArray((byte[]) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke((byte[])this.value);
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagByteArray{" + Arrays.toString(this.value) + '}';
        }

        static {
            Class<?> clazz = getNBTClass("NBTTagByteArray");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, byte[].class));
                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public static final class NBTTagByte extends NBTNumber<Byte> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagByte(byte value) {
            super(value);
        }

        public static NBTBase<Byte> fromNBT(Object nbtObject) {
            try {
                return new NBTTagByte((Byte) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public double getAsDouble() {
            return (double) this.value;
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(this.getAsByte());
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagByte{" + this.value + '}';
        }

        static {
            Class<?> clazz = getNBTClass("NBTTagByte");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, Byte.TYPE));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, Byte.TYPE));
                }

                Field field = getDeclaredField(clazz, "x", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public static final class NBTTagInt extends NBTNumber<Integer> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagInt(int value) {
            super(value);
        }

        public static NBTTagInt fromNBT(Object nbtObject) {
            try {
                return new NBTTagInt((Integer) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public double getAsDouble() {
            return (double) this.value;
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke((Integer)this.value);
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagInt{" + this.value + '}';
        }

        static {
            Class<?> clazz = getNBTClass("NBTTagInt");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, Integer.TYPE));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, Integer.TYPE));
                }

                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public static final class NBTTagDouble extends NBTNumber<Double> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagDouble(double value) {
            super(value);
        }

        public static NBTTagDouble fromNBT(Object nbtObject) {
            try {
                return new NBTTagDouble((Double) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(this.getAsDouble());
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagDouble{" + this.value + '}';
        }

        public double getAsDouble() {
            return this.value;
        }

        static {
            Class<?> clazz = getNBTClass("NBTTagDouble");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, Double.TYPE));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, Double.TYPE));
                }

                Field field = getDeclaredField(clazz, "w", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public abstract static class NBTNumber<T extends Number> extends NBTBase<T> {
        public NBTNumber(T value) {
            super(value);
        }

        public int getAsInt() {
            return (int)Math.floor(this.getAsDouble());
        }

        public long getAsLong() {
            return (long)Math.floor(this.getAsDouble());
        }

        public abstract double getAsDouble();

        public float getAsFloat() {
            return (float)this.getAsDouble();
        }

        public byte getAsByte() {
            return (byte)(this.getAsInt() & 255);
        }

        public short getAsShort() {
            return (short)(this.getAsInt() & '\uffff');
        }
    }

    public abstract static class NBTArray<T> extends NBTBase<T> {
        public NBTArray(T value) {
            super(value);
        }
    }

    public static final class NBTTagList<T> extends NBTBase<List<NBTBase<T>>> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle GET_DATA;
        private static final MethodHandle SET_DATA;
        private static final MethodHandle GET_TYPE_ID;

        public NBTTagList(List<NBTBase<T>> value) {
            super(value);
        }

        public NBTTagList() {
            super(new ArrayList<>());
        }

        public static NBTTagList<?> fromNBT(Object nbtObject) {
            List nbtList;
            try {
                nbtList = (List) GET_DATA.invoke(nbtObject);
            } catch (Throwable var5) {
                var5.printStackTrace();
                return new NBTTagList<>();
            }

            List<NBTBase<?>> list = new ArrayList<>(nbtList.size());

            for (Object entry : nbtList) {
                list.add(fromNBT(entry));
            }

            return new NBTTagList(list);
        }

        public boolean add(NBTBase<T> base) {
            this.value.add(base);
            return true;
        }

        public boolean isType(NBTBase<?> type) {
            return this.value.isEmpty() || this.value.get(0).getClass().isInstance(type);
        }

        public Object toNBT() {
            try {
                List<Object> array = new ArrayList<>(this.value.size());
                Iterator<NBTBase<T>> var2 = ((List)this.value).iterator();

                while(var2.hasNext()) {
                    NBTBase<T> base = var2.next();
                    array.add(base.toNBT());
                }

                if (XMaterial.supports(15)) {
                    byte typeId = array.isEmpty() ? 0 : (byte) GET_TYPE_ID.invoke(array.get(0));
                    return CONSTRUCTOR.invoke(array, typeId);
                } else {
                    Object nbtList = CONSTRUCTOR.invoke();
                    SET_DATA.invoke(nbtList, array);
                    return nbtList;
                }
            } catch (Throwable var4) {
                var4.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagList{" + Arrays.toString(this.value.toArray()) + '}';
        }

        static {
            Class<?> clazz = getNBTClass("NBTTagList");
            Class<?> nbtBase = getNBTClass("NBTBase");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;
            MethodHandle getData = null;
            MethodHandle setData = null;
            MethodHandle getTypeId = null;

            try {
                Field field = getDeclaredField(clazz, "c", "list");
                field.setAccessible(true);
                getData = lookup.unreflectGetter(field);
                if (XMaterial.supports(15)) {
                    Constructor<?> ctor = clazz.getDeclaredConstructor(List.class, Byte.TYPE);
                    ctor.setAccessible(true);
                    handler = lookup.unreflectConstructor(ctor);
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE));
                    setData = lookup.unreflectSetter(field);
                }

                getTypeId = lookup.findVirtual(nbtBase, ReflectionUtils.v(19, "b").v(18, "a").orElse("getTypeId"), MethodType.methodType(Byte.TYPE));
            } catch (IllegalAccessException | NoSuchMethodException var9) {
                var9.printStackTrace();
            }

            CONSTRUCTOR = handler;
            GET_DATA = getData;
            SET_DATA = setData;
            GET_TYPE_ID = getTypeId;
        }
    }

    public static final class NBTTagIntArray extends NBTArray<int[]> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagIntArray(int[] value) {
            super(value);
        }

        public static NBTTagIntArray fromNBT(Object nbtObject) {
            try {
                return new NBTTagIntArray((int[]) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke((int[])this.value);
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagIntArray{" + Arrays.toString(this.value) + '}';
        }

        static {
            Class<?> clazz = getNBTClass("NBTTagIntArray");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, int[].class));
                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public static final class NBTTagLongArray extends NBTArray<long[]> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagLongArray(long[] value) {
            super(value);
        }

        public static NBTTagLongArray fromNBT(Object nbtObject) {
            try {
                return new NBTTagLongArray((long[]) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke((long[])this.value);
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagLongArray{" + Arrays.toString(this.value) + '}';
        }

        static {
            Class<?> clazz = getNBTClass("NBTTagLongArray");
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, long[].class));
                Field field = getDeclaredField(clazz, "c", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public static final class NBTTagCompound extends NBTBase<Map<String, NBTBase<?>>> implements fr.krishenk.castel.utils.nbt.NBTTagCompound {
        private static final MethodHandle NBT_TAG_COMPOUND_CONSTRUCTOR;
        private static final MethodHandle GET_COMPOUND_MAP;
        private static final MethodHandle SET_COMPOUND_MAP;

        public NBTTagCompound(Map<String, NBTBase<?>> value) {
            super(value);
        }

        public NBTTagCompound(int capacity) {
            this(new HashMap<>(capacity));
        }

        public NBTTagCompound() {
            this(new HashMap<>());
        }

        public static Map<String, Object> getRawMap(Object nbtObject) {
            try {
                return (Map<String, Object>) GET_COMPOUND_MAP.invoke(nbtObject);
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public static NBTTagCompound fromNBT(Object nbtObject) {
            try {
                Map<String, Object> baseMap = getRawMap(nbtObject);
                NBTTagCompound compound = new NBTTagCompound(baseMap.size());
                Iterator<Map.Entry<String, Object>> var3 = baseMap.entrySet().iterator();

                while(var3.hasNext()) {
                    Map.Entry<String, Object> base = var3.next();
                    NBTBase<?> nbtBase = NBTBase.fromNBT(base.getValue());
                    if (nbtBase != null) {
                        compound.set(base.getKey(), nbtBase);
                    }
                }

                return compound;
            } catch (Throwable var6) {
                var6.printStackTrace();
                return null;
            }
        }

        public <T> void set(String key, NBTType<T> type, T value) {
            NBTBase<?> base = null;
            if (type == NBTType.STRING) {
                base = new NBTTagString((String)value);
            } else if (type == NBTType.BYTE) {
                base = new NBTTagByte((Byte)value);
            } else if (type == NBTType.BOOLEAN) {
                base = new NBTTagByte((byte)((Boolean)value ? 1 : 0));
            } else if (type == NBTType.SHORT) {
                base = new NBTTagShort((Short)value);
            } else if (type == NBTType.INTEGER) {
                base = new NBTTagInt((Integer)value);
            } else if (type == NBTType.LONG) {
                base = new NBTTagLong((Long)value);
            } else if (type == NBTType.FLOAT) {
                base = new NBTTagFloat((Float)value);
            } else if (type == NBTType.DOUBLE) {
                base = new NBTTagDouble((Double)value);
            } else if (type == NBTType.BYTE_ARRAY) {
                base = new NBTTagByteArray((byte[])value);
            } else if (type == NBTType.INTEGER_ARRAY) {
                base = new NBTTagIntArray((int[])value);
            } else if (type == NBTType.LONG_ARRAY) {
                base = new NBTTagLong((Long)value);
            }

            ((Map)this.value).put(key, base);
        }

        public <T> T get(String key, NBTType<T> type) {
            NBTBase<T> base = (NBTBase<T>) this.value.get(key);
            return base == null ? null : base.value;
        }

        public <T> boolean has(String key, NBTType<T> type) {
            return this.has(key);
        }

        public boolean has(String key) {
            return this.value.containsKey(key);
        }

        public Object getContainer() {
            return this;
        }

        public void set(String key, NBTBase<?> nbt) {
            this.value.put(key, nbt);
        }

        public <T extends NBTBase<?>> T remove(String key) {
            return (T) this.value.remove(key);
        }

        public NBTBase<?> removeUnchecked(String key) {
            return this.value.remove(key);
        }

        public void setByte(String key, byte value) {
            this.getValue().put(key, new NBTTagByte(value));
        }

        public void setShort(String key, short value) {
            this.getValue().put(key, new NBTTagShort(value));
        }

        public void setInt(String key, int value) {
            this.getValue().put(key, new NBTTagInt(value));
        }

        public void setLong(String key, long value) {
            this.getValue().put(key, new NBTTagLong(value));
        }

        public void setFloat(String key, float value) {
            this.getValue().put(key, new NBTTagFloat(value));
        }

        public void setDouble(String key, double value) {
            this.getValue().put(key, new NBTTagDouble(value));
        }

        public void setString(String key, String value) {
            this.getValue().put(key, new NBTTagString(value));
        }

        public void setStringList(String key, List<String> value) {
            List<NBTBase<String>> strings = new ArrayList<>(value.size());

            for (String val : value) {
                strings.add(new NBTTagString(val));
            }

           this.getValue().put(key, new NBTTagList(strings));
        }

        public void setCompound(String key, NBTTagCompound compound) {
            this.value.put(key, compound);
        }

        public void setByteArray(String key, byte[] value) {
           this.getValue().put(key, new NBTTagByteArray(value));
        }

        public void setIntArray(String key, int[] value) {
           this.getValue().put(key, new NBTTagIntArray(value));
        }

        public void setBoolean(String key, boolean value) {
            this.setByte(key, (byte)(value ? 1 : 0));
        }

        public NBTBase<?> get(String key) {
            return this.value.get(key);
        }

        public byte getByte(String key) {
            NBTBase<?> nbt = this.get(key);
            return !(nbt instanceof NBTTagByte) ? 0 : ((NBTTagByte)nbt).getAsByte();
        }

        public short getShort(String key) {
            NBTBase<?> nbt = this.get(key);
            return !(nbt instanceof NBTTagShort) ? 0 : ((NBTTagShort)nbt).getAsShort();
        }

        public int getInt(String key) {
            NBTBase<?> nbt = this.get(key);
            return !(nbt instanceof NBTTagInt) ? 0 : ((NBTTagInt)nbt).getAsShort();
        }

        public long getLong(String key) {
            NBTBase<?> nbt = this.get(key);
            return !(nbt instanceof NBTTagLong) ? 0L : ((NBTTagLong)nbt).getAsLong();
        }

        public NBTTagCompound getCompound(String key) {
            NBTBase<?> value = this.get(key);
            return !(value instanceof NBTTagCompound) ? null : (NBTTagCompound)value;
        }

        public float getFloat(String key) {
            NBTBase<?> nbt = this.get(key);
            return !(nbt instanceof NBTTagFloat) ? 0.0F : ((NBTTagFloat)nbt).getAsFloat();
        }

        public double getDouble(String key) {
            NBTBase<?> nbt = this.get(key);
            return !(nbt instanceof NBTTagDouble) ? 0.0 : ((NBTTagDouble)nbt).getAsDouble();
        }

        public String getString(String key) {
            NBTBase<?> nbt = this.get(key);
            return !(nbt instanceof NBTTagString) ? null : ((NBTTagString)nbt).getValue();
        }

        public List<String> getStringList(String key) {
            NBTBase<?> nbt = this.get(key);
            if (!(nbt instanceof NBTTagList)) {
                return null;
            } else {
                List<NBTBase<?>> values = (List<NBTBase<?>>) nbt.getValue();
                List<String> strings = new ArrayList<>(values.size());

                for (NBTBase<?> nbtBase : values) {
                    NBTBase<?> val = nbtBase;
                    strings.add(String.valueOf(val.value));
                }

                return strings;
            }
        }

        public byte[] getByteArray(String key) {
            NBTBase<?> nbt = this.get(key);
            return !(nbt instanceof NBTTagByteArray) ? null : ((NBTTagByteArray)nbt).getValue();
        }

        public int[] getIntArray(String key) {
            NBTBase<?> nbt = this.get(key);
            return !(nbt instanceof NBTTagIntArray) ? null : ((NBTTagIntArray)nbt).getValue();
        }

        public boolean getBoolean(String key) {
            return this.getByte(key) != 0;
        }

        public Object toNBT() {
            try {
                Map<String, Object> map = new HashMap<>(this.value.size());

                for (Object o : this.value.entrySet()) {
                    Map.Entry<String, NBTBase<?>> entry = (Map.Entry) o;
                    if (entry.getValue() == this) {
                        throw new IllegalStateException("recursive NBT");
                    }

                    map.put(entry.getKey(), entry.getValue().toNBT());
                }

                Object compound;
                if (XMaterial.supports(15)) {
                    compound = NBT_TAG_COMPOUND_CONSTRUCTOR.invoke(map);
                } else {
                    compound = NBT_TAG_COMPOUND_CONSTRUCTOR.invoke();
                    SET_COMPOUND_MAP.invoke(compound, map);
                }

                return compound;
            } catch (Throwable var4) {
                var4.printStackTrace();
                return null;
            }
        }

        public String toString() {
            StringBuilder builder = new StringBuilder(10 + this.value.size() * 50);
            builder.append("NBTTagCompound{");
            Iterator var2 = ((Map)this.value).entrySet().iterator();

            while(var2.hasNext()) {
                Map.Entry<String, NBTBase<?>> entry = (Map.Entry)var2.next();
                builder.append('\n').append("  ").append(entry.getKey()).append(": ").append(entry.getValue());
            }

            return builder.append('\n').append('}').toString();
        }

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> nbtCompound = getNBTClass("NBTTagCompound");
            MethodHandle handler = null;
            MethodHandle getMap = null;
            MethodHandle setMap = null;

            try {
                Field field = getDeclaredField(nbtCompound, "x", "map");
                field.setAccessible(true);
                getMap = lookup.unreflectGetter(field);
                if (XMaterial.supports(15)) {
                    Constructor<?> ctor = nbtCompound.getDeclaredConstructor(Map.class);
                    ctor.setAccessible(true);
                    handler = lookup.unreflectConstructor(ctor);
                } else {
                    handler = lookup.findConstructor(nbtCompound, MethodType.methodType(Void.TYPE));
                    setMap = lookup.unreflectSetter(field);
                }
            } catch (IllegalAccessException | NoSuchMethodException var7) {
                var7.printStackTrace();
            }

            NBT_TAG_COMPOUND_CONSTRUCTOR = handler;
            GET_COMPOUND_MAP = getMap;
            SET_COMPOUND_MAP = setMap;
        }
    }

    public static final class NBTTagString extends NBTBase<String> {
        private static final MethodHandle CONSTRUCTOR;
        private static final MethodHandle NBT_DATA;

        public NBTTagString(String value) {
            super(value);
        }

        public static NBTTagString fromNBT(Object nbtObject) {
            try {
                return new NBTTagString((String) NBT_DATA.invoke(nbtObject));
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public NBTType<String> getNBTType() {
            return NBTType.STRING;
        }

        public Object toNBT() {
            try {
                return CONSTRUCTOR.invoke(this.value == null ? "" : this.value);
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagString{" + this.value + '}';
        }

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> clazz = getNBTClass("NBTTagString");
            MethodHandle handler = null;
            MethodHandle data = null;

            try {
                if (XMaterial.supports(15)) {
                    handler = lookup.findStatic(clazz, "a", MethodType.methodType(clazz, String.class));
                } else {
                    handler = lookup.findConstructor(clazz, MethodType.methodType(Void.TYPE, String.class));
                }

                Field field = getDeclaredField(clazz, "A", "data");
                field.setAccessible(true);
                data = lookup.unreflectGetter(field);
            } catch (IllegalAccessException | NoSuchMethodException var5) {
                var5.printStackTrace();
            }

            CONSTRUCTOR = handler;
            NBT_DATA = data;
        }
    }

    public static final class NBTTagEnd extends NBTBase<Void> {
        private static final MethodHandle NBT_CONSTRUCTOR;

        public NBTTagEnd() {
            super(null);
        }

        public static NBTBase<Void> fromNBT(Object nbtObject) {
            return null;
        }

        public Object toNBT() {
            try {
                return NBT_CONSTRUCTOR.invoke();
            } catch (Throwable var2) {
                var2.printStackTrace();
                return null;
            }
        }

        public String toString() {
            return "NBTTagEnd";
        }

        static {
            MethodHandles.Lookup lookup = MethodHandles.lookup();
            Class<?> stringClass = getNBTClass("NBTTagEnd");
            MethodHandle handler = null;

            try {
                lookup.findConstructor(stringClass, MethodType.methodType(Void.TYPE));
            } catch (IllegalAccessException | NoSuchMethodException var4) {
                var4.printStackTrace();
            }

            NBT_CONSTRUCTOR = handler;
        }
    }

    public abstract static class NBTBase<T> {
        protected final T value;

        public NBTBase(T value) {
            this.value = value;
        }

        public static NBTBase<?> fromNBT(Object nbtObject) {
            switch (nbtObject.getClass().getSimpleName()) {
                case "NBTTagCompound":
                    return NBTWrappers.NBTTagCompound.fromNBT(nbtObject);
                case "NBTTagString":
                    return NBTWrappers.NBTTagString.fromNBT(nbtObject);
                case "NBTTagByte":
                    return NBTWrappers.NBTTagByte.fromNBT(nbtObject);
                case "NBTTagShort":
                    return NBTWrappers.NBTTagShort.fromNBT(nbtObject);
                case "NBTTagInt":
                    return NBTWrappers.NBTTagInt.fromNBT(nbtObject);
                case "NBTTagLong":
                    return NBTWrappers.NBTTagLong.fromNBT(nbtObject);
                case "NBTTagFloat":
                    return NBTWrappers.NBTTagFloat.fromNBT(nbtObject);
                case "NBTTagDouble":
                    return NBTWrappers.NBTTagDouble.fromNBT(nbtObject);
                case "NBTTagByteArray":
                    return NBTWrappers.NBTTagByteArray.fromNBT(nbtObject);
                case "NBTTagIntArray":
                    return NBTWrappers.NBTTagIntArray.fromNBT(nbtObject);
                case "NBTTagLongArray":
                    return NBTWrappers.NBTTagLongArray.fromNBT(nbtObject);
                case "NBTTagList":
                    return NBTWrappers.NBTTagList.fromNBT(nbtObject);
                case "NBTTagEnd":
                    return NBTWrappers.NBTTagEnd.fromNBT(nbtObject);
                default:
                    throw new UnsupportedOperationException("Unknown NBT type: " + nbtObject.getClass().getSimpleName());
            }
        }

        public final T getValue() {
            return this.value;
        }

        public abstract Object toNBT();
    }
}
