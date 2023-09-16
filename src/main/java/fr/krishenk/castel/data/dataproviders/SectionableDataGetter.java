package fr.krishenk.castel.data.dataproviders;



import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.SQLException;

public interface SectionableDataGetter extends SectionableDataProvider, DataGetter {
    @Override
    @NotNull SectionableDataGetter get(@NotNull String name);

    @NotNull SectionableDataGetter asSection();

    @Nullable
    default String getString(@NotNull String name) throws SQLException {
        return this.get(name).asString();
    }

    default int getInt(@NotNull String name) throws SQLException {
        return this.get(name).asInt();
    }

    default float getFloat(@NotNull String name) throws SQLException {
        return this.get(name).asFloat();
    }

    default long getLong(@NotNull String name) throws SQLException {
        return this.get(name).asLong();
    }

    default double getDouble(@NotNull String name) throws SQLException {
        return this.get(name).asDouble();
    }

    default boolean getBoolean(@NotNull String name) throws SQLException {
        return this.get(name).asBoolean();
    }
}
