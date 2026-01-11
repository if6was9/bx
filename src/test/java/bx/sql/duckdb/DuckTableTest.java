package bx.sql.duckdb;

import bx.util.BxTest;
import bx.util.RateCounter;
import com.google.common.base.Stopwatch;
import java.io.IOException;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.duckdb.DuckDBAppender;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DuckTableTest extends BxTest {
  static Logger logger = bx.util.Slogger.forEnclosingClass();

  @Test
  public void testExists() {

    var t = DuckTable.of(db().getDataSource(), "test");

    Assertions.assertThat(t.exists()).isFalse();

    db().getJdbcClient().sql("create table test (abc int)").update();

    Assertions.assertThat(t.exists()).isTrue();

    // Assertions.assertThat(t.rowCount()).isEqualTo(0);

  }

  @Test
  public void testRowCount() {

    Stopwatch sw = Stopwatch.createStarted();
    loadAdsbTable("adsb");

    logger.atInfo().log("load adsb {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
    var t = DuckTable.of(db().getDataSource(), "adsb");

    Assertions.assertThat(t.exists()).isTrue();

    Assertions.assertThat(t.rowCount()).isEqualTo(1000L);

    t = DuckTable.of(db().getDataSource(), "foo");
    logger.atInfo().log("complete {}ms", sw.elapsed(TimeUnit.MILLISECONDS));
  }

  @Test
  public void testCreateTable() {
    DuckTable t = loadAdsbTable("adsb");

    String sql = t.getCreateTableSql();
    System.out.println(sql);
    Assertions.assertThat(sql).contains("true_heading DOUBLE");
  }

  @Test
  public void testGetTableNames() {
    var t1 = loadAdsbTable("t1");
    var t2 = loadAdsbTable("t2");
  }

  @Test
  public void testRenameTable() {
    var t = loadAdsbTable("adsb");

    var t2 = t.renameTable("foo");

    Assertions.assertThat(t.getTableName()).isEqualTo("adsb");
    Assertions.assertThat(t2.getTableName()).isEqualTo("foo");
  }

  @Test
  public void testRenameColumn() {
    var t = loadAdsbTable("adsb");

    t.renameColumn("id", "idx");

    Assertions.assertThat(t.getColumnNames()).startsWith("idx", "hex", "flight");
  }

  @Test
  public void testHasColumn() {
    var t = loadAdsbTable("adsb");

    Assertions.assertThat(t.hasColumn("id")).isTrue();
    Assertions.assertThat(t.hasColumn("ID")).isTrue();
    Assertions.assertThat(t.hasColumn("nope")).isFalse();
  }

  @Test
  public void testGetColumnNames() {
    var t = loadAdsbTable("adsb");

    var cols = t.getColumnNames();

    Assertions.assertThat(cols).startsWith("id", "hex", "flight");
  }

  @Test
  public void testDropColumn() {
    var t = loadAdsbTable("adsb");

    var cols = t.getColumnNames();

    Assertions.assertThat(cols).startsWith("id", "hex", "flight");

    t.dropColumn("hex");

    Assertions.assertThat(t.getColumnNames()).startsWith("id", "flight");

    t.dropColumn("hex");
  }

  @Test
  public void testDropColumnsExcept() {
    var t = loadAdsbTable("adsb");
    t.dropColumnsExcept("FLIGHT", "LAT", "lon");

    var names = t.getColumnNames();

    Assertions.assertThat(names).containsExactly("flight", "lat", "lon");
  }

  @Test
  public void testDropColumns() {
    var t = loadAdsbTable("adsb");
    t.dropColumns("FLIGHT", "LAT", "lon", "flight");

    var names = t.getColumnNames();

    Assertions.assertThat(names).doesNotContain("flight", "lat", "lon");
  }

  @Test
  public void testDeleteRow() {
    var t = loadAdsbTable("adsb");

    Assertions.assertThat(t.rowCount()).isEqualTo(1000);

    t.deleteRow(10);

    Assertions.assertThat(t.rowCount()).isEqualTo(999);
  }

  @Test
  public void testUpdateRow() {
    var t = loadAdsbTable("adsb");

    t.update(2, "flight", "XXX");

    Object val =
        t.getJdbcClient().sql("select flight from adsb where rowid=2").query().singleValue();

    Assertions.assertThat(val).isEqualTo("XXX");
  }

  @Test
  public void testPrettySelect() {
    var t = loadAdsbTable("adsb");

    t.prettyQuery().select();
  }

  @Test
  public void testAddColumn() {
    var t = loadAdsbTable("adsb");

    Assertions.assertThat(t.getColumnNames()).doesNotContain("foo");

    t.addColumn("foo int");

    Assertions.assertThat(t.getColumnNames()).contains("foo");
  }

  @Test
  public void testRetainColumns() {
    var t = loadAdsbTable("adsb");

    t.dropColumnsExcept("id", "flight");
  }

  @Test
  public void testDrop() {
    loadAdsbTable("adsb");

    var t = DuckTable.of(db().getDataSource(), "adsb");

    Assertions.assertThat(t.exists()).isTrue();

    t.drop();
    Assertions.assertThat(t.exists()).isFalse();

    // test idempotency

    t.drop();
  }

  @Test
  public void testCreateJdbcClient() {

    // verify that JdbcClient.create() is a low-cost operation
    // this "test" is more for my understanding than a test per se

    var c = RateCounter.create();
    for (int i = 0; i < 10000; i++) {
      JdbcClient.create(dataSource());
      c.increment();
    }
    c.log();
    Assertions.assertThat(c.getRate()).isGreaterThan(10000d);
  }

  @Test
  public void testCreateAppender() throws SQLException {

    var t = DuckTable.of(db().getDataSource(), "test");

    t.getJdbcClient().sql("create table test (name varchar(20), age int)").update();

    DuckDBAppender x = t.createAppender();

    x.beginRow();
    x.append("Homer");
    x.append(8);
    x.endRow();
    x.beginRow();
    x.append("Rosie");
    x.append(3);
    x.endRow();

    x.close();

    t.prettyQuery().select();
  }

  @Test
  public void testPrettySelectToStream() {
    var t = loadAdsbTable("adsb");

    t.prettyQuery().select();
  }

  @Test
  public void testPrettySelectWihtLog() {
    var t = loadAdsbTable("adsb");
    t.prettyQuery().select();

    t.prettyQuery()
        .to(LoggerFactory.getLogger(getClass()))
        .select(c -> c.sql("select * from adsb where flight=:flight").param("flight", "N915CM"));
  }

  @Test
  public void testCreateFromCsv() throws IOException {
    String csv =
        """
        isbn,name,author
        039309670X,"Moby Dick",Herman Melville
        9368343039,As I Lay Dying,William Faulkner
        0140441182,Thus Spoke Zarathustra,Friedrich Nietzsche
        """;

    var t = DuckTable.of(dataSource(), "book").csvImport().fromString(csv).table("book").load();

    Assertions.assertThat(t.getName()).isEqualTo("book");
    t.show();
  }
}
