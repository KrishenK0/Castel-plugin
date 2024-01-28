package fr.krishenk.castel.data;

import com.zaxxer.hikari.HikariConfig;
import fr.krishenk.castel.config.CastelConfig;
import fr.krishenk.castel.utils.config.ConfigSection;
import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

public class DatabaseProperties {
    public String user;
    public String password;
    public String address;
    private int port;
    public String databaseName;
    private boolean useSSL = true;
    private boolean verifyServerCertificate = true;
    private boolean allowPublicKeyRetrieval;
    private final long socketTimeout = Duration.ofSeconds(30L).toMillis();
    private final Map<String, Object> others = new HashMap<>();
    private final Set<String> ignoredProperties = new HashSet<>();
    private static final DatabaseProperties DEFAULTS = new DatabaseProperties();
    private static boolean initialized;

    @NotNull
    public String getUser() {
        Objects.requireNonNull(user, "User has not been initialized");
        return this.user;
    }

    public void setUser(@NotNull String user) {
        this.user = user;
    }

    @NotNull
    public String getPassword() {
        Objects.requireNonNull(password, "Password has not been initialized");
        return password;
    }

    public void setPassword(@NotNull String password) {
        this.password = password;
    }

    @NotNull
    public String getAddress() {
        Objects.requireNonNull(address, "Address has not been initialized");
        return address;
    }

    public void setAddress(@NotNull String address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @NotNull
    public String getDatabaseName() {
        Objects.requireNonNull(databaseName, "Database name has not been initialized");
        return databaseName;
    }

    public void setDatabaseName(@NotNull String databaseName) {
        this.databaseName = databaseName;
    }

    public boolean getUseSSL() {
        return useSSL;
    }

    public void setUseSSL(boolean useSSL) {
        this.useSSL = useSSL;
    }

    public boolean getVerifyServerCertificate() {
        return verifyServerCertificate;
    }

    public void setVerifyServerCertificate(boolean verifyServerCertificate) {
        this.verifyServerCertificate = verifyServerCertificate;
    }

    public boolean getAllowPublicKeyRetrieval() {
        return allowPublicKeyRetrieval;
    }

    public void setAllowPublicKeyRetrieval(boolean allowPublicKeyRetrieval) {
        this.allowPublicKeyRetrieval = allowPublicKeyRetrieval;
    }

    public long getSocketTimeout() {
        return socketTimeout;
    }

    @NotNull
    public Map<String, Object> getOthers() {
        return others;
    }

    @NotNull
    public Set<String> getIgnoredProperties() {
        return ignoredProperties;
    }

    public final boolean ignore(String... name) {
        ignoredProperties.addAll(Arrays.asList(name));
        return true;
    }

    public final void add(@NotNull String name, @NotNull Object value) {
        others.put(name, value);
    }

    public final void useStandardDataSourcePropertyAppender(@NotNull HikariConfig hikariConfig) {
        Objects.requireNonNull(hikariConfig, "HikariConfig cannot be null");

        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "user", getUser());
        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "password", getPassword());
        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "serverName", getAddress());
        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "port", getPort());
        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "portNumber", getPort());
        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "databaseName", getDatabaseName());
        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "useSSL", getUseSSL());
        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "verifyServerCertificate", getVerifyServerCertificate());
        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "allowPublicKeyRetrieval", getAllowPublicKeyRetrieval());
        useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, "socketTimeout", getSocketTimeout());

        others.forEach((name, value) -> useStandardDataSourcePropertyAppenderAdd(this, hikariConfig, name, value));
    }

    private static final void useStandardDataSourcePropertyAppenderAdd(DatabaseProperties instance, HikariConfig hikariConfig, String name, Object value) {
        if (!instance.getIgnoredProperties().contains(name)) {
            hikariConfig.addDataSourceProperty(name, value);
        }
    }

    public static final DatabaseProperties defaults() {
        if (initialized) return DEFAULTS;

        DatabaseProperties databaseProperties = DEFAULTS;
        databaseProperties.setAddress("localhost");
        databaseProperties.setPort(3306);
        databaseProperties.setUser(CastelConfig.DATABASE_USERNAME.getManager().getString());
        databaseProperties.setPassword(CastelConfig.DATABASE_PASSWORD.getManager().getString());
        databaseProperties.setDatabaseName(CastelConfig.DATABASE_DATABASE.getManager().getString());
        databaseProperties.setUseSSL(true);
        databaseProperties.setVerifyServerCertificate(true);
        databaseProperties.setAllowPublicKeyRetrieval(false);

        ConfigSection dataSourceProperties = CastelConfig.DATABASE_POOL_SETTINGS_PROPERTIES.getManager().getSection().noDefault().getSection();
        dataSourceProperties.getKeys().forEach(key -> {
            Object value = dataSourceProperties.get(key);
            databaseProperties.add(key, value);
        });

        initialized = true;
        return DEFAULTS;

    }

}
