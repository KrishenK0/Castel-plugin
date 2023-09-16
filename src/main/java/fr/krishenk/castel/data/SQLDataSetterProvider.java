package fr.krishenk.castel.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.data.dataproviders.*;
import fr.krishenk.castel.data.json.JsonElementDataProvider;
import fr.krishenk.castel.data.json.NamedJsonDataProvider;
import fr.krishenk.castel.data.statements.setters.PreparedNamedSetterStatement;
import fr.krishenk.castel.data.statements.setters.RawSimplePreparedStatement;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.BiConsumer;

public class SQLDataSetterProvider<K> extends SQLDataProvider<K> implements SectionableDataSetter {
    private final PreparedNamedSetterStatement statement;

    public SQLDataSetterProvider(K id, IdDataTypeHandler<K> idType, Connection connection, String table, String name, boolean isInsideSingularEntity, boolean nameIsSection, PreparedNamedSetterStatement statement) {
        super(id, idType, connection, table, name, isInsideSingularEntity, nameIsSection);
        this.statement = statement;
    }

    private void checkNullName() {
        if (this.getName() != null && !this.isNameIsSection()) {
            throw new IllegalStateException("Specified name is not given a type: " + this.getTable() + "- -> " + this.getName());
        }
    }

    @Override
    public @NotNull SectionableDataSetter get(@NotNull String name) {
        Objects.requireNonNull(name);
        this.checkNullName();
        return new SQLDataSetterProvider<>(this.getId(), this.getIdType(), this.getConnection(), this.getTable(), this.nameSetorEmpty() + name, this.isInsideSingularEntity(), false, this.statement);
    }

    @Override
    public void setString(@Nullable String value) {
        this.statement.setString(this.getNamed(), value);
    }

    @Override
    public void setInt(int value) {
        this.statement.setInt(this.getName(), value);
    }

    @Override
    public void setLocation(@Nullable Location value) {
        if (value == null) return;
        this.statement.setString(this.getNamed() + "_world", value.getWorld().getName());
        this.statement.setDouble(this.getNamed() + "_x", value.getX());
        this.statement.setDouble(this.getNamed() + "_y", value.getY());
        this.statement.setDouble(this.getNamed() + "_z", value.getZ());
        this.statement.setFloat(this.getNamed() + "_yaw", value.getYaw());
        this.statement.setFloat(this.getNamed() + "_pitch", value.getPitch());
    }

    @Override
    public void setSimpleLocation(@Nullable SimpleLocation value) {
        if (value == null) return;
        this.statement.setString(this.getNamed() + "_world", value.getWorld());
        this.statement.setInt(this.getNamed() + "_x", value.getX());
        this.statement.setInt(this.getNamed() + "_y", value.getY());
        this.statement.setInt(this.getNamed() + "_z", value.getZ());
    }

    @Override
    public void setSimpleChunkLocation(@NotNull SimpleChunkLocation value) {
        if (value == null) return;
        this.statement.setString(this.getNamed() + "_world", value.getWorld());
        this.statement.setInt(this.getNamed() + "_x", value.getX());
        this.statement.setInt(this.getNamed() + "_z", value.getZ());
    }

    @Override
    public void setLong(long value) {
        this.statement.setLong(this.getNamed(), value);
    }

    @Override
    public void setFloat(float value) {
        this.statement.setFloat(this.getNamed(), value);
    }

    @Override
    public void setDouble(double value) {
        this.statement.setDouble(this.getNamed(), value);
    }

    @Override
    public void setBoolean(boolean value) {
        this.statement.setBoolean(this.getNamed(), value);
    }

    private void deleteAssociatedData(String table) throws SQLException {
        String query = "DELETE FROM `" + table + "` WHERE " + this.getIdType().getWhereClause();
        try (PreparedStatement preparedStatement = this.getConnection().prepareStatement(query)) {
            IdDataTypeHandler idDataTypeHandler = this.getIdType();
            idDataTypeHandler.setSQL(new RawSimplePreparedStatement(preparedStatement), this.getId());
            preparedStatement.execute();
        }
    }

    @Override
    public <V> void setCollection(Collection<? extends V> collection, BiConsumer<SectionCreatableDataSetter, V> elementHandler) {
        Objects.requireNonNull(collection);
        Objects.requireNonNull(elementHandler);
        if (collection.isEmpty()) {
            this.statement.addParameterIfNotExist(this.getNamed());
            this.statement.setJson(this.getNamed(), null);
            return;
        }
        JsonArray array = new JsonArray();
        JsonElementDataProvider arrayProvider = new JsonElementDataProvider(array);
        for (V element : collection) {
            elementHandler.accept(arrayProvider, element);
        }
        this.statement.setJson(this.getNamed(), array);
    }

    @Override
    public <K, V> void setMap(@NotNull Map<K, ? extends V> map, @NotNull MappingSetterHandler<K, V> handler) {
        Objects.requireNonNull(map);
        Objects.requireNonNull(handler);

        if (map.isEmpty()) {
            this.statement.addParameterIfNotExist(this.getNamed());
            this.statement.setJson(this.getNamed(), null);
            return;
        }

        JsonObject obj = new JsonObject();
        for (Map.Entry<K, ? extends V> entry : map.entrySet()) {
            K key = entry.getKey();
            V value = entry.getValue();
            handler.map(key, new StringMappedIdSetter(id -> new NamedJsonDataProvider(id, obj)), value);
        }
        this.statement.setJson(this.getNamed(), obj);
    }


    @Override
    public @NotNull SectionableDataSetter createSection(@NotNull String name) {
        this.checkNullName();
        return new SQLDataSetterProvider(this.getId(), this.getIdType(), this.getConnection(), this.getTable(), this.nameSetorEmpty() + name, this.isInsideSingularEntity(), true, this.statement);
    }

    @Override
    public void setUUID(@Nullable UUID value) {
        this.statement.setUUID(this.getNamed(), value);
    }
}
