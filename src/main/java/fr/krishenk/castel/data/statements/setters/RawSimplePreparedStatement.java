package fr.krishenk.castel.data.statements.setters;

import fr.krishenk.castel.data.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

public class RawSimplePreparedStatement implements SimplePreparedStatement {
    private final PreparedStatement preparedStatement;
    private int index;
    public RawSimplePreparedStatement(int fromIndex, PreparedStatement preparedStatement) {
        this.index = fromIndex;
        this.preparedStatement = preparedStatement;
    }

    public int getIndex() {
        int n = this.index;
        this.index = n + 1;
        return n;
    }

    public RawSimplePreparedStatement(PreparedStatement preparedStatement) {
        this(1, preparedStatement);
    }

    @Override
    public void setString(@NotNull String name, @Nullable String value) throws SQLException {
        this.preparedStatement.setString(this.getIndex(), value);
    }

    @Override
    public void setInt(@NotNull String name, int value) throws SQLException {
        this.preparedStatement.setInt(this.getIndex(), value);
    }

    @Override
    public void setFloat(@NotNull String name, float value) throws SQLException {
        this.preparedStatement.setFloat(this.getIndex(), value);
    }

    @Override
    public void setLong(@NotNull String name, long value) throws SQLException {
        this.preparedStatement.setLong(this.getIndex(), value);
    }

    @Override
    public void setBoolean(@NotNull String name, boolean value) throws SQLException {
        this.preparedStatement.setBoolean(this.getIndex(), value);
    }

    @Override
    public void setDouble(@NotNull String name, double value) throws SQLException {
        this.preparedStatement.setDouble(this.getIndex(), value);
    }

    @Override
    public void setUUID(@NotNull String name, @Nullable UUID value) throws SQLException {
        this.preparedStatement.setBytes(this.getIndex(), Database.asBytes(value));
    }
}
