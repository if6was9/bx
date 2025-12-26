package bx.util;

import bx.sql.Db;
import bx.sql.PrettyQuery;
import bx.sql.duckdb.DuckCsv;
import bx.sql.duckdb.DuckDataSource;
import bx.sql.duckdb.DuckTable;
import java.io.File;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BxTest {

  Logger logger = LoggerFactory.getLogger(BxTest.class);

  private List<java.lang.AutoCloseable> deferredAutoCloseable = new java.util.ArrayList<>();

  private DuckDataSource testDataSource;

  Db testDb;

  protected void defer(java.lang.AutoCloseable c) {
    deferredAutoCloseable.add(c);
  }

  public DuckTable loadAdsbTable(String name) {

    return DuckCsv.using(dataSource())
        .table(name)
        .from(new File("./src/test/resources/adsb.csv"))
        .load();
  }

  public DataSource dataSource() {
    return db().getDataSource();
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
    Db.reset(db());
  }

  PrettyQuery prettyQuery() {
    return PrettyQuery.with(dataSource()).out(LoggerFactory.getLogger(getClass()));
  }

  @AfterEach
  final void bqTestCleanup() {

    testDb = null;
    try {
      for (AutoCloseable c : deferredAutoCloseable) {
        try {
          logger.atTrace().log("closing {}", c);

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
