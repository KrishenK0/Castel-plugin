package fr.krishenk.castel.data;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class SQLFlatFileConnectionProvider extends SQLConnectionProvider {
    private final Path file;
    private final NonClosableConnection connection;

    public SQLFlatFileConnectionProvider(String tablePrefix, Path file) {
        super(tablePrefix);
        this.file = file;
        this.testTemporaryLibCreationCore();
        try {
            Connection connection;
            String filePath = this.file.toAbsolutePath().toString();
            /*
            String jdbc = "org.h2.jdbc.JdbcConnection";
            Class<?> connectionClass = URLClassLoader.getSystemClassLoader().loadClass(jdbc);
            Class[] arrayClass = new Class[]{String.class, Process.class, String.class, Object.class, Boolean.TYPE};
            Constructor ctor = connectionClass.getConstructor(arrayClass);
            Object[] arrobj = new Object[]{"jdbc:h2:" + filePath, new Properties(), null, null, false};
            Object obj = ctor.newInstance(arrobj);*/
            Class.forName("org.h2.Driver");
            connection = DriverManager.getConnection("jdbc:h2:" + filePath);
            this.connection = new NonClosableConnection(connection);
        } catch (ReflectiveOperationException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Connection getConnection() {
        return this.connection;
    }

    @Override
    public void close() throws SQLException {
        this.connection.shutdown();
    }
}
