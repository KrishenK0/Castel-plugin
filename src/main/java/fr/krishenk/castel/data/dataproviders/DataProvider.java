package fr.krishenk.castel.data.dataproviders;

import org.jetbrains.annotations.NotNull;

public interface DataProvider extends SectionableDataSetter, SectionableDataGetter, SectionCreatableDataSetter {
    @Override
    @NotNull DataProvider get(@NotNull String name);
}
