package bx.sql.duckdb;

import bx.util.BxTest;
import com.google.common.io.CharSource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DuckCsvTest extends BxTest {

  @Test
  public void testImport() {

    String csv =
        """
        a,b,c
        1,2,3
        4,5,6
        7,8,9
        """;

    DuckTable t = DuckCsv.using(dataSource()).fromString(csv).load();

    t.show();
    Assertions.assertThat(t.rowCount()).isEqualTo(3);
    Assertions.assertThat(
            t.sql("select c from " + t.getName() + " where a=7").query().singleValue())
        .isEqualTo(9L);
  }

  @Test
  public void testImportTempTable() {

    String csv =
        """
        a,b,c
        1,2,3
        4,5,6
        7,8,9
        """;

    DuckTable t = DuckCsv.using(dataSource()).fromString(csv).load();

    t.show();
    Assertions.assertThat(t.rowCount()).isEqualTo(3);
    Assertions.assertThat(
            t.sql("select c from " + t.getName() + " where a=7").query().singleValue())
        .isEqualTo(9L);
    Assertions.assertThat(t.getName()).startsWith("temp");
  }

  @Test
  public void testImportNamedTable() {

    String csv =
        """
        a,b,c
        1,2,3
        4,5,6
        7,8,9
        """;

    DuckTable t = DuckCsv.using(dataSource()).fromString(csv).table("foo").load();

    t.show();
    Assertions.assertThat(t.rowCount()).isEqualTo(3);
    Assertions.assertThat(
            t.sql("select c from " + t.getName() + " where a=7").query().singleValue())
        .isEqualTo(9L);
    Assertions.assertThat(t.getName()).startsWith("foo");
  }

  @Test
  public void test() throws IOException {

    var jdbc = JdbcClient.create(dataSource());
    jdbc.sql("create table test (name varchar(10),age int)").update();
    jdbc.sql("insert into test (name,age) values ('Homer',8)").update();
    jdbc.sql("insert into test (name,age) values ('Rosie',3)").update();

    String out = DuckCsv.using(dataSource()).table("test").exportString();

    var lines = CharSource.wrap(out).readLines();

    Assertions.assertThat(lines.get(0)).isEqualTo("name,age");
    Assertions.assertThat(lines.get(1)).isEqualTo("Homer,8");
    Assertions.assertThat(lines.get(2)).isEqualTo("Rosie,3");

    out =
        DuckCsv.using(dataSource())
            .sql("select * from test where age<:age", st -> st.param("age", 5))
            .exportString();

    lines = CharSource.wrap(out).readLines();

    Assertions.assertThat(lines.size()).isEqualTo(2);
    Assertions.assertThat(lines.get(0)).isEqualTo("name,age");
    Assertions.assertThat(lines.get(1)).isEqualTo("Rosie,3");
  }

  public static void expect(Class<? extends Throwable> clazz, CheckBlock r) {
    try {
      r.run();
      Assertions.failBecauseExceptionWasNotThrown(clazz);
    } catch (Throwable t) {

      Assertions.assertThat(clazz.isAssignableFrom(t.getClass()))
          .withFailMessage("expected %s but got %s -- %s", clazz, t.getClass(), t.toString())
          .isTrue();
    }
  }

  static interface CheckBlock {

    void run() throws Exception;
  }

  @Test
  public void testInvalidUsage() {
    expect(
        NullPointerException.class,
        () -> {
          DuckCsv.using((DataSource) null);
        });
    expect(
        NullPointerException.class,
        () -> {
          DuckCsv.using((DuckTable) null);
        });
    expect(
        NullPointerException.class,
        () -> {
          new DuckCsv(null);
        });

    expect(
        IllegalStateException.class,
        () -> {
          DuckCsv.using(dataSource()).export();
        });

    expect(
        IllegalStateException.class,
        () -> {
          File f = Files.createTempFile("temp", ".csv").toFile();
          DuckCsv.using(dataSource()).to(f).export();
        });
    expect(
        IllegalStateException.class,
        () -> {
          DuckCsv.using(dataSource()).load();
        });
  }
}
