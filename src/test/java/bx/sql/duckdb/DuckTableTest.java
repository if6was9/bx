package bx.sql.duckdb;

import bx.util.BxTest;
import com.google.common.flogger.FluentLogger;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.duckdb.DuckDBAppender;
import org.junit.jupiter.api.Test;

public class DuckTableTest extends BxTest {
  FluentLogger logger = FluentLogger.forEnclosingClass();

  @Test
  public void testExists() {

    var t = DuckTable.of(db().getDataSource(), "test");

    Assertions.assertThat(t.exists()).isFalse();

    db().getJdbcClient().sql("create table test (abc int)").update();

    Assertions.assertThat(t.exists()).isTrue();

    //	Assertions.assertThat(t.rowCount()).isEqualTo(0);

  }

  @Test
  public void testRowCount() {

    loadAdsbTable("adsb");

    var t = DuckTable.of(db().getDataSource(), "adsb");

    Assertions.assertThat(t.exists()).isTrue();

    Assertions.assertThat(t.rowCount()).isEqualTo(1000L);

    t = DuckTable.of(db().getDataSource(), "foo");
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
    t.selectPretty("select rowid,flight from adsb", System.out);
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

    t.selectPretty(logger.atInfo());
  }

  @Test
  public void testPrettySelectToStream() {
    var t = loadAdsbTable("adsb");

    t.selectPretty(System.out);
  }

  @Test
  public void testPrettySelectWihtLog() {
    var t = loadAdsbTable("adsb");
    t.selectPretty(logger.atInfo());

    t.selectPretty(
        c -> c.sql("select * from adsb where flight=:flight").param("flight", "N915CM"),
        logger.atInfo());
  }
}
