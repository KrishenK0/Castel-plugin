package fr.krishenk.castel.data.dataproviders;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

public interface DataSetter {
    void setString(@Nullable String value);

    void setInt(int value);

    void setLocation(@Nullable Location value);

    void setSimpleLocation(@Nullable SimpleLocation value);

    void setSimpleChunkLocation(@NotNull SimpleChunkLocation value);

    void setLong(long value);

    void setFloat(float value);

    void setDouble(double value);

    void setBoolean(boolean value);

    void setUUID(@Nullable UUID value);

    <V> void setCollection( Collection<? extends V> collection, BiConsumer<SectionCreatableDataSetter, V> elementHandler);

    <K, V> void setMap(Map<K, ? extends V> map, MappingSetterHandler<K, V> handler);
}
