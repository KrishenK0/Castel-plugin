package fr.krishenk.castel.data.handlers;

import fr.krishenk.castel.data.dataproviders.IdDataTypeHandler;
import fr.krishenk.castel.data.dataproviders.SQLDataHandlerProperties;
import fr.krishenk.castel.data.dataproviders.SectionableDataGetter;
import fr.krishenk.castel.data.dataproviders.SectionableDataSetter;

import java.sql.SQLException;

public abstract class DataHandler<ID, T> {
    private final IdDataTypeHandler<ID> idHandler;
    private final SQLDataHandlerProperties sqlProperties;

    public DataHandler(IdDataTypeHandler idHandler, SQLDataHandlerProperties sqlProperties) {
        this.idHandler = idHandler;
        this.sqlProperties = sqlProperties;
    }

    public final IdDataTypeHandler<ID> getIdHandler() {
        return this.idHandler;
    }

    public SQLDataHandlerProperties getSqlProperties() {
        return this.sqlProperties;
    }

    public abstract void save(SectionableDataSetter provider, T data);

    public abstract T load(SectionableDataGetter provider, ID id) throws SQLException;
}
