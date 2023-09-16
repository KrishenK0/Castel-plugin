package fr.krishenk.castel.data.dataproviders;

import org.jetbrains.annotations.NotNull;

public interface SectionCreatableDataSetter extends DataSetter {
    @NotNull SectionableDataSetter createSection();
}
