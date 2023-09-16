package fr.krishenk.castel.constants.land;

import fr.krishenk.castel.data.dataproviders.DataGetter;

public class DeserializationContext<T extends DataGetter> {
    private final T dataProvider;

    public DeserializationContext(T provider) {
        this.dataProvider = provider;
    }

    public T getDataProvider() {
        return this.dataProvider;
    }
}
