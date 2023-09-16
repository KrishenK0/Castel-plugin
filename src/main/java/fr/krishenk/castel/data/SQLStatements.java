package fr.krishenk.castel.data;

import lombok.NonNull;

public class SQLStatements {

    public String upsertStatement(@NonNull String table, @NonNull String parameters, @NonNull String preparedValues) {
        return "MERGE INTO `"+ table + "` (" + parameters + ") VALUES(" + preparedValues +")";
    }

}
