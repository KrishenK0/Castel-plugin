package fr.krishenk.castel.utils.scoreboards;

import com.google.common.collect.MapMaker;
import fr.krishenk.castel.libs.xseries.ReflectionUtils;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Glow implements Listener {
    private static final Map<Integer, Entity> ENTITY_ID_MAPPING = (new MapMaker()).weakValues().makeMap();
    private static final Map<UUID, Set<Entity>> GLOWS = new HashMap<>();
    private static final Map<UUID, Set<Player>> GLOWING_FOR = new HashMap<>();
    private static final boolean isPaper;
    static Class<?> DataWatcher = ReflectionUtils.getNMSClass("network.syncher", "DataWatcher");
    static Class<?> DataWatcherItem = ReflectionUtils.getNMSClass("network.syncher", "DataWatcher$Item");
    private static final Class<?> DATA_WATCHER_ITEMS_TYPE;
    private static final Class<?> PacketPlayOutEntityMetadata = ReflectionUtils.getNMSClass("network.protocol.game", "PacketPlayOutEntityMetadata");
    private static final Class<?> Entity = ReflectionUtils.getNMSClass("world.entity", "Entity");

    public static void setGlowing(Player receiver, XScoreboard.Color color, Entity ... members) {
        Objects.requireNonNull(members);
        Objects.requireNonNull(receiver);
        Set<Entity> glows = GLOWS.computeIfAbsent(receiver.getUniqueId(), (k) -> new HashSet<>());
        glows.addAll(Arrays.asList(members));
        Set<Player> glowFor = GLOWING_FOR.computeIfAbsent(receiver.getUniqueId(), (k) -> new HashSet<>());
        glowFor.add(receiver);
        for (org.bukkit.entity.Entity entity : members) {
            sendGlowPacket(receiver, true, entity);
        }

        (new XScoreboard()).setColor(color).setMembers(Arrays.asList(members)).build().setScoreboard(receiver);
    }

    public static void removeGlow(Player receiver) {
        Objects.requireNonNull(receiver);
        Set<Entity> entities = GLOWS.remove(receiver.getUniqueId());
        for (org.bukkit.entity.Entity entity : entities) {
            sendGlowPacket(receiver, false, entity);
        }
    }

    public static boolean isGlowing(Entity entity, Player receiver) {
        Set<Player> glows = GLOWING_FOR.get(entity.getUniqueId());
        return glows != null && glows.contains(receiver);
    }

    private static byte magicGlowIndex(boolean glowing, byte flags) {
        return (byte) (glowing ? flags | 64 : flags & -65);
    }

    protected static void sendGlowPacket(Player receiver, boolean glowing, org.bukkit.entity.Entity entity) {
        try {
            ENTITY_ID_MAPPING.put(entity.getEntityId(), entity);
            Method handle = entity.getClass().getMethod("getHandle");
            Object dataWatcher = Entity.getDeclaredMethod("getDataWatcher").invoke(handle.invoke(entity));
            Map<Integer, Object> dataWatcherItems = (Map<Integer, Object>) resolveByLastType(DataWatcher, DATA_WATCHER_ITEMS_TYPE).get(dataWatcher);
            Field field = null;
            for (Field fieldloop : Entity.getDeclaredFields()) {
                if ("DataWatcherObject".equals(fieldloop.getType().getSimpleName())) {
                    field = fieldloop;
                    break;
                }
            }

            if (field == null) {
                throw new IllegalStateException("No field found in " + Entity + " that equals to DataWatcherObject");
            }
            field.setAccessible(true);
            Object dataWatcherObject = field.get(null);
            field = DataWatcherItem.getDeclaredField("b");
            field.setAccessible(true);
            byte flags = dataWatcherItems.isEmpty() ? 0 : (byte) field.get(dataWatcherItems.get(0));
            byte newFlags = magicGlowIndex(glowing, flags);
            Object dataWatcherItem = DataWatcherItem.getConstructors()[0].newInstance(dataWatcherObject, newFlags);
            Object packetMetadata;
            if (ReflectionUtils.supports(17)) {
                packetMetadata = PacketPlayOutEntityMetadata.getConstructor(Integer.TYPE, DataWatcher, Boolean.TYPE).newInstance(-entity.getEntityId(), dataWatcher, true);
                field = PacketPlayOutEntityMetadata.getDeclaredField("b");
                field.setAccessible(true);
                List<Object> dataWatcherList = (List<Object>) field.get(packetMetadata);
                dataWatcherList.clear();
                dataWatcherList.add(dataWatcherItem);
            } else {
                packetMetadata = PacketPlayOutEntityMetadata.newInstance();
                field = PacketPlayOutEntityMetadata.getDeclaredField("a");
                field.setAccessible(true);
                field.set(packetMetadata, -entity.getEntityId());
                List<Object> list = new ArrayList<>(8);
                list.add(dataWatcherItem);
                field = PacketPlayOutEntityMetadata.getDeclaredField("b");
                field.setAccessible(true);
                field.set(packetMetadata, list);
            }

            ReflectionUtils.sendPacketSync(receiver, packetMetadata);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static Field resolveByLastType(Class<?> clazz, Class<?> type) throws ReflectiveOperationException {
        Field field = null;
        for (Field field1 : clazz.getDeclaredFields()) {
            if (field1.getType() == type) field = field1;
        }

        if (field == null) {
            throw new NoSuchFieldException("Could not resolve field of type '" + type.toString() + "' in class " + clazz);
        } else {
            field.setAccessible(true);
            return field;
        }
    }

    static {
        boolean paper;
        try {
            Class.forName("com.destroystokyo.paper.PaperConfig");
            paper = true;
        } catch (ClassNotFoundException e) {
            paper = false;
        }
        isPaper = paper;

        Class<?> dataWatcherItemsType = null;
        try {
            if (!ReflectionUtils.supports(14)) {
                dataWatcherItemsType = Map.class;
            } else if (!ReflectionUtils.supports(17)) {
                if (paper) dataWatcherItemsType = Class.forName("it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap");
                else dataWatcherItemsType = Class.forName("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap");
            } else if (isPaper) {
                dataWatcherItemsType = Class.forName("it.unimi.dsi.fastutil.ints.Int2ObjectMap");
            } else {
                dataWatcherItemsType = Class.forName("org.bukkit.craftbukkit.libs.it.unimi.dsi.fastutil.ints.Int2ObjectMap");
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        DATA_WATCHER_ITEMS_TYPE = dataWatcherItemsType;
//        CLogger.error("Protocol channels opened unexpectedly");
//        throw new IllegalStateException();
    }
}
