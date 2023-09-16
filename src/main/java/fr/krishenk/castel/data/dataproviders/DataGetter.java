package fr.krishenk.castel.data.dataproviders;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import org.apache.logging.log4j.util.TriConsumer;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public interface DataGetter {
    @Nullable
    default String asString() throws SQLException {
        return asString(() -> null);
    }

    default int asInt() throws SQLException {
        return asInt(() -> 0);
    }

    default long asLong() throws SQLException {
        return asLong(() -> 0L);
    }

    default float asFloat() throws SQLException {
        return asFloat(() -> 0.0F);
    }

    default double asDouble() throws SQLException {
        return asDouble(() -> 0.0);
    }

    default boolean asBoolean() throws SQLException {
        return asBoolean(() -> false);
    }

    @Nullable
    String asString(Supplier<String> defaultSupplier) throws SQLException;

    @Nullable
    UUID asUUID() throws SQLException;

    @Nullable
    SimpleLocation asSimpleLocation() throws SQLException;

    @NotNull
    SimpleChunkLocation asSimpleChunkLocation() throws SQLException;

    @Nullable
    Location asLocation() throws SQLException;

    int asInt(Supplier<Integer> defaultSupplier) throws SQLException;

    long asLong(Supplier<Long> defaultSupplier) throws SQLException;

    float asFloat(Supplier<Float> defaultSupplier) throws SQLException;

    double asDouble(Supplier<Double> defaultSupplier) throws SQLException;

    boolean asBoolean(Supplier<Boolean> defaultSupplier) throws SQLException;

    @NotNull
    <V, C extends Collection<V>> C asCollection(@NotNull C collection, @NotNull BiConsumer<C, SectionableDataGetter> elementHandler) throws SQLException;

    @NotNull
    <K, V, M extends Map<K, V>> M asMap(@NotNull M map, @NotNull TriConsumer<M, DataGetter, SectionableDataGetter> handler) throws SQLException;
}


