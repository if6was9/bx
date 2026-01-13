package bx.sql.duckdb;

import bx.sql.ConsoleQuery;
import bx.util.BxTest;
import bx.util.Slogger;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class ConsoleQueryTest extends BxTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void test() {
    loadAdsbTable("adsb");

    ConsoleQuery.with(dataSource()).table("adsb").show();
    ConsoleQuery.with(dataSource())
        .select(c -> c.sql("select * from adsb limit :limit").param("limit", 5));
  }

  @Test
  public void testLoggingShouldNotThrowExceptions() {

    ConsoleQuery.with(dataSource()).stdout().select(c -> c.sql("select * from does_not_exist"));
  }

  @Test
  public void testIt() {
    var client = getH2Db().getInstance().getJdbcClient();

    client.sql("create table test (name char(20), age int)").update();
    client.sql("insert into test (name, age) values ('homer',8)").update();

    ConsoleQuery.with(client).table("test").show();
  }
}
