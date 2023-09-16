package fr.krishenk.castel.data.json;

import com.google.gson.*;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.data.dataproviders.*;
import fr.krishenk.castel.utils.internal.FastUUID;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class JsonElementDataProvider implements DataProvider, SectionableDataSetter {
    private final JsonElement element;

    public JsonElementDataProvider(JsonElement element) {
        this.element = element;
    }

    @Override
    public @NotNull SectionableDataSetter createSection() {
        throw new UnsupportedOperationException();
    }

    @Override
    public @NotNull SectionableDataSetter createSection(@NotNull String name) {
        JsonObject object = new JsonObject();
        if (!(this.element instanceof JsonArray)) throw new UnsupportedOperationException();
        ((JsonArray)this.element).add(object);
        return new NamedJsonDataProvider(null, object);
    }

    @Override
    public @NotNull DataProvider get(@NotNull String name) {
        return new NamedJsonDataProvider(name, this.getObj());
    }

    @Override
    public @NotNull SectionableDataGetter asSection() {
        return this;
    }

    @Nullable
    @Override
    public String asString(Supplier<String> function) throws SQLException {
        if (this.element instanceof JsonNull) return null;
        String string = this.element.getAsString();
        if (string == null) string = function.get();
        return string;
    }

    @Nullable
    @Override
    public UUID asUUID() throws SQLException {
        UUID uUID = FastUUID.fromString(Objects.requireNonNull(this.asString(() -> {
            throw new IllegalStateException();
        })));
        return uUID;
    }

    @Nullable
    @Override
    public SimpleLocation asSimpleLocation() throws SQLException {
        return SimpleLocation.fromString(Objects.requireNonNull(this.asString(() -> {
            throw new IllegalStateException();
        })));
    }

    @Override
    @NotNull
    public SimpleChunkLocation asSimpleChunkLocation() throws SQLException {
        String string = this.asString(() -> {
            throw new IllegalStateException();
        });
        SimpleChunkLocation simpleChunkLocation = SimpleChunkLocation.fromString(string);
        Objects.requireNonNull(simpleChunkLocation);
        return simpleChunkLocation;
    }

    @Nullable
    @Override
    public Location asLocation() throws SQLException {
        Location location;
        String string = this.asString(() -> {
            throw new IllegalStateException();
        });
        if (string != null) {
            location = NamedJsonDataProvider.deserializeLocation(string);
        } else location = null;

        return location;
    }

    @Override
    public int asInt(Supplier<Integer> supplier) {
        return this.element.getAsInt();
    }

    @Override
    public long asLong(Supplier<Long> supplier) {
        return this.element.getAsLong();
    }

    @Override
    public float asFloat(Supplier<Float> supplier) {
        return this.element.getAsFloat();
    }

    @Override
    public double asDouble(Supplier<Double> supplier) {
        return this.element.getAsDouble();
    }

    @Override
    public boolean asBoolean(Supplier<Boolean> supplier) {
        return this.element.getAsBoolean();
    }

    @Override
    public <V, C extends Collection<V>> @NotNull C asCollection(@NotNull C collection, @NotNull BiConsumer<C, SectionableDataGetter> elementHandler) throws SQLException {
        JsonArray array = (JsonArray) this.element;
        for (JsonElement element : array) {
            elementHandler.accept(collection, new JsonElementDataProvider(element));
        }
        return collection;
    }

    @Override
    public <K, V, M extends Map<K, V>> @NotNull M asMap(@NotNull M map, @NotNull TriConsumer<M, DataGetter, SectionableDataGetter> handler) {
        JsonObject object = (JsonObject) this.element;
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            JsonElementDataProvider elementDataProvider = new JsonElementDataProvider(new JsonPrimitive(key));
            handler.accept(map, elementDataProvider, new JsonElementDataProvider(value));
        }
        return map;
    }

    @Override
    public void setString(@Nullable String value) {
        if (!(this.element instanceof JsonArray)) throw new UnsupportedOperationException();
        ((JsonArray)this.element).add(value);
    }

    @Override
    public void setInt(int value) {
        if (!(this.element instanceof JsonArray)) throw new UnsupportedOperationException();
        ((JsonArray)this.element).add(value);
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
    public void setLong(long value) {
        if (!(this.element instanceof JsonArray)) throw new UnsupportedOperationException();
        ((JsonArray)this.element).add(value);
    }

    @Override
    public void setFloat(float value) {
        if (!(this.element instanceof JsonArray)) throw new UnsupportedOperationException();
        ((JsonArray)this.element).add(Float.valueOf(value));
    }

    @Override
    public void setDouble(double value) {
        if (!(this.element instanceof JsonArray)) throw new UnsupportedOperationException();
        ((JsonArray)this.element).add(value);
    }

    @Override
    public void setBoolean(boolean value) {
        if (!(this.element instanceof JsonArray)) throw new UnsupportedOperationException();
        ((JsonArray)this.element).add(Boolean.valueOf(value));
    }

    @Override
    public <V> void setCollection(Collection<? extends V> collection, BiConsumer<SectionCreatableDataSetter, V> elementHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <K, V> void setMap(Map<K, ? extends V> map, MappingSetterHandler<K, V> handler) {
        throw new UnsupportedOperationException();
    }

    private JsonObject getObj() {
        return ((JsonObject) this.element);
    }

    @Override
    public void setLocation(@Nullable Location value) {
        if (value == null) return;
        this.setString(NamedJsonDataProvider.serializeLocation(value));
    }

    @Override
    public void setUUID(@Nullable UUID value) {
        this.setString(FastUUID.toString(value));
    }
}
