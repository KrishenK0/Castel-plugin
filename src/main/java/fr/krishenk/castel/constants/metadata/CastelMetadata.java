package fr.krishenk.castel.constants.metadata;

import fr.krishenk.castel.constants.land.SerializationContext;
import fr.krishenk.castel.data.dataproviders.SectionCreatableDataSetter;
import org.jetbrains.annotations.NotNull;

public interface CastelMetadata {
    @NotNull Object getValue();

    void setValue(@NotNull Object obj);

    void serialize(@NotNull CastelObject<?> obj, @NotNull SerializationContext<SectionCreatableDataSetter> context);

    default boolean shouldSave(@NotNull CastelObject<?> container) {
        return true;
    }
}
