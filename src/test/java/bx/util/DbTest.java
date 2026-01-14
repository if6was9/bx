package bx.util;

import bx.sql.Db;
import bx.sql.duckdb.DuckDataSource;
import com.google.common.collect.Lists;
import com.zaxxer.hikari.HikariConfig;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

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

  @Test
  public void testToHikariConfig() {

    // This is more direct testing than testToHikariConfigOld....migrate to this
    Assertions.assertThat(
            Db.toHikariConfig(Config.just(Map.of("DB_JDBC_URL", "jdbc:fizz:buzz"))).getJdbcUrl())
        .isEqualTo("jdbc:fizz:buzz");
    Assertions.assertThat(
            Db.toHikariConfig(Config.just(Map.of("DB_URL", "jdbc:fizz:buzz"))).getJdbcUrl())
        .isEqualTo("jdbc:fizz:buzz");
    Assertions.assertThat(
            Db.toHikariConfig(Config.just(Map.of("DB_MAX_LIFETIME", "6100"))).getMaxLifetime())
        .isEqualTo(6100);
  }

  @Test
  public void testToHikariConfigOld() {

    var m =
        Map.of(
            "DB_URL",
            "jdbc:foo:bar",
            "DB_USERNAME",
            "myuser",
            "DB_PASSWORD",
            "mypassword",
            "DB_CONNECTION_TIMEOUT",
            "250",
            "DB_IDLE_TIMEOUT",
            "260");

    m = new HashMap(m);

    m.put("DB_INITIALIZATION_FAIL_TIMEOUT", "270");
    m.put("DB_KEEPALIVE_TIME", "30000");
    m.put("DB_LEAK_DETECTION_THRESHOLD", "12");
    m.put("DB_MAXIMUM_POOL_SIZE", "9");
    m.put("DB_MAX_LIFETIME", "61000");
    m.put("DB_MINIMUM_IDLE", "3");
    m.put("DB_CATALOG", "foo");
    m.put("DB_VALIDATION_TIMEOUT", "295");
    m.put("DB_ALLOW_POOL_SUSPENSION", "true");
    Config cfg = Config.just(m);
    HikariConfig hk = Db.toHikariConfig(cfg);

    Assertions.assertThat(hk.getJdbcUrl()).isEqualTo("jdbc:foo:bar");
    Assertions.assertThat(hk.getUsername()).isEqualTo("myuser");
    Assertions.assertThat(hk.getPassword()).isEqualTo("mypassword");

    Assertions.assertThat(hk.getConnectionTimeout()).isEqualTo(250);
    Assertions.assertThat(hk.getIdleTimeout()).isEqualTo(260);
    Assertions.assertThat(hk.getInitializationFailTimeout()).isEqualTo(270);
    Assertions.assertThat(hk.getKeepaliveTime()).isEqualTo(30000);
    Assertions.assertThat(hk.getLeakDetectionThreshold()).isEqualTo(12);
    Assertions.assertThat(hk.getMaximumPoolSize()).isEqualTo(9);
    Assertions.assertThat(hk.getMaxLifetime()).isEqualTo(61000);
    Assertions.assertThat(hk.getMinimumIdle()).isEqualTo(3);
    Assertions.assertThat(hk.getValidationTimeout()).isEqualTo(295);
    Assertions.assertThat(hk.getCatalog()).isEqualTo("foo");
    Assertions.assertThat(hk.isAllowPoolSuspension()).isEqualTo(true);
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
