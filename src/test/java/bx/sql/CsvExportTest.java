package bx.sql;

import bx.sql.duckdb.DuckTable;
import bx.util.BxTest;
import de.siegmar.fastcsv.writer.QuoteStrategies;
import java.io.File;
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

    String output =
        CsvExport.from(dataSource())
            .sql(
                c ->
                    c.sql("select flight,ac_reg from adsb where flight=:flight")
                        .param("flight", "SWA3880"))
            .exportToString();

    System.out.println(output);
  }

  @Test
  public void testExportToFile() {
    loadAdsbTable("adsb");

    CsvExport.from(dataSource())
        .to(new File("./target/test.csv"))
        .sql(
            c ->
                c.sql("select flight,ac_reg from adsb where flight=:flight")
                    .param("flight", "SWA3880"))
        .export();
  }
}
