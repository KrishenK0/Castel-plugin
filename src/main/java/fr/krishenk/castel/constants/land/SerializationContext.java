package fr.krishenk.castel.constants.land;

import fr.krishenk.castel.data.dataproviders.DataSetter;

public class SerializationContext<T extends DataSetter> {
    private final T dataProvider;

    public SerializationContext(T provider) {
        this.dataProvider = provider;
    }

    public T getDataProvider() {
        return this.dataProvider;
    }
}
