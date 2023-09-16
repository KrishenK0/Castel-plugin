package fr.krishenk.castel.data;

import fr.krishenk.castel.CastelPlugin;
import fr.krishenk.castel.constants.metadata.CastelObject;
import fr.krishenk.castel.data.dataproviders.IdDataTypeHandler;
import fr.krishenk.castel.data.statements.getters.SimpleResultSetQuery;
import fr.krishenk.castel.data.handlers.DataHandler;
import fr.krishenk.castel.data.statements.setters.PreparedNamedSetterStatement;
import fr.krishenk.castel.data.statements.setters.RawSimplePreparedStatement;
import fr.krishenk.castel.utils.string.StringUtils;
import lombok.NonNull;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO : IMPLEMENTS SQLDatabase
public class Database<K, T extends CastelObject<K>> implements CastelDatabase<K, T> {
    public static boolean ranSchema = false;
    private final DataHandler<K, T> dataHandler;
    private final SQLConnectionProvider connectionProvider;
    private final String table;
    private int totalDataCount = 10;
    @Language(value="RegExp")
    private static final String NULLABLE = "( +(?:NOT )?NULL)?";
    @Language(value="RegExp")
    private static final String NOTHING_BEHIND = "(?<!\\w)";
    private static final Pattern LOCAION_TYPE = Database.compile("Location\\((\\w+)\\)");
    private static final Pattern SIMPLE_LOCATION_TYPE = Database.compile("SimpleLocation\\((\\w+)\\)");
    private static final Pattern SIMPLE_CHUNK_LOCATION_TYPE = Database.compile("SimpleChunkLocation\\((\\w+)\\)");
    private static final Logger Logger = CastelPlugin.getInstance().getLogger();

    public Database(String table, DataHandler<K, T> dataHandler, SQLConnectionProvider connectionProvider) {
        this.connectionProvider = connectionProvider;
        this.dataHandler = dataHandler;
        this.table = SQLConnectionProvider.TABLE_PREFIX + table;
        if(!ranSchema) {
            this.printMeta();
            try {
                List<String> statements = SchemaReader.getStatements(CastelPlugin.getInstance().getResource("schema.sql"));
                try (Connection connection = this.getConnection();
                     Statement statement = connection.createStatement()) {
                    for (String query : statements) {
                        query = Database.globalSchemaProcessor(query);
                        query = this.processCommands(query);
                        statement.addBatch(query);
                    }
                    statement.executeBatch();
                }
            } catch (IOException | SQLException e) {
                throw new RuntimeException(e);
            }
            ranSchema = true;
        }
    }
    public String processCommands(String line) {
        line = StringUtils.replace(line, "LONG", "BIGINT");
        line = StringUtils.replace(line, "FLOAT", "REAL");
        line = StringUtils.replace(line, "DOUBLE", "DOUBLE PRECISION");
        line = StringUtils.replace(line, "NVARCHAR", "VARCHAR");
        line = StringUtils.replace(line, "STRICT", "");
        return line;
    }

    private static String globalSchemaProcessor(String query) {
        query = Database.replacePrefix(query);
        query = Database.replace(LOCAION_TYPE, query, "world WORLD", "x DOUBLE", "y DOUBLE", "z DOUBLE", "yaw FLOAT", "pitch FLOAT");
        query = Database.replace(SIMPLE_LOCATION_TYPE, query, "world WORLD", "x INT", "y INT", "z INT");
        query = Database.replace(SIMPLE_CHUNK_LOCATION_TYPE, query, "world WORLD", "x INT", "z INT");
        query = StringUtils.replace(query, "WORLD", "VARCHAR(64)");
        query = StringUtils.replace(query, "RANK_NODE", "VARCHAR(50)");
        query = StringUtils.replace(query, "RANK_NAME", "NVARCHAR(100)");
        query = StringUtils.replace(query, "COLOR", "VARCHAR(30)");
        return query;
    }

    private static String replace(Pattern pattern, String query, String ... fragments) {
        Matcher matcher = pattern.matcher(query);
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String prefix = matcher.group(1);
            String nullability = matcher.group(2) != null ? matcher.group(2) : "";
            StringJoiner builder = new StringJoiner(", ");
            for (String fragment : fragments) {
                builder.add(prefix + '_' + fragment + nullability);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(builder.toString()));
        }
        matcher.appendTail(result);
        return result.toString();
    }

    private static String replacePrefix(String query) {
        return StringUtils.replace(query, "{PREFIX}", SQLConnectionProvider.TABLE_PREFIX);
    }

    private String handleQuery(String query) {
        return query;
    }

    private void printMeta() {
        try (Connection connection = this.getConnection()) {
            DatabaseMetaData meta = connection.getMetaData();
            Logger.info("Running SQL Database:");
            Logger.info("   | Driver: " + meta.getDriverName() + "(" + meta.getCatalogTerm() + ") | " + meta.getDriverVersion());
            Logger.info("   | Product: " + meta.getDatabaseProductName() + " | " + meta.getDatabaseProductVersion());
            Logger.info("   | JDBC: " + meta.getJDBCMajorVersion() + "." + meta.getJDBCMinorVersion());
        } catch (SQLException e) {
            throw new RuntimeException("Failed to retrieve meta information for SQL: H2", e);
        }
    }

    private Connection getConnection() {
        return this.connectionProvider.getConnection();
    }

    private static Pattern compile(String s) {
        String patternString = NOTHING_BEHIND + s + NULLABLE;
        return Pattern.compile(patternString);
    }

    @NotNull
    public static String upsertStatement(@NotNull String table, @NotNull String parameters, @NotNull String preparedValues) {
        Objects.requireNonNull(table);
        Objects.requireNonNull(parameters);
        Objects.requireNonNull(preparedValues);
        return "MERGE INTO `" + table + "` (" + parameters + ") VALUES(" + preparedValues + ')';
    }

    /*
    public static Connection getConnection() {
        Connection connection = null;
        try {
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection(CastelPlugin.getConnectionURL());
        } catch (SQLException ex) {
            logger.info("Database connection problem." + ex.toString());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return connection;
    }

    public static void initDB() {
        Connection connection = getConnection();
        PreparedStatement preparedStatement;
        try {
            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS Groups (" +
                    "UUID varchar(255)," +
                    "LeaderUUID varchar(255)," +
                    "Name varchar(255)" +
                    ");");
            preparedStatement.execute();
            connection.close();
        } catch (SQLException ex) {
            logger.info("Failed initializing the database");
        }
    }*/

    public static byte[] asBytes(UUID uuid) {
        if (uuid == null) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID asUUID(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    @Nullable
    @Override
    public T load(@NonNull K key) {
        Objects.requireNonNull(key);
        String query = handleQuery("SELECT * FROM `" + this.table + "` WHERE " + this.dataHandler.getIdHandler().getWhereClause());
        try (Connection connection = getConnection()) {
            SQLDataGetterProvider<K> provider;
            T data;
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                this.dataHandler.getIdHandler().setSQL(new RawSimplePreparedStatement(ps), key);
                try (ResultSet result = ps.executeQuery()) {
                    if (result.next()) {
                        provider = new SQLDataGetterProvider<>(key, this.dataHandler.getIdHandler(), connection, this.table, null, false, false, new SimpleResultSetQuery(result));
                        data = this.dataHandler.load(provider, key);
                    } else data = null;
                }
            }
            return data;
        } catch (SQLException var14) {
            throw new RuntimeException("Error while loading data for key [" + key + "] with query: " + query, var14);
        }
    }

    @Override
    public @NonNull Collection<T> load(@NonNull Collection<K> keys, @NonNull Collection<T> to, @NonNull DataManager<K, T> dataManager) {
        Objects.requireNonNull(keys);
        Objects.requireNonNull(to);
        Objects.requireNonNull(dataManager);
        if (keys.isEmpty()) return to;
        int columCount = this.dataHandler.getIdHandler().getColumns().length;
        String inClause = StringUtils.repeat('(' + this.dataHandler.getIdHandler().getInClause() + "),", keys.size());
        String query = "SELECT * FROM `" + this.table + "` WHERE (" + this.dataHandler.getIdHandler().getColumnsTuple() + ") IN(" + inClause.substring(0, inClause.length()-1) + ')';
        try (Connection connection = this.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                int i = 0;
                for (K key : keys) {
                    this.dataHandler.getIdHandler().setSQL(new RawSimplePreparedStatement(i * columCount + 1, ps), key);
                    i++;
                }
                try (ResultSet result = ps.executeQuery()) {
                    while (result.next()) {
                        K key = this.dataHandler.getIdHandler().fromSQL(new SimpleResultSetQuery(result));
                        SQLDataGetterProvider<K> provider = new SQLDataGetterProvider<>(key, this.dataHandler.getIdHandler(), connection, this.table, null, false, false, new SimpleResultSetQuery(result));
                        to.add(this.dataHandler.load(provider, key));
                    }
                }
            } catch (Throwable e) {
                if (connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable ex) {
                        e.addSuppressed(ex);
                    }
                }

                throw e;
            }

            if (connection != null) {
                connection.close();
            }
            return to;
        } catch (SQLException e) {
            throw new RuntimeException("Error while loading data for key [" + keys + "] with query: " + query, e);
        }
    }

    @Override
    public void save(@NonNull T data) {
        PreparedNamedSetterStatement ps = new PreparedNamedSetterStatement(this.dataHandler.getSqlProperties().getAssociateNamedData());
        try (Connection connection = this.getConnection()) {
            SQLDataSetterProvider provider = new SQLDataSetterProvider(data.getDataKey(), this.dataHandler.getIdHandler(), connection, this.table, null, false, false, ps);
            this.dataHandler.save(provider, data);
            ps.buildStatement(this.table, connection);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error while saving data " + data.getDataKey() + " (" + data.getClass() + ')', e);
        }
    }

    @Override
    public void delete(@NonNull K key) {
        String query = this.handleQuery("DELETE FROM `" + this.table + "` WHERE " + this.dataHandler.getIdHandler().getWhereClause());
        try (Connection connection = this.getConnection(); PreparedStatement ps = connection.prepareStatement(query)) {
            this.dataHandler.getIdHandler().setSQL(new RawSimplePreparedStatement(ps), key);
            ps.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Error while deleting data with query: " + query, e);
        }
    }

    @Override
    public boolean hasData(@NonNull K key) {
        String query = this.handleQuery("SELECT 1 FROM `" + this.table + "` WHERE " + this.dataHandler.getIdHandler().getWhereClause());
        try (Connection connection = this.getConnection()) {
            boolean hasData;
            block: {
                PreparedStatement ps = connection.prepareStatement(query);
                try {
                    this.dataHandler.getIdHandler().setSQL(new RawSimplePreparedStatement(ps), key);
                    ResultSet result = ps.executeQuery();
                    hasData =  result.next();
                    if (ps == null) break block;
                } catch (SQLException e) {
                    if (ps != null) {
                        try {
                            ps.close();
                        } catch (SQLException ex) {
                            ex.addSuppressed(ex);
                        }
                    }
                    throw e;
                }
                ps.close();
            }
            return hasData;
        } catch (SQLException e) {
            throw new RuntimeException("Error while attempting to check if data exists with query: " + query, e);
        }
    }

    @Override
    public @NonNull Collection<K> getDataKeys() {
        ArrayList<K> keys = new ArrayList<>(this.totalDataCount);
        String query = this.handleQuery("SELECT " + this.dataHandler.getIdHandler().getColumnsTuple() + " FROM `" + this.table + '`');
        try (Connection connection = this.getConnection()) {
            ArrayList<K> arrayList;
            block24: {
                Statement statement = connection.createStatement();
                try {
                    try (ResultSet result = statement.executeQuery(query)){
                        while (result.next()) {
                            K key = this.dataHandler.getIdHandler().fromSQL(new SimpleResultSetQuery(result));
                            keys.add(key);
                        }
                    }
                    catch (Throwable ex) {
                        throw new RuntimeException("Error while getting key from query: " + query, ex);
                    }
                    this.totalDataCount = Math.max(this.totalDataCount, keys.size());
                    arrayList = keys;
                    if (statement == null) break block24;
                }
                catch (Throwable throwable) {
                    if (statement != null) {
                        try {
                            statement.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                statement.close();
            }
            return arrayList;
        } catch (SQLException e) {
            throw new RuntimeException("Error while running query: ", e);
        }
    }

    @Override
    public void deleteAllData() {
        String query = this.handleQuery("DROP TABLE `" + this.table + '`');
        try (Connection connection = this.getConnection(); Statement statement = connection.createStatement()) {
            statement.executeQuery(query);
        } catch (SQLException e) {
            throw new RuntimeException("Error while attempting to drop table: ", e);
        }
    }

    @Override
    public @NonNull Collection<T> loadAllData() {
        ArrayList<T> datas = new ArrayList<>(this.totalDataCount);
        String query = this.handleQuery("SELECT * FROM `" + this.table + '`');
        try (Connection connection = this.getConnection();
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                SimpleResultSetQuery result = new SimpleResultSetQuery(rs);
                K key = this.dataHandler.getIdHandler().fromSQL(result);
                SQLDataGetterProvider<K> provider = new SQLDataGetterProvider<K>(key, this.dataHandler.getIdHandler(), connection, this.table, null, false, false, result);
                datas.add(this.dataHandler.load(provider, key));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error while loading all data with query: " + query, e);
        }
        this.totalDataCount = Math.max(this.totalDataCount, datas.size());
        return datas;
    }

    @Override
    public void save(@NonNull Collection<T> datas) {
        if (datas.isEmpty()) return;
        PreparedNamedSetterStatement ps = new PreparedNamedSetterStatement(this.dataHandler.getSqlProperties().getAssociateNamedData());
        IdDataTypeHandler<K> idHandler = this.dataHandler.getIdHandler();
        try (Connection connection = this.getConnection()) {
            connection.setAutoCommit(false);
            for (T data : datas) {
                SQLDataSetterProvider<K> provider = new SQLDataSetterProvider<>(data.getDataKey(), idHandler, connection, this.table, null, false, false, ps);
                this.dataHandler.getIdHandler().setSQL(ps, data.getDataKey());
                this.dataHandler.save(provider, data);
                ps.buildStatement(this.table, connection);
                ps.addBatch();
            }
            ps.execute();
            connection.commit();
            connection.setAutoCommit(true);
        } catch (SQLException e) {
            throw new RuntimeException("Error while trying to save batch data", e);
        }
    }

    @Override
    public void close() {
        try {
            this.connectionProvider.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
