package fr.krishenk.castel.config.implementation;

import fr.krishenk.castel.config.ConfigAccessor;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.Configuration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ConfigAccessorConfigurationSection implements ConfigurationSection {
    private final ConfigAccessor config;

    public ConfigAccessorConfigurationSection(ConfigAccessor config) {
        this.config = Objects.requireNonNull(config, "Config accessor cannot be null");
    }

    public @NonNull Set<String> getKeys(boolean b) {
        return this.config.getKeys();
    }

    public @NonNull Map<String, Object> getValues(boolean b) {
        return this.config.getEntries();
    }

    public boolean contains(@NonNull String s) {
        return this.config.isSet(s);
    }

    public boolean contains(@NonNull String s, boolean b) {
        return this.config.isSet(s);
    }

    public boolean isSet(@NonNull String s) {
        return this.config.isSet(s);
    }

    public @Nullable String getCurrentPath() {
        return this.config.getCurrentPath();
    }

    public @NonNull String getName() {
        throw new UnsupportedOperationException();
    }

    public @Nullable Configuration getRoot() {
        throw new UnsupportedOperationException();
    }

    public @Nullable ConfigurationSection getParent() {
        throw new UnsupportedOperationException();
    }

    public @Nullable Object get(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable Object get(@NonNull String s, @Nullable Object o) {
        throw new UnsupportedOperationException();
    }

    public void set(@NonNull String s, @Nullable Object o) {
        throw new UnsupportedOperationException();
    }

    public @NonNull ConfigurationSection createSection(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @NonNull ConfigurationSection createSection(@NonNull String s, @NonNull Map<?, ?> map) {
        throw new UnsupportedOperationException();
    }

    public @Nullable String getString(@NonNull String s) {
        return this.config.getString(s);
    }

    public @Nullable String getString(@NonNull String s, @Nullable String s1) {
        return this.config.getString(s);
    }

    public boolean isString(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public int getInt(@NonNull String s) {
        return this.config.getInt(s);
    }

    public int getInt(@NonNull String s, int i) {
        return this.config.getInt(s);
    }

    public boolean isInt(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public boolean getBoolean(@NonNull String s) {
        return this.config.getBoolean(s);
    }

    public boolean getBoolean(@NonNull String s, boolean b) {
        return this.config.getBoolean(s);
    }

    public boolean isBoolean(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public double getDouble(@NonNull String s) {
        return this.config.getDouble(s);
    }

    public double getDouble(@NonNull String s, double v) {
        return this.config.getDouble(s);
    }

    public boolean isDouble(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public long getLong(@NonNull String s) {
        return this.config.getLong(s);
    }

    public long getLong(@NonNull String s, long l) {
        return this.config.getLong(s);
    }

    public boolean isLong(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable List<?> getList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable List<?> getList(@NonNull String s, @Nullable List<?> list2) {
        throw new UnsupportedOperationException();
    }

    public boolean isList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @NonNull List<String> getStringList(@NonNull String s) {
        return this.config.getStringList(s);
    }

    public @NonNull List<Integer> getIntegerList(@NonNull String s) {
        return this.config.getIntegerList(s);
    }

    public @NonNull List<Boolean> getBooleanList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @NonNull List<Double> getDoubleList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @NonNull List<Float> getFloatList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @NonNull List<Long> getLongList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @NonNull List<Byte> getByteList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @NonNull List<Character> getCharacterList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @NonNull List<Short> getShortList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @NonNull List<Map<?, ?>> getMapList(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public <T> @Nullable T getObject(@NonNull String s, @NonNull Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    public <T> @Nullable T getObject(@NonNull String s, @NonNull Class<T> aClass, @Nullable T t) {
        throw new UnsupportedOperationException();
    }

    public <T extends ConfigurationSerializable> @Nullable T getSerializable(@NonNull String s, @NonNull Class<T> aClass) {
        throw new UnsupportedOperationException();
    }

    public <T extends ConfigurationSerializable> @Nullable T getSerializable(@NonNull String s, @NonNull Class<T> aClass, @Nullable T t) {
        throw new UnsupportedOperationException();
    }

    public @Nullable Vector getVector(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable Vector getVector(@NonNull String s, @Nullable Vector vector) {
        throw new UnsupportedOperationException();
    }

    public boolean isVector(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable OfflinePlayer getOfflinePlayer(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable OfflinePlayer getOfflinePlayer(@NonNull String s, @Nullable OfflinePlayer offlinePlayer) {
        throw new UnsupportedOperationException();
    }

    public boolean isOfflinePlayer(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable ItemStack getItemStack(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable ItemStack getItemStack(@NonNull String s, @Nullable ItemStack itemStack) {
        throw new UnsupportedOperationException();
    }

    public boolean isItemStack(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable Color getColor(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable Color getColor(@NonNull String s, @Nullable Color color) {
        throw new UnsupportedOperationException();
    }

    public boolean isColor(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable Location getLocation(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable Location getLocation(@NonNull String s, @Nullable Location location) {
        throw new UnsupportedOperationException();
    }

    public boolean isLocation(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable ConfigurationSection getConfigurationSection(@NonNull String s) {
        ConfigAccessor section = this.config.gotoSection(s);
        return section == null ? null : new ConfigAccessorConfigurationSection(section);
    }

    public boolean isConfigurationSection(@NonNull String s) {
        throw new UnsupportedOperationException();
    }

    public @Nullable ConfigurationSection getDefaultSection() {
        throw new UnsupportedOperationException();
    }

    public void addDefault(@NonNull String s, @Nullable Object o) {
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


