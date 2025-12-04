package bx.util;

import bx.sql.Db;
import bx.sql.duckdb.DuckDataSource;
import bx.sql.duckdb.DuckTable;
import com.google.common.base.Suppliers;
import java.util.List;
import java.util.function.Supplier;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BxTest {

  Logger logger = LoggerFactory.getLogger(BxTest.class);

  private List<java.lang.AutoCloseable> deferredAutoCloseable = new java.util.ArrayList<>();

  DuckDataSource testDataSource;

  Db testDb;

  protected void defer(java.lang.AutoCloseable c) {
    deferredAutoCloseable.add(c);
  }

  public DuckTable loadAdsbTable(String name) {

    db().getJdbcClient()
        .sql(
            "create table "
                + name
                + " as (select * from 'src/test/resources/adsb.csv' order by ts)")
        .update();

    return DuckTable.of(db().getDataSource(), name);
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
  final void setup() {

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
  final void bqTestCleanup() {

    testDb = null;
    try {
      for (AutoCloseable c : deferredAutoCloseable) {
        try {
          logger.atInfo().log("closing {}", c);

          c.close();
        } catch (Exception e) {
          logger.atWarn().setCause(e).log("problem closing {}", c);
        }
      }
    } finally {
      this.deferredAutoCloseable.clear();
    }
  }
}
