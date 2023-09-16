package fr.krishenk.castel.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.data.dataproviders.DataGetter;
import fr.krishenk.castel.data.dataproviders.IdDataTypeHandler;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.statements.getters.SimpleResultSetQuery;
import fr.krishenk.castel.data.json.JsonElementDataProvider;
import fr.krishenk.castel.data.json.NamedJsonDataProvider;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class SQLDataGetterProvider<K> extends SQLDataProvider<K> implements SectionableDataGetter {
    private final SimpleResultSetQuery statement;


    public SQLDataGetterProvider(K id, IdDataTypeHandler<K> idType, Connection connection, String table, String name, boolean isInsideSingularEntity, boolean nameIsSection, SimpleResultSetQuery statement) {
        super(id, idType, connection, table, name, isInsideSingularEntity, nameIsSection);
        this.statement = statement;
    }

    @Override
    public @NotNull SectionableDataGetter asSection() {
        return new SQLDataGetterProvider(this.getId(), this.getIdType(), this.getConnection(), this.getTable(), this.getName(), false, true, this.statement);
    }

    @Override
    public @NotNull SectionableDataGetter get(@NotNull String name) {
        if (this.getName() != null && !this.isNameIsSection()) {
            throw new IllegalStateException("Attempting to get() without specifying the previous ones type: " + this.getTable() + " -> " + this.getName() + " -> " + name);
        }
        return new SQLDataGetterProvider(this.getId(), this.getIdType(), this.getConnection(), this.getTable(), this.nameSetorEmpty() + name, false, false, this.statement);
    }

    @Nullable
    @Override
    public String asString(Supplier<String> function) throws SQLException {
        String string = this.statement.getString(this.getNamed());
        if (string == null) string = function.get();
        return string;
    }

    @Nullable
    @Override
    public UUID asUUID() throws SQLException {
        return this.statement.getUUID(this.getNamed());
    }

    @Nullable
    @Override
    public SimpleLocation asSimpleLocation() throws SQLException {
        String string = this.asSection().getString("world");
        if (string == null) return null;
        return new SimpleLocation(string, this.asSection().getInt("x"), this.asSection().getInt("y"), this.asSection().getInt("z"));
    }

    @Override
    public SimpleChunkLocation asSimpleChunkLocation() throws SQLException {
        String string = this.asSection().getString("world");
        return new SimpleChunkLocation(string, this.asSection().getInt("x"), this.asSection().getInt("z"));
    }

    @Nullable
    @Override
    public Location asLocation() throws SQLException {
        String string = this.asSection().getString("world");
        if (string == null) return null;
        return new Location(Bukkit.getWorld(string), this.asSection().getInt("x"), this.asSection().getInt("y"), this.asSection().getInt("z"), this.asSection().getInt("yaw"), this.asSection().getInt("pitch"));
    }

    @Override
    public int asInt(Supplier<Integer> supplier) throws SQLException {
        return this.statement.getInt(this.getNamed());
    }

    @Override
    public long asLong(Supplier<Long> supplier) throws SQLException {
        return this.statement.getLong(this.getNamed());
    }

    @Override
    public float asFloat(Supplier<Float> supplier) throws SQLException {
        return this.statement.getFloat(this.getNamed());
    }

    @Override
    public double asDouble(Supplier<Double> supplier) throws SQLException {
        return this.statement.getDouble(this.getNamed());
    }

    @Override
    public boolean asBoolean(Supplier<Boolean> supplier) throws SQLException {
        return this.statement.getBoolean(this.getNamed());
    }

    @Override
    public <V, C extends Collection<V>> @NotNull C asCollection(@NotNull C collection, @NotNull BiConsumer<C, SectionableDataGetter> elementHandler) throws SQLException {
        String string = this.statement.getString(this.getNamed());
        if (string == null) return collection;
        JsonArray array = (JsonArray) CastelGson.fromString(string);
        for (JsonElement element : array) {
            elementHandler.accept(collection, NamedJsonDataProvider.createProvider(element));
        }
        return collection;
    }

    @Override
    public <K, V, M extends Map<K, V>> @NotNull M asMap(@NotNull M map, @NotNull TriConsumer<M, DataGetter, SectionableDataGetter> handler) throws SQLException {
        String string = this.statement.getString(this.getNamed());
        if (string == null) return map;
        JsonElement element = CastelGson.fromString(string);
        if (!element.isJsonObject()) return map;
        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();
            handler.accept(map, new JsonElementDataProvider(new JsonPrimitive(key)), NamedJsonDataProvider.createProvider(value));
        }
        return map;
    }
}
