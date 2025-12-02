package bx.util;

import bx.sql.Db;
import bx.sql.duckdb.DuckDataSource;
import com.google.common.base.Suppliers;
import com.google.common.flogger.FluentLogger;
import java.util.List;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class BxTest {

  private static final FluentLogger testLogger = FluentLogger.forEnclosingClass();
  private List<java.lang.AutoCloseable> deferredAutoCloseable = new java.util.ArrayList<>();

  DuckDataSource testDataSource;

  Db testDb;

  protected void defer(java.lang.AutoCloseable c) {
    deferredAutoCloseable.add(c);
  }

  public void loadAdsbTable(String name) {

    db().getJdbcClient()
        .sql(
            "create table "
                + name
                + " as (select * from 'src/test/resources/adsb.csv' order by ts)")
        .update();
  }

  public Db db() {
    if (testDb == null) {

      DataSource ds = DuckDataSource.createInMemory();

      testDb = new Db(ds);
      defer((AutoCloseable) ds);
    }
    return testDb;
  }

  @BeforeEach
  private final void setup() {

    try {
      var supplierField = Db.class.getDeclaredField("supplier");
      supplierField.setAccessible(true);

      Supplier<Db> s =
          Suppliers.memoize(
              () -> {
                return db();
              });
      supplierField.set(null, s);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new BxException(e);
    }
  }

  @AfterEach
  private final void bqTestCleanup() {

    testDb = null;
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
