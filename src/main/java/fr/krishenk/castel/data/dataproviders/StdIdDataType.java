package fr.krishenk.castel.data.dataproviders;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.data.statements.getters.SimpleResultSetQuery;
import fr.krishenk.castel.data.statements.setters.SimplePreparedStatement;
import fr.krishenk.castel.utils.internal.FastUUID;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

public class StdIdDataType {
    public static final StdIdDataType INSTANCE = new StdIdDataType();
    public static final UUIDKey UUID = new UUIDKey("id");
    public static final SimpleChunkLocationKey SIMPLE_CHUNK_LOCATION = new SimpleChunkLocationKey("id");

    private StdIdDataType() {}

    public static class UUIDKey extends IdDataTypeHandler<UUID> {
        public UUIDKey(String prefix) {
            super(prefix, UUID.class, new String[0]);
        }

        @Override
        public void setSQL(SimplePreparedStatement statement, UUID uuid) {
            Objects.requireNonNull(uuid);
            try {
                statement.setUUID(this.getPrefix(), uuid);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public UUID fromSQL(SimpleResultSetQuery result) {
            try {
                return result.getUUID(this.getPrefix());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String toString(java.util.UUID id) {
            return FastUUID.toString(id);
        }

        @Override
        public UUID fromString(String string) {
            return FastUUID.fromString(string);
        }
    }

    private static class SimpleChunkLocationKey extends IdDataTypeHandler<SimpleChunkLocation> {
        public SimpleChunkLocationKey(String prefix) {
            super(prefix, SimpleChunkLocation.class, new String[]{"world", "x", "z"});
        }

        @Override
        public void setSQL(@NotNull SimplePreparedStatement statement, SimpleChunkLocation id) {
            try {
                statement.setString(this.getPrefix() + "_world", id.getWorld());
                statement.setInt(this.getPrefix() + "_x", id.getX());
                statement.setInt(this.getPrefix() + "_z", id.getZ());
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public SimpleChunkLocation fromSQL(@NotNull SimpleResultSetQuery result) throws SQLException {
            return new SimpleChunkLocation(result.getString(this.getPrefix() + "_world"), result.getInt(this.getPrefix() + "_x"), result.getInt(this.getPrefix() + "_z"));
        }

        @Override
        public SimpleChunkLocation fromString(@NotNull String string) {
            return SimpleChunkLocation.fromString(string);
        }

        @Override
        public @NotNull String toString(SimpleChunkLocation id) {
            return id.toString();
        }
    }
}
