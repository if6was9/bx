package bx.sql.duckdb;

import bx.sql.PrettyQuery;
import bx.util.BxTest;
import bx.util.Slogger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class PrettyQueryTest extends BxTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void test() {
    loadAdsbTable("adsb");

    PrettyQuery.with(dataSource()).table("adsb").show();
    PrettyQuery.with(dataSource()).select("select * from adsb limit 5");
  }

  @Test
  public void testLoggingShouldNotThrowExceptions() {

    PrettyQuery.with(dataSource()).stdout().select("select * from does_not_exist");
  }
}
