package fr.krishenk.castel.utils.internal;

import com.google.common.collect.ImmutableList;
import fr.krishenk.castel.data.Pair;
import fr.krishenk.castel.utils.platform.JavaVersion;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.*;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.RegisteredListener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.NumberConversions;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

public class ProxyBytecodeManipulator {
    public ProxyBytecodeManipulator(Class<?> ... interfaces) {
        Proxy.newProxyInstance(this.getClass().getClassLoader(), interfaces, (proxy, method, args) -> {
            Method m;
            try {
                m = ProxyBytecodeManipulator.this.getClass().getMethod(method.getName(), method.getParameterTypes());
                m.setAccessible(true);
            }
            catch (Exception e) {
                throw new UnsupportedOperationException(method.toString(), e);
            }
            try {
                return m.invoke(ProxyBytecodeManipulator.this, args);
            }
            catch (InvocationTargetException e) {
                throw e.getCause();
            }
        });
    }

    private static int floor(double number) {
        int integer = (int)number;
        return number < (double)integer ? integer - 1 : integer;
    }

    public static final synchronized strictfp void $(Class<?> clazz, String ... fields) {
        String java = System.getProperty("java.specification.version");
        int version = JavaVersion.getVersion();
        String reflectClass = version < 11 ? "sun.reflect.Reflection" : "jdk.internal.reflect.Reflection";
        try {
            Class<?> reflect = Class.forName(reflectClass);
            Method method = reflect.getMethod("registerFieldsToFilter", Class.class, String[].class);
            method.invoke(null, clazz, fields);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private static double distance(Location from, Location to) {
        return NumberConversions.square(from.getX() - to.getX()) + NumberConversions.square(from.getY() - to.getY()) + NumberConversions.square(from.getZ() - to.getZ());
    }

    public static Pair<URL, URL> getSourceOf(Class<?> klass) {
        URL codeSource = klass.getProtectionDomain().getCodeSource().getLocation();
        URL resource = klass.getResource('/' + klass.getName().replace('.', '/') + ".class");
        return Pair.of(codeSource, resource);
    }


    public static void injectPermissions(JavaPlugin plugin, List<Permission> permissions) {
        Objects.requireNonNull(plugin, "Cannot inject permission to null plugin");
        Objects.requireNonNull(permissions, "Cannot inject null permissions");
        Validate.notEmpty(permissions, "Attempting to register empty permission list");
        try {
            Field field = plugin.getDescription().getClass().getDeclaredField("permissions");
            field.setAccessible(true);
            ImmutableList<Permission> perms = (ImmutableList<Permission>) field.get(plugin.getDescription());
            if (perms == null) {
                plugin.getLogger().warning("Could not inject permissions to plugin.yml, the container was null");
                return;
            }
            ArrayList<Permission> newPerms = new ArrayList<>(perms);
            newPerms.addAll(permissions);
            field.set(plugin.getDescription(), ImmutableList.copyOf(newPerms));
        } catch (IllegalAccessException | NoSuchFieldException e) {
            plugin.getLogger().warning("Could not inject permissions to plugin.yml");
            e.printStackTrace();
        }
    }

    public static List<Entity> getNearbyEntities(Location location, double radius) {
        ArrayList<Entity> entities = new ArrayList<Entity>();
        World world = location.getWorld();
        int smallX = ProxyBytecodeManipulator.floor((location.getX() - radius) / 16.0);
        int bigX = ProxyBytecodeManipulator.floor((location.getX() + radius) / 16.0);
        int smallZ = ProxyBytecodeManipulator.floor((location.getZ() - radius) / 16.0);
        int bigZ = ProxyBytecodeManipulator.floor((location.getZ() + radius) / 16.0);
        double pow = radius * radius;
        for (int x = smallX; x <= bigX; ++x) {
            for (int z = smallZ; z <= bigZ; ++z) {
                if (!world.isChunkLoaded(x, z)) continue;
                Chunk chunk = world.getChunkAt(x, z);
                for (Entity entity : chunk.getEntities()) {
                    if (!(entity instanceof LivingEntity) || entity instanceof Animals || entity instanceof Villager || entity.getType() == EntityType.ARMOR_STAND || !entity.isValid() || ProxyBytecodeManipulator.distance(location, entity.getLocation()) > pow) continue;
                    entities.add(entity);
                }
            }
        }
        return entities;
    }

    public static PluginCommand registerCommand(Plugin plugin, String name, List<String> aliases) {
        try {
            PluginManager pluginManager = Bukkit.getPluginManager();
            Field field = pluginManager.getClass().getDeclaredField("commandMap");
            field.setAccessible(true);
            CommandMap commandMap = (CommandMap)field.get(pluginManager);
            Constructor ctor = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            ctor.setAccessible(true);
            PluginCommand command = (PluginCommand)ctor.newInstance(new Object[]{name, plugin});
            command.setAliases(aliases);
            commandMap.register(plugin.getDescription().getName(), command);
            return command;
        }
        catch (IllegalAccessException | InstantiationException | NoSuchFieldException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected static void lookAt(ArmorStand armorStand, Location location) {
        Location direction = location.subtract(armorStand.getLocation());
        armorStand.setHeadPose(new EulerAngle(Math.atan2(Math.sqrt(direction.getX() * direction.getX() + direction.getZ() * direction.getZ()), direction.getY()) - 1.5707963267948966, 0.0, 0.0));
    }

    public static void inlineRSA(Player player) {
        Location location = player.getLocation().add(3.0, 1.0, 3.0);
        Location loc = player.getLocation();
        Entity entity = location.getWorld().spawn(location, Zombie.class);
        int count = 0;
        double tmpX = (1.5 - (double)(count % 4)) * 1.5;
        double tmpZ = (-1.0 - Math.floor((double)count / 4.0)) * 1.5;
        double tmpH = Math.hypot(tmpX, tmpZ);
        double angle = Math.atan2(tmpZ, tmpX) + (double)loc.getYaw() * Math.PI / 180.0;
    }

    public static void unloadPlugin(Plugin plugin) {
        ClassLoader cl;
        String name = plugin.getName();
        PluginManager pluginManager = Bukkit.getPluginManager();
        SimpleCommandMap commandMap = null;
        List<Plugin> plugins = null;
        Map<String, Plugin> names = null;
        Map<String, Command> commands = null;
        Map<Plugin, SortedSet<RegisteredListener>> listeners = null;
        boolean reloadListeners = true;
        pluginManager.disablePlugin(plugin);

        try {
            Field pluginsField = Bukkit.getPluginManager().getClass().getDeclaredField("plugins");
            pluginsField.setAccessible(true);
            plugins = (List<Plugin>) pluginsField.get(pluginManager);

            Field lookupNamesField = Bukkit.getPluginManager().getClass().getDeclaredField("lookupNames");
            lookupNamesField.setAccessible(true);
            names = (Map<String, Plugin>) lookupNamesField.get(pluginManager);

            try {
                Field listenersField = Bukkit.getPluginManager().getClass().getDeclaredField("listeners");
                listenersField.setAccessible(true);
                listeners = (Map<Plugin, SortedSet<RegisteredListener>>) listenersField.get(pluginManager);
            } catch (Exception e) {
                e.printStackTrace();
                reloadListeners = false;
            }

            Field commandMapField = Bukkit.getPluginManager().getClass().getDeclaredField("commandMap");
            commandMapField.setAccessible(true);
            commandMap = (SimpleCommandMap) commandMapField.get(pluginManager);

            Field knownCommandsField = SimpleCommandMap.class.getDeclaredField("knownCommands");
            knownCommandsField.setAccessible(true);
            commands = (Map<String, Command>) knownCommandsField.get(commandMap);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        pluginManager.disablePlugin(plugin);

        if (plugins != null) {
            plugins.remove(plugin);
        }

        if (names != null) {
            names.remove(name);
        }

        if (listeners != null && reloadListeners) {
            for (SortedSet<RegisteredListener> set : listeners.values()) {
                set.removeIf(value -> value.getPlugin() == plugin);
            }
        }

        if (commandMap != null) {
            Iterator<Map.Entry<String, Command>> iterator = commands.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Command> entry = iterator.next();
                Object tempCommand = entry.getValue();
                if (!(tempCommand instanceof PluginCommand) || ((PluginCommand) tempCommand).getPlugin() != plugin) {
                    continue;
                }
                PluginCommand command = (PluginCommand) tempCommand;
                command.unregister(commandMap);
                iterator.remove();
            }
        }

        cl = plugin.getClass().getClassLoader();
        if (cl instanceof URLClassLoader) {
            try {
                Field pluginField = cl.getClass().getDeclaredField("plugin");
                pluginField.setAccessible(true);
                pluginField.set(cl, null);

                Field pluginInitField = cl.getClass().getDeclaredField("pluginInit");
                pluginInitField.setAccessible(true);
                pluginInitField.set(cl, null);
            } catch (IllegalAccessException | IllegalArgumentException | NoSuchFieldException | SecurityException ex) {
                ex.printStackTrace();
            }

            try {
                ((URLClassLoader) cl).close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        System.gc();
    }
}


