package fr.krishenk.castel.data.statements.getters;

import com.google.common.base.Charsets;
import com.google.gson.JsonElement;
import fr.krishenk.castel.data.CastelGson;
import fr.krishenk.castel.data.Database;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.*;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

public final class SimpleResultSetQuery implements ResultSet {
    @NotNull
    private final ResultSet result;

    public SimpleResultSetQuery(@NotNull ResultSet result) {
        this.result = result;
    }

    @Override
    public boolean absolute(int p0) throws SQLException {
        return this.result.absolute(p0);
    }

    @Override
    public void afterLast() throws SQLException {
        this.result.afterLast();
    }

    @Override
    public void beforeFirst() throws SQLException {
        this.result.beforeFirst();
    }

    @Override
    public void cancelRowUpdates() throws SQLException {
        this.result.cancelRowUpdates();
    }

    @Override
    public void clearWarnings() throws SQLException {
        this.result.clearWarnings();
    }

    @Override
    public void close() throws SQLException {
        this.result.close();
    }

    @Override
    public void deleteRow() throws SQLException {
        this.result.deleteRow();
    }

    @Override
    public int findColumn(String p0) throws SQLException {
        return this.result.findColumn(p0);
    }

    @Override
    public boolean first() throws SQLException {
        return this.result.first();
    }

    @Override
    public Array getArray(int p0) throws SQLException {
        return this.result.getArray(p0);
    }

    @Override
    public Array getArray(String p0) throws SQLException {
        return this.result.getArray(p0);
    }

    @Override
    public InputStream getAsciiStream(int p0) throws SQLException {
        return this.result.getAsciiStream(p0);
    }

    @Override
    public InputStream getAsciiStream(String p0) throws SQLException {
        return this.result.getAsciiStream(p0);
    }

    @Override
    public BigDecimal getBigDecimal(int p0) throws SQLException {
        return this.result.getBigDecimal(p0);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(int p0, int p1) throws SQLException {
        return this.result.getBigDecimal(p0, p1);
    }

    @Override
    public BigDecimal getBigDecimal(String p0) throws SQLException {
        return this.result.getBigDecimal(p0);
    }

    @Override
    @Deprecated
    public BigDecimal getBigDecimal(String p0, int p1) throws SQLException {
        return this.result.getBigDecimal(p0, p1);
    }

    @Override
    public InputStream getBinaryStream(int p0) throws SQLException {
        return this.result.getBinaryStream(p0);
    }

    @Override
    public InputStream getBinaryStream(String p0) throws SQLException {
        return this.result.getBinaryStream(p0);
    }

    @Override
    public Blob getBlob(int p0) throws SQLException {
        return this.result.getBlob(p0);
    }

    @Override
    public Blob getBlob(String p0) throws SQLException {
        return this.result.getBlob(p0);
    }

    @Override
    public boolean getBoolean(int p0) throws SQLException {
        return this.result.getBoolean(p0);
    }

    @Override
    public boolean getBoolean(String p0) throws SQLException {
        return this.result.getBoolean(p0);
    }

    @Override
    public byte getByte(int p0) throws SQLException {
        return this.result.getByte(p0);
    }

    @Override
    public byte getByte(String p0) throws SQLException {
        return this.result.getByte(p0);
    }

    @Override
    public byte[] getBytes(int p0) throws SQLException {
        return this.result.getBytes(p0);
    }

    @Override
    public byte[] getBytes(String p0) throws SQLException {
        return this.result.getBytes(p0);
    }

    @Override
    public Reader getCharacterStream(int p0) throws SQLException {
        return this.result.getCharacterStream(p0);
    }

    @Override
    public Reader getCharacterStream(String p0) throws SQLException {
        return this.result.getCharacterStream(p0);
    }

    @Override
    public Clob getClob(int p0) throws SQLException {
        return this.result.getClob(p0);
    }

    @Override
    public Clob getClob(String p0) throws SQLException {
        return this.result.getClob(p0);
    }

    @Override
    public int getConcurrency() throws SQLException {
        return this.result.getConcurrency();
    }

    @Override
    public String getCursorName() throws SQLException {
        return this.result.getCursorName();
    }

    @Override
    public Date getDate(int p0) throws SQLException {
        return this.result.getDate(p0);
    }

    @Override
    public Date getDate(int p0, Calendar p1) throws SQLException {
        return this.result.getDate(p0, p1);
    }

    @Override
    public Date getDate(String p0) throws SQLException {
        return this.result.getDate(p0);
    }

    @Override
    public Date getDate(String p0, Calendar p1) throws SQLException {
        return this.result.getDate(p0, p1);
    }

    @Override
    public double getDouble(int p0) throws SQLException {
        return this.result.getDouble(p0);
    }

    @Override
    public double getDouble(String p0) throws SQLException {
        return this.result.getDouble(p0);
    }

    @Override
    public int getFetchDirection() throws SQLException {
        return this.result.getFetchDirection();
    }

    @Override
    public int getFetchSize() throws SQLException {
        return this.result.getFetchSize();
    }

    @Override
    public float getFloat(int p0) throws SQLException {
        return this.result.getFloat(p0);
    }

    @Override
    public float getFloat(String p0) throws SQLException {
        return this.result.getFloat(p0);
    }

    @Override
    public int getHoldability() throws SQLException {
        return this.result.getHoldability();
    }

    @Override
    public int getInt(int p0) throws SQLException {
        return this.result.getInt(p0);
    }

    @Override
    public int getInt(String p0) throws SQLException {
        return this.result.getInt(p0);
    }

    @Override
    public long getLong(int p0) throws SQLException {
        return this.result.getLong(p0);
    }

    @Override
    public long getLong(String p0) throws SQLException {
        return this.result.getLong(p0);
    }

    @Override
    public ResultSetMetaData getMetaData() throws SQLException {
        return this.result.getMetaData();
    }

    @Override
    public Reader getNCharacterStream(int p0) throws SQLException {
        return this.result.getNCharacterStream(p0);
    }

    @Override
    public Reader getNCharacterStream(String p0) throws SQLException {
        return this.result.getNCharacterStream(p0);
    }

    @Override
    public NClob getNClob(int p0) throws SQLException {
        return this.result.getNClob(p0);
    }

    @Override
    public NClob getNClob(String p0) throws SQLException {
        return this.result.getNClob(p0);
    }

    @Override
    public String getNString(int p0) throws SQLException {
        return this.result.getNString(p0);
    }

    @Override
    public String getNString(String p0) throws SQLException {
        return this.result.getNString(p0);
    }

    @Override
    public Object getObject(int p0) throws SQLException {
        return this.result.getObject(p0);
    }

    @Override
    public <T> T getObject(int p0, Class<T> p1) throws SQLException {
        return this.result.getObject(p0, p1);
    }

    @Override
    public Object getObject(int p0, Map<String, Class<?>> p1) throws SQLException {
        return this.result.getObject(p0, p1);
    }

    @Override
    public Object getObject(String p0) throws SQLException {
        return this.result.getObject(p0);
    }

    @Override
    public <T> T getObject(String p0, Class<T> p1) throws SQLException {
        return this.result.getObject(p0, p1);
    }

    @Override
    public Object getObject(String p0, Map<String, Class<?>> p1) throws SQLException {
        return this.result.getObject(p0, p1);
    }

    @Override
    public Ref getRef(int p0) throws SQLException {
        return this.result.getRef(p0);
    }

    @Override
    public Ref getRef(String p0) throws SQLException {
        return this.result.getRef(p0);
    }

    @Override
    public int getRow() throws SQLException {
        return this.result.getRow();
    }

    @Override
    public RowId getRowId(int p0) throws SQLException {
        return this.result.getRowId(p0);
    }

    @Override
    public RowId getRowId(String p0) throws SQLException {
        return this.result.getRowId(p0);
    }

    @Override
    public SQLXML getSQLXML(int p0) throws SQLException {
        return this.result.getSQLXML(p0);
    }

    @Override
    public SQLXML getSQLXML(String p0) throws SQLException {
        return this.result.getSQLXML(p0);
    }

    @Override
    public short getShort(int p0) throws SQLException {
        return this.result.getShort(p0);
    }

    @Override
    public short getShort(String p0) throws SQLException {
        return this.result.getShort(p0);
    }

    @Override
    public Statement getStatement() throws SQLException {
        return this.result.getStatement();
    }

    @Override
    public String getString(int p0) throws SQLException {
        return this.result.getString(p0);
    }

    @Override
    public String getString(String p0) throws SQLException {
        return this.result.getString(p0);
    }

    @Override
    public Time getTime(int p0) throws SQLException {
        return this.result.getTime(p0);
    }

    @Override
    public Time getTime(int p0, Calendar p1) throws SQLException {
        return this.result.getTime(p0, p1);
    }

    @Override
    public Time getTime(String p0) throws SQLException {
        return this.result.getTime(p0);
    }

    @Override
    public Time getTime(String p0, Calendar p1) throws SQLException {
        return this.result.getTime(p0, p1);
    }

    @Override
    public Timestamp getTimestamp(int p0) throws SQLException {
        return this.result.getTimestamp(p0);
    }

    @Override
    public Timestamp getTimestamp(int p0, Calendar p1) throws SQLException {
        return this.result.getTimestamp(p0, p1);
    }

    @Override
    public Timestamp getTimestamp(String p0) throws SQLException {
        return this.result.getTimestamp(p0);
    }

    @Override
    public Timestamp getTimestamp(String p0, Calendar p1) throws SQLException {
        return this.result.getTimestamp(p0, p1);
    }

    @Override
    public int getType() throws SQLException {
        return this.result.getType();
    }

    @Override
    public URL getURL(int p0) throws SQLException {
        return this.result.getURL(p0);
    }

    @Override
    public URL getURL(String p0) throws SQLException {
        return this.result.getURL(p0);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(int p0) throws SQLException {
        return this.result.getUnicodeStream(p0);
    }

    @Override
    @Deprecated
    public InputStream getUnicodeStream(String p0) throws SQLException {
        return this.result.getUnicodeStream(p0);
    }

    @Override
    public SQLWarning getWarnings() throws SQLException {
        return this.result.getWarnings();
    }

    @Override
    public void insertRow() throws SQLException {
        this.result.insertRow();
    }

    @Override
    public boolean isAfterLast() throws SQLException {
        return this.result.isAfterLast();
    }

    @Override
    public boolean isBeforeFirst() throws SQLException {
        return this.result.isBeforeFirst();
    }

    @Override
    public boolean isClosed() throws SQLException {
        return this.result.isClosed();
    }

    @Override
    public boolean isFirst() throws SQLException {
        return this.result.isFirst();
    }

    @Override
    public boolean isLast() throws SQLException {
        return this.result.isLast();
    }

    @Override
    public boolean isWrapperFor(Class<?> p0) throws SQLException {
        return this.result.isWrapperFor(p0);
    }

    @Override
    public boolean last() throws SQLException {
        return this.result.last();
    }

    @Override
    public void moveToCurrentRow() throws SQLException {
        this.result.moveToCurrentRow();
    }

    @Override
    public void moveToInsertRow() throws SQLException {
        this.result.moveToInsertRow();
    }

    @Override
    public boolean next() throws SQLException {
        return this.result.next();
    }

    @Override
    public boolean previous() throws SQLException {
        return this.result.previous();
    }

    @Override
    public void refreshRow() throws SQLException {
        this.result.refreshRow();
    }

    @Override
    public boolean relative(int p0) throws SQLException {
        return this.result.relative(p0);
    }

    @Override
    public boolean rowDeleted() throws SQLException {
        return this.result.rowDeleted();
    }

    @Override
    public boolean rowInserted() throws SQLException {
        return this.result.rowInserted();
    }

    @Override
    public boolean rowUpdated() throws SQLException {
        return this.result.rowUpdated();
    }

    @Override
    public void setFetchDirection(int p0) throws SQLException {
        this.result.setFetchDirection(p0);
    }

    @Override
    public void setFetchSize(int p0) throws SQLException {
        this.result.setFetchSize(p0);
    }

    @Override
    public <T> T unwrap(Class<T> p0) throws SQLException {
        return this.result.unwrap(p0);
    }

    @Override
    public void updateArray(int p0, Array p1) throws SQLException {
        this.result.updateArray(p0, p1);
    }

    @Override
    public void updateArray(String p0, Array p1) throws SQLException {
        this.result.updateArray(p0, p1);
    }

    @Override
    public void updateAsciiStream(int p0, InputStream p1) throws SQLException {
        this.result.updateAsciiStream(p0, p1);
    }

    @Override
    public void updateAsciiStream(int p0, InputStream p1, int p2) throws SQLException {
        this.result.updateAsciiStream(p0, p1, p2);
    }

    @Override
    public void updateAsciiStream(int p0, InputStream p1, long p2) throws SQLException {
        this.result.updateAsciiStream(p0, p1, p2);
    }

    @Override
    public void updateAsciiStream(String p0, InputStream p1) throws SQLException {
        this.result.updateAsciiStream(p0, p1);
    }

    @Override
    public void updateAsciiStream(String p0, InputStream p1, int p2) throws SQLException {
        this.result.updateAsciiStream(p0, p1, p2);
    }

    @Override
    public void updateAsciiStream(String p0, InputStream p1, long p2) throws SQLException {
        this.result.updateAsciiStream(p0, p1, p2);
    }

    @Override
    public void updateBigDecimal(int p0, BigDecimal p1) throws SQLException {
        this.result.updateBigDecimal(p0, p1);
    }

    @Override
    public void updateBigDecimal(String p0, BigDecimal p1) throws SQLException {
        this.result.updateBigDecimal(p0, p1);
    }

    @Override
    public void updateBinaryStream(int p0, InputStream p1) throws SQLException {
        this.result.updateBinaryStream(p0, p1);
    }

    @Override
    public void updateBinaryStream(int p0, InputStream p1, int p2) throws SQLException {
        this.result.updateBinaryStream(p0, p1, p2);
    }

    @Override
    public void updateBinaryStream(int p0, InputStream p1, long p2) throws SQLException {
        this.result.updateBinaryStream(p0, p1, p2);
    }

    @Override
    public void updateBinaryStream(String p0, InputStream p1) throws SQLException {
        this.result.updateBinaryStream(p0, p1);
    }

    @Override
    public void updateBinaryStream(String p0, InputStream p1, int p2) throws SQLException {
        this.result.updateBinaryStream(p0, p1, p2);
    }

    @Override
    public void updateBinaryStream(String p0, InputStream p1, long p2) throws SQLException {
        this.result.updateBinaryStream(p0, p1, p2);
    }

    @Override
    public void updateBlob(int p0, InputStream p1) throws SQLException {
        this.result.updateBlob(p0, p1);
    }

    @Override
    public void updateBlob(int p0, InputStream p1, long p2) throws SQLException {
        this.result.updateBlob(p0, p1, p2);
    }

    @Override
    public void updateBlob(int p0, Blob p1) throws SQLException {
        this.result.updateBlob(p0, p1);
    }

    @Override
    public void updateBlob(String p0, InputStream p1) throws SQLException {
        this.result.updateBlob(p0, p1);
    }

    @Override
    public void updateBlob(String p0, InputStream p1, long p2) throws SQLException {
        this.result.updateBlob(p0, p1, p2);
    }

    @Override
    public void updateBlob(String p0, Blob p1) throws SQLException {
        this.result.updateBlob(p0, p1);
    }

    @Override
    public void updateBoolean(int p0, boolean p1) throws SQLException {
        this.result.updateBoolean(p0, p1);
    }

    @Override
    public void updateBoolean(String p0, boolean p1) throws SQLException {
        this.result.updateBoolean(p0, p1);
    }

    @Override
    public void updateByte(int p0, byte p1) throws SQLException {
        this.result.updateByte(p0, p1);
    }

    @Override
    public void updateByte(String p0, byte p1) throws SQLException {
        this.result.updateByte(p0, p1);
    }

    @Override
    public void updateBytes(int p0, byte[] p1) throws SQLException {
        this.result.updateBytes(p0, p1);
    }

    @Override
    public void updateBytes(String p0, byte[] p1) throws SQLException {
        this.result.updateBytes(p0, p1);
    }

    @Override
    public void updateCharacterStream(int p0, Reader p1) throws SQLException {
        this.result.updateCharacterStream(p0, p1);
    }

    @Override
    public void updateCharacterStream(int p0, Reader p1, int p2) throws SQLException {
        this.result.updateCharacterStream(p0, p1, p2);
    }

    @Override
    public void updateCharacterStream(int p0, Reader p1, long p2) throws SQLException {
        this.result.updateCharacterStream(p0, p1, p2);
    }

    @Override
    public void updateCharacterStream(String p0, Reader p1) throws SQLException {
        this.result.updateCharacterStream(p0, p1);
    }

    @Override
    public void updateCharacterStream(String p0, Reader p1, int p2) throws SQLException {
        this.result.updateCharacterStream(p0, p1, p2);
    }

    @Override
    public void updateCharacterStream(String p0, Reader p1, long p2) throws SQLException {
        this.result.updateCharacterStream(p0, p1, p2);
    }

    @Override
    public void updateClob(int p0, Reader p1) throws SQLException {
        this.result.updateClob(p0, p1);
    }

    @Override
    public void updateClob(int p0, Reader p1, long p2) throws SQLException {
        this.result.updateClob(p0, p1, p2);
    }

    @Override
    public void updateClob(int p0, Clob p1) throws SQLException {
        this.result.updateClob(p0, p1);
    }

    @Override
    public void updateClob(String p0, Reader p1) throws SQLException {
        this.result.updateClob(p0, p1);
    }

    @Override
    public void updateClob(String p0, Reader p1, long p2) throws SQLException {
        this.result.updateClob(p0, p1, p2);
    }

    @Override
    public void updateClob(String p0, Clob p1) throws SQLException {
        this.result.updateClob(p0, p1);
    }

    @Override
    public void updateDate(int p0, Date p1) throws SQLException {
        this.result.updateDate(p0, p1);
    }

    @Override
    public void updateDate(String p0, Date p1) throws SQLException {
        this.result.updateDate(p0, p1);
    }

    @Override
    public void updateDouble(int p0, double p1) throws SQLException {
        this.result.updateDouble(p0, p1);
    }

    @Override
    public void updateDouble(String p0, double p1) throws SQLException {
        this.result.updateDouble(p0, p1);
    }

    @Override
    public void updateFloat(int p0, float p1) throws SQLException {
        this.result.updateFloat(p0, p1);
    }

    @Override
    public void updateFloat(String p0, float p1) throws SQLException {
        this.result.updateFloat(p0, p1);
    }

    @Override
    public void updateInt(int p0, int p1) throws SQLException {
        this.result.updateInt(p0, p1);
    }

    @Override
    public void updateInt(String p0, int p1) throws SQLException {
        this.result.updateInt(p0, p1);
    }

    @Override
    public void updateLong(int p0, long p1) throws SQLException {
        this.result.updateLong(p0, p1);
    }

    @Override
    public void updateLong(String p0, long p1) throws SQLException {
        this.result.updateLong(p0, p1);
    }

    @Override
    public void updateNCharacterStream(int p0, Reader p1) throws SQLException {
        this.result.updateNCharacterStream(p0, p1);
    }

    @Override
    public void updateNCharacterStream(int p0, Reader p1, long p2) throws SQLException {
        this.result.updateNCharacterStream(p0, p1, p2);
    }

    @Override
    public void updateNCharacterStream(String p0, Reader p1) throws SQLException {
        this.result.updateNCharacterStream(p0, p1);
    }

    @Override
    public void updateNCharacterStream(String p0, Reader p1, long p2) throws SQLException {
        this.result.updateNCharacterStream(p0, p1, p2);
    }

    @Override
    public void updateNClob(int p0, Reader p1) throws SQLException {
        this.result.updateNClob(p0, p1);
    }

    @Override
    public void updateNClob(int p0, Reader p1, long p2) throws SQLException {
        this.result.updateNClob(p0, p1, p2);
    }

    @Override
    public void updateNClob(int p0, NClob p1) throws SQLException {
        this.result.updateNClob(p0, p1);
    }

    @Override
    public void updateNClob(String p0, Reader p1) throws SQLException {
        this.result.updateNClob(p0, p1);
    }

    @Override
    public void updateNClob(String p0, Reader p1, long p2) throws SQLException {
        this.result.updateNClob(p0, p1, p2);
    }

    @Override
    public void updateNClob(String p0, NClob p1) throws SQLException {
        this.result.updateNClob(p0, p1);
    }

    @Override
    public void updateNString(int p0, String p1) throws SQLException {
        this.result.updateNString(p0, p1);
    }

    @Override
    public void updateNString(String p0, String p1) throws SQLException {
        this.result.updateNString(p0, p1);
    }

    @Override
    public void updateNull(int p0) throws SQLException {
        this.result.updateNull(p0);
    }

    @Override
    public void updateNull(String p0) throws SQLException {
        this.result.updateNull(p0);
    }

    @Override
    public void updateObject(int p0, Object p1) throws SQLException {
        this.result.updateObject(p0, p1);
    }

    @Override
    public void updateObject(int p0, Object p1, int p2) throws SQLException {
        this.result.updateObject(p0, p1, p2);
    }

    @Override
    public void updateObject(String p0, Object p1) throws SQLException {
        this.result.updateObject(p0, p1);
    }

    @Override
    public void updateObject(String p0, Object p1, int p2) throws SQLException {
        this.result.updateObject(p0, p1, p2);
    }

    @Override
    public void updateRef(int p0, Ref p1) throws SQLException {
        this.result.updateRef(p0, p1);
    }

    @Override
    public void updateRef(String p0, Ref p1) throws SQLException {
        this.result.updateRef(p0, p1);
    }

    @Override
    public void updateRow() throws SQLException {
        this.result.updateRow();
    }

    @Override
    public void updateRowId(int p0, RowId p1) throws SQLException {
        this.result.updateRowId(p0, p1);
    }

    @Override
    public void updateRowId(String p0, RowId p1) throws SQLException {
        this.result.updateRowId(p0, p1);
    }

    @Override
    public void updateSQLXML(int p0, SQLXML p1) throws SQLException {
        this.result.updateSQLXML(p0, p1);
    }

    @Override
    public void updateSQLXML(String p0, SQLXML p1) throws SQLException {
        this.result.updateSQLXML(p0, p1);
    }

    @Override
    public void updateShort(int p0, short p1) throws SQLException {
        this.result.updateShort(p0, p1);
    }

    @Override
    public void updateShort(String p0, short p1) throws SQLException {
        this.result.updateShort(p0, p1);
    }

    @Override
    public void updateString(int p0, String p1) throws SQLException {
        this.result.updateString(p0, p1);
    }

    @Override
    public void updateString(String p0, String p1) throws SQLException {
        this.result.updateString(p0, p1);
    }

    @Override
    public void updateTime(int p0, Time p1) throws SQLException {
        this.result.updateTime(p0, p1);
    }

    @Override
    public void updateTime(String p0, Time p1) throws SQLException {
        this.result.updateTime(p0, p1);
    }

    @Override
    public void updateTimestamp(int p0, Timestamp p1) throws SQLException {
        this.result.updateTimestamp(p0, p1);
    }

    @Override
    public void updateTimestamp(String p0, Timestamp p1) throws SQLException {
        this.result.updateTimestamp(p0, p1);
    }

    @Override
    public boolean wasNull() throws SQLException {
        return this.result.wasNull();
    }

    @Nullable
    public UUID getUUID(@NotNull String name) throws SQLException {
        UUID uuid;
        byte[] bytes = this.result.getBytes(name);
        if (bytes != null) {
            byte[] it = bytes;
            boolean b1 = false;
            uuid = Database.asUUID(it);
        } else {
            uuid = null;
        }
        return uuid;
    }

    @Nullable
    public JsonElement getJSON(@NotNull String name) throws SQLException {
        String string = null;
        byte[] arrby = this.result.getBytes(name);
        if (arrby != null) {
            byte[] arrby2 = arrby;
            Charset charset = Charsets.UTF_8;
            string = new String(arrby2, charset);
        }
        return CastelGson.fromString(string);
    }
}
