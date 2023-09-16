package fr.krishenk.castel.data;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class NonClosableConnection implements Connection {
    private final Connection delegate;

    public NonClosableConnection(Connection delegate) {
        this.delegate = delegate;
    }

    @Override
    public void abort(Executor p0) throws SQLException {
        this.delegate.abort(p0);
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.delegate.clearWarnings();
    }

    @Override
    public void commit() throws SQLException {
        this.delegate.commit();
    }

    @Override
    public Array createArrayOf(String p0, Object[] p1) throws SQLException {
        return this.delegate.createArrayOf(p0, p1);
    }

    @Override
    public Blob createBlob() throws SQLException {
        return this.delegate.createBlob();
    }

    @Override
    public Clob createClob() throws SQLException {
        return this.delegate.createClob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return this.delegate.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return this.delegate.createSQLXML();
    }

    @Override
    public Statement createStatement() throws SQLException {
        return this.delegate.createStatement();
    }

    @Override
    public Statement createStatement(int p0, int p1) throws SQLException {
        return this.delegate.createStatement(p0, p1);
    }

    @Override
    public Statement createStatement(int p0, int p1, int p2) throws SQLException {
        return this.delegate.createStatement(p0, p1, p2);
    }

    @Override
    public Struct createStruct(String p0, Object[] p1) throws SQLException {
        return this.delegate.createStruct(p0, p1);
    }

    @Override
    public boolean getAutoCommit() throws SQLException {
        return this.delegate.getAutoCommit();
    }

    @Override
    public String getCatalog() throws SQLException {
        return this.delegate.getCatalog();
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return this.delegate.getClientInfo();
    }

    @Override
    public String getClientInfo(String p0) throws SQLException {
        return this.delegate.getClientInfo(p0);
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.delegate.getHoldability();
    }

    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        return this.delegate.getMetaData();
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
        return this.delegate.getNetworkTimeout();
    }

    @Override
    public String getSchema() throws SQLException {
        return this.delegate.getSchema();
    }

    @Override
    public int getTransactionIsolation() throws SQLException {
        return this.delegate.getTransactionIsolation();
    }

    @Override
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return this.delegate.getTypeMap();
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.delegate.getWarnings();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.delegate.isClosed();
    }

    @Override
    public boolean isReadOnly() throws SQLException {
        return this.delegate.isReadOnly();
    }

    @Override
    public boolean isValid(int p0) throws SQLException {
        return this.delegate.isValid(p0);
    }

    @Override
    public String nativeSQL(String p0) throws SQLException {
        return this.delegate.nativeSQL(p0);
    }

    @Override
    public CallableStatement prepareCall(String p0) throws SQLException {
        return this.delegate.prepareCall(p0);
    }

    @Override
    public CallableStatement prepareCall(String p0, int p1, int p2) throws SQLException {
        return this.delegate.prepareCall(p0, p1, p2);
    }

    @Override
    public CallableStatement prepareCall(String p0, int p1, int p2, int p3) throws SQLException {
        return this.delegate.prepareCall(p0, p1, p2, p3);
    }

    @Override
    public PreparedStatement prepareStatement(String p0) throws SQLException {
        return this.delegate.prepareStatement(p0);
    }

    @Override
    public PreparedStatement prepareStatement(String p0, String[] p1) throws SQLException {
        return this.delegate.prepareStatement(p0, p1);
    }

    @Override
    public PreparedStatement prepareStatement(String p0, int p1) throws SQLException {
        return this.delegate.prepareStatement(p0, p1);
    }

    @Override
    public PreparedStatement prepareStatement(String p0, int p1, int p2) throws SQLException {
        return this.delegate.prepareStatement(p0, p1, p2);
    }

    @Override
    public PreparedStatement prepareStatement(String p0, int p1, int p2, int p3) throws SQLException {
        return this.delegate.prepareStatement(p0, p1, p2, p3);
    }

    @Override
    public PreparedStatement prepareStatement(String p0, int[] p1) throws SQLException {
        return this.delegate.prepareStatement(p0, p1);
    }

    @Override
    public void releaseSavepoint(Savepoint p0) throws SQLException {
        this.delegate.releaseSavepoint(p0);
    }

    @Override
    public void rollback() throws SQLException {
        this.delegate.rollback();
    }

    @Override
    public void rollback(Savepoint p0) throws SQLException {
        this.delegate.rollback(p0);
    }

    @Override
    public void setAutoCommit(boolean p0) throws SQLException {
        this.delegate.setAutoCommit(p0);
    }

    @Override
    public void setCatalog(String p0) throws SQLException {
        this.delegate.setCatalog(p0);
    }

    @Override
    public void setClientInfo(Properties p0) throws SQLClientInfoException {
        this.delegate.setClientInfo(p0);
    }

    @Override
    public void setClientInfo(String p0, String p1) throws SQLClientInfoException {
        this.delegate.setClientInfo(p0, p1);
    }

    @Override
    public void setHoldability(int p0) throws SQLException {
        this.delegate.setHoldability(p0);
    }

    @Override
    public void setNetworkTimeout(Executor p0, int p1) throws SQLException {
        this.delegate.setNetworkTimeout(p0, p1);
    }

    @Override
    public void setReadOnly(boolean p0) throws SQLException {
        this.delegate.setReadOnly(p0);
    }

    @Override
    public Savepoint setSavepoint() throws SQLException {
        return this.delegate.setSavepoint();
    }

    @Override
    public Savepoint setSavepoint(String p0) throws SQLException {
        return this.delegate.setSavepoint(p0);
    }

    @Override
    public void setSchema(String p0) throws SQLException {
        this.delegate.setSchema(p0);
    }

    @Override
    public void setTransactionIsolation(int p0) throws SQLException {
        this.delegate.setTransactionIsolation(p0);
    }

    @Override
    public void setTypeMap(Map<String, Class<?>> p0) throws SQLException {
        this.delegate.setTypeMap(p0);
    }

    public final void shutdown() throws SQLException {
        this.delegate.close();
    }

    @Override
    public void close() throws SQLException {
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this.delegate) || this.delegate.isWrapperFor(iface);
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T)(iface.isInstance(this.delegate) ? (Object)this.delegate : this.delegate.unwrap(iface));
    }
}
