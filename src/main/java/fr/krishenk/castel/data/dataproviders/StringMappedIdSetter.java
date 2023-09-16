package fr.krishenk.castel.data.dataproviders;

import fr.krishenk.castel.constants.land.location.SimpleChunkLocation;
import fr.krishenk.castel.constants.land.location.SimpleLocation;
import fr.krishenk.castel.utils.internal.FastUUID;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class StringMappedIdSetter implements MappedIdSetter {
    private final Function<String, SectionCreatableDataSetter> dataSetterConstructor;
    private String id;

    public StringMappedIdSetter(Function<String, SectionCreatableDataSetter> dataSetterConstructor) {
        this.dataSetterConstructor = dataSetterConstructor;
    }

    private void unsupportedId() {
        throw new UnsupportedOperationException("This ID type is not supported");
    }

    @Override
    public @NotNull SectionCreatableDataSetter getValueProvider() {
        if (this.id == null) throw new IllegalStateException("Cannot get value provider before setting the ID");
        return this.dataSetterConstructor.apply(this.id);
    }

    @Override
    public void setString(@Nullable String value) {
        this.id = value;
    }

    @Override
    public void setInt(int value) {
        this.id = String.valueOf(value);
    }

    @Override
    public void setLocation(@Nullable Location value) {
        this.unsupportedId();
    }

    @Override
    public void setSimpleLocation(@Nullable SimpleLocation value) {
        this.id = String.valueOf(value);
    }

    @Override
    public void setSimpleChunkLocation(@NotNull SimpleChunkLocation value) {
        this.id = value.toString();
    }

    @Override
    public void setLong(long value) {
        this.id = String.valueOf(value);
    }

    @Override
    public void setFloat(float value) {
        this.unsupportedId();
    }

    @Override
    public void setDouble(double value) {
        this.unsupportedId();
    }

    @Override
    public void setBoolean(boolean value) {
        this.unsupportedId();
    }

    @Override
    public void setUUID(@Nullable UUID value) {
        this.id = FastUUID.toString(value);
    }

    @Override
    public <V> void setCollection(Collection<? extends V> collection, BiConsumer<SectionCreatableDataSetter, V> elementHandler) {
        this.unsupportedId();
    }

    @Override
    public <K, V> void setMap(Map<K, ? extends V> map, MappingSetterHandler<K, V> handler) {
        this.unsupportedId();
    }
}
