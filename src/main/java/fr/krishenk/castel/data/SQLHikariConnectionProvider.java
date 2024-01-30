package fr.krishenk.castel.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import fr.krishenk.castel.CLogger;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Map;
import java.util.Objects;

public class SQLHikariConnectionProvider extends SQLConnectionProvider {
    private final HikariDataSource hikari;

    public SQLHikariConnectionProvider(String tablePrefix, HikariDataSource hikari) {
        super(tablePrefix);
        this.hikari = hikari;
        this.hikari.setDataSourceClassName("org.mariadb.jdbc.MariaDbDataSource");
        applyProperties(this.hikari, DatabaseProperties.defaults());
        if (CLogger.isDebugging()) this.hikari.setLeakDetectionThreshold(Duration.ofSeconds(30L).toMillis());

        this.hikari.setPoolName("castel-hikari");
        this.hikari.setMaximumPoolSize(10);
        this.hikari.setMinimumIdle(10);
        this.hikari.setMaxLifetime(Duration.ofMinutes(30L).toMillis());
        this.hikari.setKeepaliveTime(0);
        this.hikari.setConnectionTimeout(Duration.ofSeconds(5L).toMillis());
    }

    void applyProperties(HikariConfig hikariConfig, DatabaseProperties databaseProperties) {
        StringBuilder propertiesStrBuilder = new StringBuilder();
        if (databaseProperties.getOthers().isEmpty()) {
            char separator = '?';
            for (Map.Entry<String, Object> entry : databaseProperties.getOthers().entrySet()) {
                propertiesStrBuilder.append(entry.getKey()).append('=').append(entry.getValue()).append('&');
            }
            propertiesStrBuilder.deleteCharAt(propertiesStrBuilder.length()-1);
            propertiesStrBuilder.insert(0, separator);
        }

        String url = "jdbc:mariadb://" + databaseProperties.getAddress() + ':' + databaseProperties.getPort() + '/' + databaseProperties.getDatabaseName() + propertiesStrBuilder.toString();

        hikariConfig.addDataSourceProperty("url", url);
        hikariConfig.addDataSourceProperty("user", databaseProperties.getUser());
        hikariConfig.addDataSourceProperty("password", databaseProperties.getPassword());
    }

    @Override
    public Connection getConnection() {
        Connection connection;
        try {
            connection = this.hikari.getConnection();
        } catch (SQLException e) {
            throw new IllegalStateException(e);
        }
        if (connection == null) throw new IllegalStateException("Unable to get a connection from the pool.");
        return connection;
    }

    @Override
    public void close() throws SQLException {
        this.hikari.close();
    }
}
