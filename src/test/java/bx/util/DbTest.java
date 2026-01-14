package bx.util;

import bx.sql.Db;
import bx.sql.duckdb.DuckDataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;
import org.assertj.core.api.Assertions;

import org.junit.jupiter.api.Test;

import com.google.common.collect.Lists;

public class DbTest extends BxTest {

  java.util.List<Db> dbList = Lists.newArrayList();

  @Test
  public void testSame1() {

    Assertions.assertThat(db()).isSameAs(db());
    Assertions.assertThat(Db.getInstance()).isSameAs(db());
    Assertions.assertThat(db().getDataSource()).isSameAs(db().getDataSource());
    dbList.add(db());
  }

  @Test
  public void testSame2() {

    Assertions.assertThat(db()).isSameAs(db());
    Assertions.assertThat(Db.getInstance()).isSameAs(db());
    Assertions.assertThat(db().getDataSource()).isSameAs(db().getDataSource());
    dbList.add(db());
  }

  @Test
  public void testIt() {

    Db db = db();

    Assertions.assertThat(db()).isSameAs(db);
    Assertions.assertThat(Db.getInstance()).isSameAs(db);

    Assertions.assertThat(db.getDataSource()).isSameAs(db().getDataSource());
  }

  @Test
  public void testIt3() {

    Db db = db();

    Assertions.assertThat(db()).isSameAs(db);
    Assertions.assertThat(Db.getInstance()).isSameAs(db);

    Assertions.assertThat(db.getDataSource()).isSameAs(db().getDataSource());
  }

  public void testIt2() {

    var m = Map.of("DB_JDBC_URL", "jdbc:foo:bar");

    Config cfg = Config.just(m);
    Db.toHikariConfig(cfg);
  }

  @Test
  public void testX() throws Exception {
    DuckDataSource ds = DuckDataSource.createInMemory();
    AutoCloseable ac = (AutoCloseable) ds;

    Connection keep = ds.getConnection();
    ac.close();
    ac.close(); // test idempotency

    Assertions.assertThat(keep.isClosed())
        .isFalse(); // we can't actually close connections that have been opened
    try {
      Connection c = ds.getConnection();
      Assertions.failBecauseExceptionWasNotThrown(SQLException.class);
    } catch (SQLException expected) {
      // Ok
    }
  }

  @Test
  public void testLoadAdsb() {
    loadAdsbTable("a");
    loadAdsbTable("b");
  }

  @Test
  public void testDuckDb() {

    Db db = Db.create("jdbc:duckdb:");
    defer(db);
    db.getJdbcClient().sql("select 1").query().optionalValue();
  }
}
