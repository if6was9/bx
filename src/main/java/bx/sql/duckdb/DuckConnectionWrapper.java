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

import com.google.common.base.Preconditions;

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

    return getLiveConnection().unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {

    if (iface.isInstance(conn)) {
      return true;
    }
    return iface.isAssignableFrom(getLiveConnection().getClass());
  }

  @Override
  public Statement createStatement() throws SQLException {
    return getLiveConnection().createStatement();
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return getLiveConnection().prepareStatement(sql);
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    return getLiveConnection().prepareCall(sql);
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    return getLiveConnection().nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {

    getLiveConnection().setAutoCommit(autoCommit);
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
	
    return getLiveConnection().getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
	
    getLiveConnection().commit();
  }


  @Override
  public void rollback() throws SQLException {

    getLiveConnection().rollback();
  }

  @Override
  public void close() throws SQLException {
    // DO NOTHING

  }

  @Override
  public boolean isClosed() throws SQLException {
	  if (conn==null) {
		  return true;
	  }
    return getLiveConnection().isClosed();
  }

  private Connection getLiveConnection() throws SQLException {
	  if (conn==null) {
		  throw new SQLException("connection is closed");
	  }
	  return conn;
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {

    return getLiveConnection().getMetaData();
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {

    getLiveConnection().setReadOnly(readOnly);
  }

  @Override
  public boolean isReadOnly() throws SQLException {
	
    return getLiveConnection().isReadOnly();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    getLiveConnection().setCatalog(catalog);
  }

  @Override
  public String getCatalog() throws SQLException {
    return getLiveConnection().getCatalog();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    getLiveConnection().setTransactionIsolation(level);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return getLiveConnection().getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return getLiveConnection().getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    getLiveConnection().clearWarnings();
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return getLiveConnection().createStatement(resultSetType, resultSetConcurrency);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return getLiveConnection().prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return getLiveConnection().prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return getLiveConnection().getTypeMap();
  }

  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    getLiveConnection().setTypeMap(map);
  }

  public void setHoldability(int holdability) throws SQLException {
    getLiveConnection().setHoldability(holdability);
  }

  public int getHoldability() throws SQLException {
    return getLiveConnection().getHoldability();
  }

  public Savepoint setSavepoint() throws SQLException {
    return getLiveConnection().setSavepoint();
  }

  public Savepoint setSavepoint(String name) throws SQLException {
    return getLiveConnection().setSavepoint(name);
  }

  public void rollback(Savepoint savepoint) throws SQLException {
    getLiveConnection().rollback(savepoint);
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    getLiveConnection().releaseSavepoint(savepoint);
  }

  public Statement createStatement(
      int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    return getLiveConnection().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    return getLiveConnection().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public CallableStatement prepareCall(
      String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    return getLiveConnection().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return getLiveConnection().prepareStatement(sql, autoGeneratedKeys);
  }

  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    return getLiveConnection().prepareStatement(sql, columnIndexes);
  }

  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return getLiveConnection().prepareStatement(sql, columnNames);
  }

  public Clob createClob() throws SQLException {
    return getLiveConnection().createClob();
  }

  public Blob createBlob() throws SQLException {
    return getLiveConnection().createBlob();
  }

  public NClob createNClob() throws SQLException {
    return getLiveConnection().createNClob();
  }

  public SQLXML createSQLXML() throws SQLException {
    return getLiveConnection().createSQLXML();
  }

  public boolean isValid(int timeout) throws SQLException {
    return getLiveConnection().isValid(timeout);
  }

  public void setClientInfo(String name, String value) throws SQLClientInfoException {
	  try {
    getLiveConnection().setClientInfo(name, value);
	  }
	  catch (SQLException e) {
		  throw new SQLClientInfoException(e.toString(),Map.of());
	  }
  }

  public void setClientInfo(Properties properties) throws SQLClientInfoException {
	  try {
    getLiveConnection().setClientInfo(properties);
	  }
	  catch (SQLException e) {
		  throw new SQLClientInfoException(e.toString(),Map.of());
	  }
  }

  public String getClientInfo(String name) throws SQLException {
    return getLiveConnection().getClientInfo(name);
  }

  public Properties getClientInfo() throws SQLException {
    return getLiveConnection().getClientInfo();
  }

  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return getLiveConnection().createArrayOf(typeName, elements);
  }

  public void beginRequest() throws SQLException {
    getLiveConnection().beginRequest();
  }

  public void endRequest() throws SQLException {
    getLiveConnection().endRequest();
  }

  public boolean setShardingKeyIfValid(
      ShardingKey shardingKey, ShardingKey superShardingKey, int timeout) throws SQLException {
    return getLiveConnection().setShardingKeyIfValid(shardingKey, superShardingKey, timeout);
  }

  public boolean setShardingKeyIfValid(ShardingKey shardingKey, int timeout) throws SQLException {
    return getLiveConnection().setShardingKeyIfValid(shardingKey, timeout);
  }

  public void setShardingKey(ShardingKey shardingKey, ShardingKey superShardingKey)
      throws SQLException {
    getLiveConnection().setShardingKey(shardingKey, superShardingKey);
  }

  public void setShardingKey(ShardingKey shardingKey) throws SQLException {
    getLiveConnection().setShardingKey(shardingKey);
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return getLiveConnection().createStruct(typeName, attributes);
  }

  @Override
  public void setSchema(String schema) throws SQLException {
    getLiveConnection().setSchema(schema);
  }

  @Override
  public String getSchema() throws SQLException {
    return getLiveConnection().getSchema();
  }

  @Override
  public void abort(Executor executor) throws SQLException {
    getLiveConnection().abort(executor);
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    getLiveConnection().setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    return getLiveConnection().getNetworkTimeout();
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
