package bx.util;

import bx.sql.ConsoleQuery;
import bx.sql.Db;
import bx.sql.duckdb.DuckCsvImport;
import bx.sql.duckdb.DuckDataSource;
import bx.sql.duckdb.DuckTable;
import com.google.common.base.Stopwatch;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BxTest {

  Logger logger = LoggerFactory.getLogger(BxTest.class);

  Defer defer = Defer.create().withSwallow(true);
  Db testDb;

  Db h2Db;

  public Db getH2Db() {

    if (h2Db == null) {

      h2Db = Db.create("jdbc:h2:mem:test");
      // DO NOT use DEFER

    }
    return h2Db;
  }

  protected void defer(Object c) {
    defer.register(c);
  }

  public DuckTable loadAdsbTable(String name) {

    Stopwatch sw = Stopwatch.createStarted();
    try {
      return DuckCsvImport.using(getDataSource())
          .table(name)
          .from(new File("./src/test/resources/adsb.csv"))
          .load();
    } finally {
      logger.atTrace().log("load adsb took {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
    }
  }

  public DataSource getDataSource() {
    return db().getDataSource();
  }

  public Db db() {
    if (testDb == null) {

      Stopwatch sw = Stopwatch.createStarted();
      DataSource ds = DuckDataSource.createInMemory();
      logger.atTrace().log("create db took {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
      testDb = Db.create(ds);
      defer((AutoCloseable) ds);
    }
    return testDb;
  }

  @BeforeEach
  final void setup() {
    Db.reset(db());
  }

  public File createTempDir() {
    try {
      return java.nio.file.Files.createTempDirectory("temp").toFile();
    } catch (IOException e) {
      throw new BxException(e);
    }
  }

  ConsoleQuery consoleQuery() {
    return ConsoleQuery.with(getDataSource()).out(LoggerFactory.getLogger(getClass()));
  }

  @AfterEach
  final void bxTestCleanup() {

    testDb = null;

    defer.close();

    // we don't want to close the DB as we do with duckdb.
    // we'll just nuke all the objects between test methods;
    if (h2Db != null) {

      h2Db.getJdbcClient().sql("DROP ALL OBJECTS").update();
    }
  }

  @FunctionalInterface
  public static interface CodeBlock {

    public void run() throws Exception;
  }

  public void expect(Class<? extends Throwable> type, CodeBlock block) {
    try {
      block.run();
      Assertions.failBecauseExceptionWasNotThrown(type);
    } catch (Exception e) {
      Assertions.assertThat(e).isInstanceOf(type);
    }
  }
}
