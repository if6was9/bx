package bx.sql.duckdb;

import bx.util.BxTest;
import java.io.File;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DuckCsvImportTest extends BxTest {

  @Test
  public void testIt() {

    DuckTable t =
        new DuckCsvImport(getDataSource())
            .from(new File("./src/test/resources/adsb.csv"))
            .importTable();

    t.show();
  }

  @Test
  public void testOrderByWithoutOrderBy() {

    DuckTable t =
        new DuckCsvImport(getDataSource())
            .from(new File("./src/test/resources/adsb.csv"))
            .columns(List.of("flight", "id"))
            .orderBy("ac_type")
            .importTable();

    Assertions.assertThat(t.getColumnNames()).containsExactly("flight", "id");
    t.show();
  }

  @Test
  public void testOrderByWithOrderBy() {

    var t =
        new DuckCsvImport(getDataSource())
            .from(new File("./src/test/resources/adsb.csv"))
            .columns(List.of("flight", "id"))
            .orderBy("ORDER BY ac_type")
            .importTable();

    Assertions.assertThat(t.getColumnNames()).containsExactly("flight", "id");
    t.show();
  }
}
