package bx.util;

import bx.sql.DbException;
import bx.sql.duckdb.DuckDataSource;
import com.google.common.flogger.FluentLogger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.springframework.jdbc.core.simple.JdbcClient;

public abstract class BxTest {

  private static final FluentLogger testLogger = FluentLogger.forEnclosingClass();
  private List<java.lang.AutoCloseable> deferredAutoCloseable = new java.util.ArrayList<>();

  DuckDataSource testDataSource;

  protected void defer(java.lang.AutoCloseable c) {
    deferredAutoCloseable.add(c);
  }

  public void loadAdsbTable(String name) {
    var client = JdbcClient.create(testDataSource());
    client
        .sql(
            "create table "
                + name
                + " as (select * from 'src/test/resources/adsb.csv' order by ts)")
        .update();
  }

  public JdbcClient testJdbcClient() {
    return JdbcClient.create(testDataSource());
  }

  public DataSource testDataSource() {
    try {
      if (testDataSource == null) {
        Connection c = DriverManager.getConnection("jdbc:duckdb:");
        defer(c);
        DuckDataSource ds = (DuckDataSource) DuckDataSource.create(c);
        this.testDataSource = ds;
      }
      return testDataSource;
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  @AfterEach
  private final void bqTestCleanup() {

    this.testDataSource = null;

    try {
      for (AutoCloseable c : deferredAutoCloseable) {
        try {
          testLogger.atInfo().log("closing %s", c);
          c.close();
        } catch (Exception e) {
          testLogger.atWarning().withCause(e).log("problem closing %s", c);
        }
      }
    } finally {
      this.deferredAutoCloseable.clear();
    }
  }
}
