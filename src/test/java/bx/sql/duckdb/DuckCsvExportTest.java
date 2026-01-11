package bx.sql.duckdb;

import bx.util.BxTest;
import bx.util.Slogger;
import com.google.common.io.CharSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class DuckCsvExportTest extends BxTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testIt() throws IOException {

    DuckTable t = loadAdsbTable("adsb");

    File outputFile = new File(createTempDir(), "out.csv");

    new DuckCsvExport(t).to(outputFile).export();

    List<String> lines =
        com.google.common.io.Files.asCharSource(outputFile, StandardCharsets.UTF_8).readLines();

    lines.forEach(
        it -> {
          System.out.println(it);
        });

    Assertions.assertThat(lines.size()).isGreaterThan(1000);
  }

  @Test
  public void testItx() throws IOException {

    DuckTable t = loadAdsbTable("adsb");

    File outputFile = new File(createTempDir(), "out.csv");

    new DuckCsvExport(t)
        .select("select * from {{table}} where ac_type=:ac_type", st -> st.param("ac_type", "C172"))
        .to(outputFile)
        .export();

    List<String> lines =
        com.google.common.io.Files.asCharSource(outputFile, StandardCharsets.UTF_8).readLines();

    Assertions.assertThat(lines.size()).isEqualTo(161);
  }

  @Test
  public void testExportWithDataSource() throws IOException {

    DuckTable t = loadAdsbTable("adsb");

    File outputFile = new File(createTempDir(), "out.csv");

    new DuckCsvExport(dataSource())
        .select("select * from adsb where ac_type=:ac_type", st -> st.param("ac_type", "C172"))
        .to(outputFile)
        .export();

    List<String> lines =
        com.google.common.io.Files.asCharSource(outputFile, StandardCharsets.UTF_8).readLines();

    Assertions.assertThat(lines.size()).isEqualTo(161);
  }

  @Test
  public void testS3Export() {

    DuckTable t = loadAdsbTable("adsb");

    String bucket = "test.bitquant.cloud";

    DuckS3Extension.load(dataSource()).useCredentialChain();

    new DuckCsvExport(dataSource())
        .select("select * from adsb where ac_type=:ac_type", st -> st.param("ac_type", "C172"))
        .toS3(bucket, "temp/out.csv")
        .export();

    // Assertions.assertThat(lines.size()).isEqualTo(161);
  }

  @Test
  public void testExport() throws IOException {

    var t = loadAdsbTable("adsb");

    String s =
        t.csvExport()
            .select(
                "select flight,ac_type from {{table}} where ac_type=:type order by flight",
                c -> c.param("type", "B789"))
            .exportAsString();

    logger.atInfo().log("csv export: \n{}", s);
    List<String> lines = CharSource.wrap(s).readLines();

    Assertions.assertThat(lines.get(0)).isEqualTo("flight,ac_type");

    Assertions.assertThat(lines.get(1)).isEqualTo("CHH7926,B789");
    Assertions.assertThat(lines.get(2)).isEqualTo("CHH7926,B789");
    Assertions.assertThat(lines.get(3)).isEqualTo("JAL062,B789");
    Assertions.assertThat(lines.get(4)).isEqualTo("UAL153,B789");
  }
}
