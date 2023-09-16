package fr.krishenk.castel.data.dataproviders;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface SectionableDataSetter extends DataSetter, SectionableDataProvider {
    @Override
    @NotNull SectionableDataSetter get(@NotNull String name);

    default void setString(@NotNull String name, @Nullable String value) {
        this.get(name).setString(value);
    }

    default void setInt(@NotNull String name, int value) {
        this.get(name).setInt(value);
    }

    default void setLong(@NotNull String name, long value) {
        this.get(name).setLong(value);
    }

    default void setFloat(@NotNull String name, float value) {
        this.get(name).setFloat(value);
    }

    default void setDouble(@NotNull String name, double value) {
        this.get(name).setDouble(value);
    }

    default void setBoolean(@NotNull String name, boolean value) {
        this.get(name).setBoolean(value);
    }

    default void setUUID(@NotNull String name, @Nullable UUID value) {
        this.get(name).setUUID(value);
    }

    @NotNull SectionableDataSetter createSection(@NotNull String name);
}
