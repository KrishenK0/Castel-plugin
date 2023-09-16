package fr.krishenk.castel.utils.config;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class BukkitConfigSection
        implements ConfigurationSection {
    private final ConfigSection section;

    BukkitConfigSection(ConfigSection section) {
        this.section = section;
    }

    @NotNull
    public Set<String> getKeys(boolean deep) {
        return this.section.getKeys();
    }

    @NotNull
    public Map<String, Object> getValues(boolean deep) {
        return this.section.getValues(deep);
    }

    public boolean contains(@NotNull String path) {
        return this.isSet(path);
    }

    public boolean contains(@NotNull String path, boolean ignoreDefault) {
        return this.isSet(path);
    }

    public boolean isSet(@NotNull String path) {
        return this.section.isSet(path);
    }

    @Contract(value = "_, !null -> !null")
    @Nullable
    public Object get(@NotNull String path, @Nullable Object def) {
        return this.section.get(ConfigPath.buildRaw(path));
    }

    @NotNull
    public String getCurrentPath() {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public String getName() {
        return this.section.getName();
    }

    @Nullable
    public Configuration getRoot() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public ConfigurationSection getParent() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Object get(@NotNull String path) {
        return this.section.get(ConfigPath.buildRaw(path));
    }

    public void addDefault(@NotNull String path, @Nullable Object value) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public ConfigurationSection getDefaultSection() {
        throw new UnsupportedOperationException();
    }

    public void set(@NotNull String path, @Nullable Object value) {
        this.section.set(path.split("\\."), value);
    }

    @NotNull
    public ConfigurationSection createSection(@NotNull String path) {
        return this.section.createSection(path).toBukkitConfigurationSection();
    }

    @NotNull
    public ConfigurationSection createSection(@NotNull String path, @NotNull Map<?, ?> map) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public String getString(@NotNull String path) {
        return this.section.getString(ConfigPath.buildRaw(path));
    }

    @Contract(value = "_, !null -> !null")
    @Nullable
    public String getString(@NotNull String path, @Nullable String def) {
        return this.section.getString(ConfigPath.buildRaw(path));
    }

    public boolean isString(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    public int getInt(@NotNull String path) {
        return this.section.getInt(ConfigPath.buildRaw(path));
    }

    public int getInt(@NotNull String path, int def) {
        return this.section.getInt(ConfigPath.buildRaw(path));
    }

    public boolean isInt(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    public boolean getBoolean(@NotNull String path) {
        return this.section.getBoolean(ConfigPath.buildRaw(path));
    }

    public boolean getBoolean(@NotNull String path, boolean def) {
        return this.section.getBoolean(ConfigPath.buildRaw(path));
    }

    public boolean isBoolean(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    public double getDouble(@NotNull String path) {
        return this.section.getDouble(ConfigPath.buildRaw(path));
    }

    public double getDouble(@NotNull String path, double def) {
        return this.section.getDouble(ConfigPath.buildRaw(path));
    }

    public boolean isDouble(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    public long getLong(@NotNull String path) {
        return this.section.getLong(ConfigPath.buildRaw(path));
    }

    public long getLong(@NotNull String path, long def) {
        return this.section.getLong(ConfigPath.buildRaw(path));
    }

    public boolean isLong(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public List<?> getList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public List<?> getList(@NotNull String path, @Nullable List<?> def) {
        throw new UnsupportedOperationException();
    }

    public boolean isList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<String> getStringList(@NotNull String path) {
        return this.section.getStringList(ConfigPath.buildRaw(path));
    }

    @NotNull
    public List<Integer> getIntegerList(@NotNull String path) {
        return this.section.getIntegerList(ConfigPath.buildRaw(path));
    }

    @NotNull
    public List<Boolean> getBooleanList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<Double> getDoubleList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<Float> getFloatList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<Long> getLongList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<Byte> getByteList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<Character> getCharacterList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<Short> getShortList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<Map<?, ?>> getMapList(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public <T> T getObject(@NotNull String path, @NotNull Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public <T> T getObject(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public <T extends ConfigurationSerializable> T
    getSerializable(@NotNull String path, @NotNull Class<T> clazz) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public <T extends ConfigurationSerializable> T
    getSerializable(@NotNull String path, @NotNull Class<T> clazz, @Nullable T def) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Vector getVector(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Vector getVector(@NotNull String path, @Nullable Vector def) {
        throw new UnsupportedOperationException();
    }

    public boolean isVector(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public OfflinePlayer getOfflinePlayer(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public OfflinePlayer getOfflinePlayer(@NotNull String path, @Nullable OfflinePlayer def) {
        throw new UnsupportedOperationException();
    }

    public boolean isOfflinePlayer(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public ItemStack getItemStack(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public ItemStack getItemStack(@NotNull String path, @Nullable ItemStack def) {
        throw new UnsupportedOperationException();
    }

    public boolean isItemStack(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Color getColor(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Color getColor(@NotNull String path, @Nullable Color def) {
        throw new UnsupportedOperationException();
    }

    public boolean isColor(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Location getLocation(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public Location getLocation(@NotNull String path, @Nullable Location def) {
        throw new UnsupportedOperationException();
    }

    public boolean isLocation(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @Nullable
    public ConfigurationSection getConfigurationSection(@NotNull String path) {
        ConfigSection newSection = this.section.getSection(ConfigPath.buildRaw(path));
        return newSection == null ? null : newSection.toBukkitConfigurationSection();
    }

    public boolean isConfigurationSection(@NotNull String path) {
        return this.section.getSection(ConfigPath.buildRaw(path)) != null;
    }

    @Nullable
    public Object getDefault(@NotNull String path) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<String> getComments(@NotNull String s) {
        throw new UnsupportedOperationException();
    }

    @NotNull
    public List<String> getInlineComments(@NotNull String s) {
        throw new UnsupportedOperationException();
    }

    public void setComments(@NotNull String s, @Nullable List<String> list2) {
        throw new UnsupportedOperationException();
    }

    public void setInlineComments(@NotNull String s, @Nullable List<String> list2) {
        throw new UnsupportedOperationException();
    }

}


