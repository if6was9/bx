package bx.sql.duckdb;

import bx.util.BxTest;
import java.io.File;
import org.junit.jupiter.api.Test;

public class DuckCsvImportTest extends BxTest {

  @Test
  public void testIt() {

    DuckTable t =
        new DuckCsvImport(dataSource())
            .from(new File("./src/test/resources/adsb.csv"))
            .importData();

    t.show();
  }

  @Test
  public void testOrderBy() {

    DuckTable t =
        new DuckCsvImport(dataSource())
            .from(new File("./src/test/resources/adsb.csv"))
            .orderBy("ac_type")
            .importData();

    t.show();
  }
}
