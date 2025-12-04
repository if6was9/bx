package bx.sql.duckdb;

import bx.util.BxException;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.ShardingKey;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

class DuckConnectionWrapper implements Connection {

  Connection conn;

  public DuckConnectionWrapper(Connection c) {
    this.conn = c;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isAssignableFrom(conn.getClass())) {
      return (T) conn;
    }

    return conn.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return iface.isAssignableFrom(conn.getClass());
  }

  @Override
  public Statement createStatement() throws SQLException {
    return conn.createStatement();
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return conn.prepareStatement(sql);
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    return conn.prepareCall(sql);
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    return conn.nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    conn.setAutoCommit(autoCommit);
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return conn.getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
    conn.commit();
  }

  @Override
  public void rollback() throws SQLException {
    conn.rollback();
  }

  @Override
  public void close() throws SQLException {
    // DO NOTHING

  }

  @Override
  public boolean isClosed() throws SQLException {

    return conn.isClosed();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return conn.getMetaData();
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    conn.setReadOnly(readOnly);
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return conn.isReadOnly();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    conn.setCatalog(catalog);
  }

  @Override
  public String getCatalog() throws SQLException {
    return conn.getCatalog();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    conn.setTransactionIsolation(level);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return conn.getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return conn.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    conn.clearWarnings();
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return conn.createStatement(resultSetType, resultSetConcurrency);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return conn.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return conn.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return conn.getTypeMap();
  }

  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    conn.setTypeMap(map);
  }

  public void setHoldability(int holdability) throws SQLException {
    conn.setHoldability(holdability);
  }

  public int getHoldability() throws SQLException {
    return conn.getHoldability();
  }

  public Savepoint setSavepoint() throws SQLException {
    return conn.setSavepoint();
  }

  public Savepoint setSavepoint(String name) throws SQLException {
    return conn.setSavepoint(name);
  }

  public void rollback(Savepoint savepoint) throws SQLException {
    conn.rollback(savepoint);
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    conn.releaseSavepoint(savepoint);
  }

  public Statement createStatement(
      int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return conn.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    return conn.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public CallableStatement prepareCall(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    return conn.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return conn.prepareStatement(sql, autoGeneratedKeys);
  }

  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    return conn.prepareStatement(sql, columnIndexes);
  }

  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return conn.prepareStatement(sql, columnNames);
  }

  public Clob createClob() throws SQLException {
    return conn.createClob();
  }

  public Blob createBlob() throws SQLException {
    return conn.createBlob();
  }

  public NClob createNClob() throws SQLException {
    return conn.createNClob();
  }

  public SQLXML createSQLXML() throws SQLException {
    return conn.createSQLXML();
  }

  public boolean isValid(int timeout) throws SQLException {
    return conn.isValid(timeout);
  }

  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    conn.setClientInfo(name, value);
  }

  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    conn.setClientInfo(properties);
  }

  public String getClientInfo(String name) throws SQLException {
    return conn.getClientInfo(name);
  }

  public Properties getClientInfo() throws SQLException {
    return conn.getClientInfo();
  }

  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return conn.createArrayOf(typeName, elements);
  }

  public void beginRequest() throws SQLException {
    conn.beginRequest();
  }

  public void endRequest() throws SQLException {
    conn.endRequest();
  }

  public boolean setShardingKeyIfValid(
      ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
    return conn.setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
  }

  public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
    return conn.setShardingKeyIfValid(shardingKey, timeout);
  }

  public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey)
      throws SQLException {
    conn.setShardingKey(shardingKey, superShardingKey);
  }

  public void setShardingKey(ShardingKey shardingKey) throws SQLException {
    conn.setShardingKey(shardingKey);
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return conn.createStruct(typeName, attributes);
  }

  @Override
  public void setSchema(String schema) throws SQLException {
    conn.setSchema(schema);
  }

  @Override
  public String getSchema() throws SQLException {
    return conn.getSchema();
  }

  @Override
  public void abort(Executor executor) throws SQLException {
    conn.abort(executor);
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    conn.setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    return conn.getNetworkTimeout();
  }

  public void destroy() {
    if (conn != null) {
      try {
        conn.close();
        conn = null;
      } catch (SQLException e) {
        conn = null;
        throw new BxException(e);
      }
    }
  }
}
