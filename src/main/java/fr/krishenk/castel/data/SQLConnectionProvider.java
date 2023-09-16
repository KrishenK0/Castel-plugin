package fr.krishenk.castel.data;

import fr.krishenk.castel.CLogger;
import fr.krishenk.castel.CastelPlugin;

import java.io.File;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class SQLConnectionProvider {
    public static final Companion Companion = new Companion();
    private final String tablePrefix;
    public static final String TABLE_PREFIX = "castel_";
    private static SQLConnectionProvider DEFAULT_PROVIDER;

    public SQLConnectionProvider(String tablePrefix) {
        this.tablePrefix = tablePrefix;
    }

    public abstract Connection getConnection();

    public abstract void close() throws SQLException;

    public final void testTemporaryLibCreationCore() {
        File tmpDir = new File(System.getProperty("java.io.tmpdir"));
        if(!(tmpDir.exists() && tmpDir.isDirectory() && tmpDir.canRead() && tmpDir.canWrite())) {
            CLogger.info("A problem has occured for with java.io.tmpdir" + tmpDir.exists() + "|" + tmpDir.isDirectory() + "|" + tmpDir.canRead() + "|" + tmpDir.canWrite());
        }
    }
    
    public static final SQLConnectionProvider getDefaultProvider() { return Companion.getDefaultProvider(); }
    
    public static final void setDefaultProviderIfNull(Path path) {
        Companion.setDefaultProviderIfNull(path);
    }

    public static class Companion {
        public Companion() {}
        public SQLConnectionProvider getDefaultProvider() {
            SQLConnectionProvider sqlConnectionProvider = DEFAULT_PROVIDER;
            if (sqlConnectionProvider == null)
                throw new IllegalStateException("Default provider not initialized yet");
            return sqlConnectionProvider;
        }

        public void setDefaultProviderIfNull(Path path) {
            SQLConnectionProvider sqlConnectionProvider;
            if (DEFAULT_PROVIDER != null) return;
            Path databasePath = path.resolve("data.db");
            sqlConnectionProvider = new SQLFlatFileConnectionProvider(TABLE_PREFIX, databasePath);
            DEFAULT_PROVIDER = sqlConnectionProvider;
        }
    }
}
