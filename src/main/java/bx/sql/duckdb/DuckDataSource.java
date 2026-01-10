package bx.sql.duckdb;

import bx.sql.DbException;
import com.google.common.base.CharMatcher;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Suppliers;
import java.io.File;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.function.Supplier;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.duckdb.DuckDBConnection;

public class DuckDataSource implements DataSource, AutoCloseable {

  DuckDBConnection rootConnection;

  Supplier<String> urlSupplier = Suppliers.memoize(this::extractUrl);
  PrintWriter logWriter = null;

  @Override
  public Logger getParentLogger() throws SQLFeatureNotSupportedException {

    throw new SQLFeatureNotSupportedException();
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    if (iface.isInstance(this)) {
      return (T) this;
    }

    throw new DbException("not a wrapped DataSource");
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    if (iface.isInstance(this)) {
      return true;
    }
    return false;
  }

  public boolean isClosed() {
    if (rootConnection == null) {
      return true;
    }
    return true;
  }

  @Override
  public Connection getConnection() throws SQLException {

    if (rootConnection == null) {
      throw new SQLException("dataSource is closed");
    }
    return rootConnection.duplicate();
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    if (rootConnection == null) {
      throw new SQLException("dataSource is closed");
    }
    return rootConnection.duplicate();
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

  public void close() throws SQLException {
    if (rootConnection != null) {
      rootConnection.close();
      rootConnection = null;
    }
  }

  public static DuckDataSource create(String url) {
    try {
      Preconditions.checkArgument(
          url.startsWith("jdbc:duckdb:"), "url must start with 'jdubc:duckdb:'");
      Connection c = DriverManager.getConnection(url);
      return DuckDataSource.create(c);
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public static DuckDataSource createInMemory() {

    return create("jdbc:duckdb:");
  }

  public static DuckDataSource create(File dbFile) {
    return create(String.format("jdbc:duckdb:%s", dbFile.toString()));
  }

  public static DuckDataSource create(Connection c) {

    DuckDataSource ds = new DuckDataSource();
    ds.rootConnection = (DuckDBConnection) c;

    return ds;
  }

  private String extractUrl() {
    try {
      String url = this.rootConnection.getMetaData().getURL();
      if (url == null) {
        return url;
      }
      // query string may contain credentials, so strip it since this is just used for logging
      return Splitter.on(CharMatcher.anyOf(";?")).splitToList(url).getFirst();

    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public String toString() {
    ToStringHelper h = MoreObjects.toStringHelper(getClass());
    try {
      h.add("url", this.urlSupplier.get());
    } catch (RuntimeException e) {
      // ignore
    }
    return h.toString();
  }
}
