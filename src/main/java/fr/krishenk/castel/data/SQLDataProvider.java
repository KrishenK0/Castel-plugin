package fr.krishenk.castel.data;

import fr.krishenk.castel.data.dataproviders.IdDataTypeHandler;

import java.sql.Connection;

public abstract class SQLDataProvider<K> {
    private final K id;
    private final IdDataTypeHandler<K> idType;
    private final Connection connection;
    private final String table;
    private final String name;
    private final boolean isInsideSingularEntity;
    private final boolean nameIsSection;

    public SQLDataProvider(K id, IdDataTypeHandler<K> idType, Connection connection, String table, String name, boolean isInsideSingularEntity, boolean nameIsSection) {
        this.id = id;
        this.idType = idType;
        this.connection = connection;
        this.table = table;
        this.name = name;
        this.isInsideSingularEntity = isInsideSingularEntity;
        this.nameIsSection = nameIsSection;
    }

    public K getId() {
        return id;
    }

    public IdDataTypeHandler<K> getIdType() {
        return idType;
    }

    public boolean isInsideSingularEntity() {
        return isInsideSingularEntity;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isNameIsSection() {
        return nameIsSection;
    }

    public String getName() {
        return name;
    }

    public String getNamed() {
        if (name != null) return name;
        throw new IllegalStateException("No name set for table: " + this.table);
    }

    public String getTable() {
        return table;
    }

    public String nameSetorEmpty() {
        String name = "";
        if (this.name != null) {
            name = this.name + '_';
        }
        return name;
    }
}
