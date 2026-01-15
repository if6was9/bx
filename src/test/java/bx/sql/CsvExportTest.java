package bx.sql;

import bx.sql.duckdb.DuckTable;
import bx.util.BxTest;
import de.siegmar.fastcsv.writer.QuoteStrategies;
import java.io.StringWriter;
import org.junit.jupiter.api.Test;

public class CsvExportTest extends BxTest {

  @Test
  public void testAsResultSetExtractor() {
    DuckTable t = loadAdsbTable("adsb");

    StringWriter sw = new StringWriter();
    t.getJdbcClient()
        .sql("select flight,ac_reg as ac_reg from adsb limit 10")
        .query(new CsvExport().withConfig(c -> c.quoteStrategy(QuoteStrategies.ALWAYS)).to(sw));

    System.out.println(sw.toString());
  }

  @Test
  public void testFluent() {
    loadAdsbTable("adsb");

    StringWriter sw = new StringWriter();

    CsvExport.from(dataSource())
        .to(sw)
        .sql(c -> c.sql("select * as xx from adsb limit :limit").param("limit", 10));

    System.out.println(sw.toString());
  }
}
