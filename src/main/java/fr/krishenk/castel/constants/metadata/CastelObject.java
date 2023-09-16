package fr.krishenk.castel.constants.metadata;

import fr.krishenk.castel.utils.internal.nonnull.NonNullMap;
import lombok.NonNull;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public abstract class CastelObject<K> {
    protected @NotNull Map<CastelMetadataHandler, CastelMetadata> metadata;
    private transient @Nullable String saveMeta;

    protected CastelObject() {
        this.metadata = new NonNullMap<>();
    }

    protected CastelObject(@NonNull Map<CastelMetadataHandler, CastelMetadata> map) {
        this.setMetadata(map);
    }

    public static String compressLocation(Location location) {
        if(location == null) return "";
        World world = location.getWorld();
        return (world == null ? "" : world.getName()) + location.getX() + location.getY() + location.getZ() + location.getYaw() + location.getPitch();
    }

    public static String compressUUID(UUID uuid) {
        return uuid == null ? "" : Long.toString(uuid.getLeastSignificantBits()) + uuid.getMostSignificantBits();
    }

    public static String compressBoolean(boolean bool) {
        return bool ? "1" : "";
    }

    public static String compressString(String string) {
        return string == null ? "" : string.length() + string;
    }

    public static String compressColor(Color color) {
        return color == null ? "" : Integer.toString(color.getRGB());
    }

    public static CharSequence compressInventory(Inventory inventory) {
        StringBuilder builder = new StringBuilder(inventory.getSize()*100);
        builder.append(inventory.getSize());
        for (ItemStack item : inventory.getContents()) {
            if (item == null) continue;
            builder.append(item.getType()).append(item.getAmount());
            if(!item.hasItemMeta()) continue;
            builder.append(item.getItemMeta());
        }
        return builder;
    }

    public void setMetadata(Map<CastelMetadataHandler, CastelMetadata> metadata) {
        this.metadata = NonNullMap.of(Objects.requireNonNull(metadata));
    }

    public static <T> String compressCollection(Collection<T> collection, Function<T, Object> function) {
        return compressCollection(collection, 0, function);
    }

    public static <T> String compressCollection(Collection<T> collection, int mod, Function<T, Object> function) {
        Objects.requireNonNull(collection, "Cannot compress null collection");
        Objects.requireNonNull(function, "Cannot function cannot be null");
        int size = collection.size();
        if (size == 0) return "";
        if (mod == 0) mod = size;
        StringBuilder builder = new StringBuilder(size*mod);
        builder.append(collection.size());
        for (T element : collection) {
            Object result = function.apply(element);
            builder.append(result);
        }
        return builder.toString();
    }

    public static <K, V> String compressMap(Map<K, V> map, int mod, Function<K, Object> keyCompressor, Function<V, Object> valueCompressor) {
        int size = map.size();
        if (size == 0) return "";
        if (mod == 0) mod = size;
        StringBuilder builder = new StringBuilder(size*mod);
        builder.append(map.size());
        for (Map.Entry<K, V> entry : map.entrySet()) {
            builder.append(keyCompressor.apply(entry.getKey()));
            Object translated = valueCompressor.apply(entry.getValue());
            builder.append(translated);
        }
        return builder.toString();
    }

    public final String compressMetadata() {
        return CastelObject.compressMap(this.metadata, 0, s-> s, Object::hashCode);
    }

    public abstract K getDataKey();

    public final void setSaveMeta() {
        this.saveMeta = this.getCompressedData();
    }

    public boolean shouldSave() {
        String compressData = this.getCompressedData();
        if (!compressData.equals(this.saveMeta)) {
            this.saveMeta = compressData;
            return true;
        }
        return false;
    }
    public abstract String getCompressedData();

    public Map<CastelMetadataHandler, CastelMetadata> getMetadata() { return this.metadata; }
}
