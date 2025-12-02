package bx.sql.duckdb;

import bx.sql.DbException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.sql.DataSource;

public class DuckDb {

  public static DataSource create(String url) {
    try {
      Connection c = DriverManager.getConnection(url);
      DataSource ds = DuckDataSource.create(c);
      return ds;
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public static DataSource createInMemory() {
    return DuckDataSource.createInMemory();
  }
}
