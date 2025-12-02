package bx.sql.duckdb;

import bx.util.BxException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;
import javax.sql.DataSource;

public class DuckDataSource implements DataSource, AutoCloseable {

  DuckConnectionWrapper connection;

  public static DataSource create(Connection c) {

    DuckDataSource ds = new DuckDataSource();
    if (c instanceof DuckConnectionWrapper) {
      ds.connection = (DuckConnectionWrapper) c;
    } else {
      ds.connection = new DuckConnectionWrapper(c);
    }
    return ds;
  }

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public Connection getConnection() throws SQLException {

    return connection;
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {

    return connection;
  }

  @Override
  public PrintWriter getLogWriter() throws SQLException {

    return null;
  }

  @Override
  public void setLogWriter(PrintWriter out) throws SQLException {
    // TODO Auto-generated method stub

  }

  @Override
  public void setLoginTimeout(int seconds) throws SQLException {}

  @Override
  public int getLoginTimeout() throws SQLException {
    return 0;
  }

  public void close() {

    if (connection != null) {
      connection.destroy();
    }
  }

  public static DataSource createInMemory() {

    try {

      Connection c = DriverManager.getConnection("jdbc:duckdb:");
      return DuckDataSource.create(c);
    } catch (SQLException e) {
      throw new BxException(e);
    }
  }
}
