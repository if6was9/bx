package bx.sql;

import bx.sql.duckdb.DuckTable;
import bx.util.BxTest;
import de.siegmar.fastcsv.writer.QuoteStrategies;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class CsvExportTest extends BxTest {

  @Test
  public void testAsResultSetExtractor() {
    DuckTable t = loadAdsbTable("adsb");

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    t.getJdbcClient()
        .sql("select flight,ac_reg as ac_reg from adsb limit 10")
        .query(
            new GenericCsvExport()
                .withConfig(c -> c.quoteStrategy(QuoteStrategies.ALWAYS))
                .to(baos)
                .newResultSetExtractor());

    System.out.println(new String(baos.toByteArray()));
  }

  @Test
  public void testFluent() {
    loadAdsbTable("adsb");

    String output =
        GenericCsvExport.from(getDataSource())
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

    GenericCsvExport.from(getDataSource())
        .to(new File("./target/test.csv"))
        .sql(
            c ->
                c.sql("select flight,ac_reg from adsb where flight=:flight")
                    .param("flight", "SWA3880"))
        .export();
  }

  @Test
  public void testGzip() throws IOException {
    DuckTable t = loadAdsbTable("adsb");

    File f = Files.createTempFile("temp", ".csv.gz").toFile();
    t.getJdbcClient()
        .sql("select flight,ac_reg as ac_reg from adsb limit 10")
        .query(
            new GenericCsvExport()
                .withConfig(c -> c.quoteStrategy(QuoteStrategies.ALWAYS))
                .to(f)
                .newResultSetExtractor());

    System.out.println(f);
  }

  @Test
  public void testVariants() throws IOException {
    DuckTable t = loadAdsbTable("adsb");

    String csv1 =
        GenericCsvExport.from(t.getDataSource())
            .sql("select flight,ac_reg from adsb order by flight limit 5")
            .exportToString();
    String csv2 =
        GenericCsvExport.from(t.getDataSource())
            .sql(c -> c.sql("select flight,ac_reg from adsb order by flight limit 5"))
            .exportToString();
    String csv3 =
        GenericCsvExport.from(t.getDataSource())
            .sql("select flight,ac_reg from adsb order by flight limit 5", null)
            .exportToString();
    String csv4 =
        GenericCsvExport.from(t.getDataSource())
            .sql(
                "select flight,ac_reg from adsb order by flight limit :limit",
                c -> c.param("limit", 5))
            .exportToString();
    String csv5 =
        GenericCsvExport.from(t.getDataSource())
            .sql(
                c ->
                    c.sql("select flight,ac_reg from adsb order by flight limit :limit")
                        .param("limit", 5))
            .exportToString();

    Assertions.assertThat(csv1).isEqualTo(csv2);
    Assertions.assertThat(csv1).isEqualTo(csv3);
    Assertions.assertThat(csv1).isEqualTo(csv4);
    Assertions.assertThat(csv1).isEqualTo(csv5);
  }
}
