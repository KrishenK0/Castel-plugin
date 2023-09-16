package fr.krishenk.castel.data.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.data.dataproviders.*;
import fr.krishenk.castel.utils.internal.FastUUID;
import fr.krishenk.castel.utils.string.StringUtils;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NamedJsonDataProvider implements DataProvider, SectionCreatableDataSetter {
    private String name;
    private JsonObject obj;

    public NamedJsonDataProvider(String name, JsonObject obj) {
        this.name = name;
        this.obj = obj;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public JsonObject getObj() {
        return obj;
    }

    public void setObj(JsonObject obj) {
        this.obj = obj;
    }

    private JsonObject getSection() {
        JsonObject object;
        if (this.name == null) object = this.obj;
        else {
            JsonElement element = this.obj.get(this.name);
            object = element != null ? element.getAsJsonObject() : null;
        }
        return object;
    }

    private String getVerifiedName() {
        if (this.name == null) {
            throw new IllegalStateException("No key name set");
        } else return this.name;
    }

    @Override
    public @NotNull DataProvider get(@NotNull String name) {
        return new NamedJsonDataProvider(name, this.obj);
    }

    @Override
    public @NotNull DataProvider asSection() {
        JsonObject jsonObject = this.getSection();
        if (jsonObject == null) jsonObject = new JsonObject();
        return new NamedJsonDataProvider(null, jsonObject);
    }

    @Nullable
    @Override
    public String asString(Supplier<String> function) throws SQLException {
        JsonElement object = this.obj.get(this.getVerifiedName());
        String string = "";
        if (object == null || (string = object.getAsString()) == null) {
            string = function.get();
        }
        return string;
    }

    @Nullable
    @Override
    public UUID asUUID() throws SQLException {
        String string = this.asString();
        UUID uuid;
        if (string != null) {
            uuid = FastUUID.fromString(string);
        } else uuid = null;
        return uuid;
    }

    @Nullable
    @Override
    public SimpleLocation asSimpleLocation() {
        SimpleLocation simpleLocation;
        JsonElement jsonElement = this.obj.get(this.getVerifiedName());
        if (jsonElement != null) {
            simpleLocation = SimpleLocation.fromString(jsonElement.getAsString());
        } else simpleLocation = null;

        return simpleLocation;
    }

    @Override
    public SimpleChunkLocation asSimpleChunkLocation() {
        JsonElement jsonElement = this.obj.get(this.getVerifiedName());
        return SimpleChunkLocation.fromString(jsonElement.getAsString());
    }

    @Nullable
    @Override
    public Location asLocation() throws SQLException {
        Location location;
        String string = this.asString();
        if (string != null) {
            location = deserializeLocation(string);
        } else location = null;
        return location;
    }

    @Override
    public int asInt(Supplier<Integer> supplier) {
        JsonElement jsonElement = this.obj.get(this.getVerifiedName());
        return jsonElement != null ? jsonElement.getAsInt() : supplier.get().intValue();
    }

    @Override
    public long asLong(Supplier<Long> supplier) {
        JsonElement jsonElement = this.obj.get(this.getVerifiedName());
        return jsonElement != null ? jsonElement.getAsLong() : supplier.get().longValue();
    }

    @Override
    public float asFloat(Supplier<Float> supplier) {
        JsonElement jsonElement = this.obj.get(this.getVerifiedName());
        return jsonElement != null ? jsonElement.getAsFloat() : supplier.get().floatValue();
    }

    @Override
    public double asDouble(Supplier<Double> supplier) {
        JsonElement jsonElement = this.obj.get(this.getVerifiedName());
        return jsonElement != null ? jsonElement.getAsDouble() : supplier.get().doubleValue();
    }

    @Override
    public boolean asBoolean(Supplier<Boolean> supplier) {
        JsonElement jsonElement = this.obj.get(this.getVerifiedName());
        return jsonElement != null ? jsonElement.getAsBoolean() : supplier.get().booleanValue();
    }

    @Override
    public <V, C extends Collection<V>> @NotNull C asCollection(@NotNull C collection, @NotNull BiConsumer<C, SectionableDataGetter> elementHandler) throws SQLException {
        JsonElement jsonElement = this.obj.get(this.getVerifiedName());
        JsonArray array = jsonElement != null ? jsonElement.getAsJsonArray() : null;
        if (array == null) return collection;
        for (JsonElement element : array) {
            elementHandler.accept(collection, createProvider(element));
        }
        return collection;
    }

    @Override
    public <K, V, M extends Map<K, V>> @NotNull M asMap(@NotNull M map, @NotNull TriConsumer<M, DataGetter, SectionableDataGetter> handler) {
        JsonObject jsonObject = this.getSection();
        JsonObject object = jsonObject != null ? jsonObject.getAsJsonObject() : null;
        if (object == null) return map;
        for (Map.Entry entry : object.entrySet()) {
            String key = (String) entry.getKey();
            JsonElement value = (JsonElement) entry.getValue();
            JsonElementDataProvider jsonElementDataProvider = new JsonElementDataProvider(new JsonPrimitive(key));
            handler.accept(map, jsonElementDataProvider, createProvider(value));
        }
        return map;
    }

    @Override
    public @NotNull SectionableDataSetter createSection() {
        JsonObject object = new JsonObject();
        this.obj.add(this.name, object);
        return new NamedJsonDataProvider(null, this.obj.get(this.name).getAsJsonObject());
    }

    @Override
    public void setString(@Nullable String value) {
        if (value != null) this.obj.addProperty(this.getVerifiedName(), value);
    }

    @Override
    public void setInt(int value) {
        this.obj.addProperty(this.getVerifiedName(), value);
    }

    @Override
    public void setSimpleLocation(@Nullable SimpleLocation value) {
        this.setString(String.valueOf(value));
    }

    @Override
    public void setSimpleChunkLocation(@NotNull SimpleChunkLocation value) {
        this.setString(value.toString());
    }

    @Override
    public void setUUID(@Nullable UUID value) {
        if (value != null) this.obj.addProperty(this.getVerifiedName(), FastUUID.toString(value));
    }

    @Override
    public void setLong(long value) {
        this.obj.addProperty(this.getVerifiedName(), value);
    }

    @Override
    public void setFloat(float value) {
        this.obj.addProperty(this.getVerifiedName(), value);
    }

    @Override
    public void setDouble(double value) {
        this.obj.addProperty(this.getVerifiedName(), value);
    }

    @Override
    public void setBoolean(boolean value) {
        this.obj.addProperty(this.getVerifiedName(), Boolean.valueOf(value));
    }

    @Override
    public void setLocation(@Nullable Location value) {
        if (value == null) return;
        this.setString(serializeLocation(value));
    }

    @Override
    public <V> void setCollection(Collection<? extends V> collection, BiConsumer<SectionCreatableDataSetter, V> elementHandler) {
        JsonArray array = new JsonArray();
        for (V element : collection) {
            elementHandler.accept(createProvider(array), element);
        }
        this.obj.add(this.getVerifiedName(), array);
    }

    @Override
    public <K, V> void setMap(@NotNull Map<K, ? extends V> map, @NotNull MappingSetterHandler<K, V> handler) {
        Objects.requireNonNull(map, "map");
        Objects.requireNonNull(handler, "handler");
        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<K, ? extends V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            handler.map(key, new StringMappedIdSetter(id -> {
                Objects.requireNonNull(id, "id");
                return new NamedJsonDataProvider(id, jsonObject);
            }), value);
        }
        this.obj.add(this.getVerifiedName(), jsonObject);
    }

    @Override
    public @NotNull SectionableDataSetter createSection(@NotNull String name) {
        if (this.name != null) throw new IllegalStateException("Previous name not handled: " + this.name + " -> " + name);
        this.obj.add(name, new JsonObject());
        return new NamedJsonDataProvider(null, obj);
    }

    @NotNull
    public static final String serializeLocation(@NotNull Location location) {
        StringBuilder stringBuilder = new StringBuilder();
        World world = location.getWorld();
        return stringBuilder.append(world.getName()).append(", ").append(location.getX()).append(", ").append(location.getY()).append(", ").append(location.getZ()).append(", ").append(location.getYaw()).append(", ").append(location.getPitch()).toString();
    }

    @NotNull
    public static final Location deserializeLocation(@NotNull String location) {
        String[] split = StringUtils.splitLocation(location, 6);
        World world = Bukkit.getWorld(split[0]);
        String string = split[1];
        double d = Double.parseDouble(string);
        String string2 = split[2];
        double d2 = Double.parseDouble(string2);
        String string3 = split[3];
        double d3 = Double.parseDouble(string3);
        String string4 = split[4];
        float f = Float.parseFloat(string4);
        String string5 = split[5];
        return new Location(world, d, d2, d3, f, Float.parseFloat(string5));
    }
    
    @NotNull
    public static final DataProvider createProvider(@NotNull JsonElement value) {
        return value instanceof JsonObject ? new NamedJsonDataProvider(null, (JsonObject)value) : new JsonElementDataProvider(value);
    }
}
