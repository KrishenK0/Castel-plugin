package fr.krishenk.castel.data.statements.setters;


import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.UUID;

public interface SimplePreparedStatement {
    void setString(@NotNull String name, @Nullable String value) throws SQLException;

    void setInt(@NotNull String name, int value) throws SQLException;

    void setFloat(@NotNull String name, float value) throws SQLException;

    void setLong(@NotNull String name, long value) throws SQLException;

    void setBoolean(@NotNull String name, boolean value) throws SQLException;

    void setDouble(@NotNull String name, double value) throws SQLException;

    void setUUID(@NotNull String name, @Nullable UUID value) throws SQLException;
}
