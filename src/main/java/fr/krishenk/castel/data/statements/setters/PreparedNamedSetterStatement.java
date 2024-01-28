package fr.krishenk.castel.data.statements.setters;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Unit;
import com.sun.javafx.geom.transform.GeneralTransform3D;
import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.data.CastelGson;
import fr.krishenk.castel.data.Database;
import fr.krishenk.castel.data.Pair;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;

public class PreparedNamedSetterStatement implements SimplePreparedStatement {
    private final List<Pair<String, Function<Integer, Unit>>> operations;
    public PreparedStatement statement;
    private boolean initialized;
    private boolean batch;
    private final Map<String, Integer> associateNamedData;
    private Set<String> setParameters;

    public PreparedNamedSetterStatement(Map<String, Integer> associatedNamedData) {
        this.operations = new ArrayList<>(30);
        this.associateNamedData = new LinkedHashMap<>(associatedNamedData);
        this.setParameters = new HashSet<>();
    }

    public PreparedStatement getStatement() {
        if (this.statement != null) return this.statement;
        return null;
    }

    public void setStatement(PreparedStatement statement) {
        this.statement = statement;
    }

    public void addParameterIfNotExist(String parameterName) {
        Objects.requireNonNull(parameterName);
        if (!this.associateNamedData.containsKey(parameterName)) {
            this.addParameter(parameterName);
        }
    }

    private void addParameter(String parameterName) {
        this.associateNamedData.put(parameterName, this.associateNamedData.size() + 1);
    }

    private void addOperation(String parameter, Function<Integer, Unit> operation) {
        this.setParameters.add(parameter);
        if (this.initialized) operation.apply(this.getIndex(parameter));
        else this.operations.add(Pair.of(parameter, operation));
    }

    private void checkInitialized() {
        if (!this.initialized) throw new IllegalStateException("Statement no built yet");
    }

    private int getIndex(String name) {
        Integer n = this.associateNamedData.get(name);
        if (n == null) throw new IllegalStateException("Unknown parameter index for " + name + " (" + this.associateNamedData + ')');
        return n;
    }

    public void buildStatement(String table, Connection connection) throws SQLException {
        Objects.requireNonNull(table, "table");
        Objects.requireNonNull(connection, "connection");
        if (initialized) return;

        StringJoiner parameterNamesJoiner = new StringJoiner(", ");
        StringJoiner preparedParameterJoiner = new StringJoiner(", ");
        for (String key : associateNamedData.keySet()) {
            parameterNamesJoiner.add('`' + key + '`');
            preparedParameterJoiner.add("?");
        }
        for (Pair<String, Function<Integer, Unit>> operation : operations) {
            String paramName = (String) ((Pair<?, ?>) operation).getKey();
            if (!associateNamedData.containsKey(paramName)) {
                addParameter(paramName);
                parameterNamesJoiner.add('`' + paramName + '`');
                preparedParameterJoiner.add("?");
            }
        }
        String parameterNames = parameterNamesJoiner.toString();
        String preparedParameters = preparedParameterJoiner.toString();
        String query = Database.upsertStatement(table, parameterNames, preparedParameters);
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            setStatement(preparedStatement);
        } catch (SQLException e) {
            CLogger.error("Failed to build setter statement with query: " + query);
            throw e;
        }
        initialized = true;

    }

    private void setParameters() {
        for (Pair<String, Function<Integer, Unit>> pair : this.operations) {
            pair.getValue().apply(this.getIndex(pair.getKey()));
        }
    }

    public void execute() {
        this.checkInitialized();
        try {
            if (this.batch) this.getStatement().executeBatch();
            else {
                this.setParameters();
                this.nullRemainingParameters();
                this.getStatement().execute();
            }
            this.getStatement().close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void nullRemainingParameters() throws SQLException {
        LinkedHashMap<String, Integer> result = new LinkedHashMap<>();
        this.associateNamedData.forEach((x, value) -> {
            if (!this.setParameters.contains(x)) result.put(x, value);
        });

        for (Map.Entry<String, Integer> stringIntegerEntry : result.entrySet()) {
            this.getStatement().setObject(((Number) stringIntegerEntry.getValue()).intValue(), null);
        }
        this.setParameters = new HashSet<>();
    }


    public void addBatch() {
        this.checkInitialized();
        if (!this.batch) this.setParameters();
        try {
            this.nullRemainingParameters();
            this.getStatement().addBatch();
            this.resetBatch();
            this.batch = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetBatch() throws SQLException {
        this.getStatement().clearParameters();
    }

    @Override
    public void setString(@NotNull String name, @Nullable String value) {
        Objects.requireNonNull(name);
        this.addOperation(name, index -> {
            try {
                this.getStatement().setString(index, value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public void setInt(@NotNull String name, int value) {
        Objects.requireNonNull(name);
        this.addOperation(name, index -> {
            try {
                this.getStatement().setInt(index, value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    public final void setJson(@NotNull String name, @Nullable JsonElement value) {
        Objects.requireNonNull(name, "name");
//         H2
//        addOperation(name, index -> {
//            byte[] arr;
//            PreparedStatement preparedStatement = this.getStatement();
//            if (value != null) {
//                String json = CastelGson.toString(value);
//                Charset charset = StandardCharsets.UTF_8;
//                arr = json.getBytes(charset);
//            } else {
//                arr = null;
//            }
//            System.out.println("name= " + name + ", value= " + value + ", arr= " + Arrays.toString(arr));
//            System.out.println("statement2 (before set)=" + preparedStatement);
//            try {
//                preparedStatement.setBytes(index, arr);
//            } catch (SQLException e) {
//                throw new RuntimeException(e);
//            }
//            System.out.println("after (after set)=" + preparedStatement);
//            return null;
//        });

        String stringValue = (value != null) ? CastelGson.toString(value) : null;
        setString(name, stringValue);
    }

    @Override
    public void setFloat(@NotNull String name, float value) {
        Objects.requireNonNull(name);
        this.addOperation(name, index -> {
            try {
                this.getStatement().setFloat(index, value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public void setLong(@NotNull String name, long value) {
        Objects.requireNonNull(name);
        this.addOperation(name, index -> {
            try {
                this.getStatement().setLong(index, value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public void setBoolean(@NotNull String name, boolean value) {
        Objects.requireNonNull(name);
        this.addOperation(name, index -> {
            try {
                this.getStatement().setBoolean(index, value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public void setDouble(@NotNull String name, double value) {
        Objects.requireNonNull(name);
        this.addOperation(name, index -> {
            try {
                this.getStatement().setDouble(index, value);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }

    @Override
    public void setUUID(@NotNull String name, @Nullable UUID value) {
        Objects.requireNonNull(name);
        this.addOperation(name, index -> {
            try {
                this.getStatement().setBytes(index, value == null ? null : Database.asBytes(value));
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
            return null;
        });
    }
}