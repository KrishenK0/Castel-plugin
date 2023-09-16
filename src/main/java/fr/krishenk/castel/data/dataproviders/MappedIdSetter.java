package fr.krishenk.castel.data.dataproviders;

import org.jetbrains.annotations.NotNull;

public interface MappedIdSetter extends DataSetter {
    @NotNull SectionCreatableDataSetter getValueProvider();
}
