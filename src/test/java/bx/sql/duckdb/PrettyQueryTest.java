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
    PrettyQuery.with(dataSource())
        .select(c -> c.sql("select * from adsb limit :limit").param("limit", 5));
  }

  @Test
  public void testLoggingShouldNotThrowExceptions() {

    PrettyQuery.with(dataSource()).stdout().select(c -> c.sql("select * from does_not_exist"));
  }
  
  @Test
  public void testIt() {
	  var client = getH2Db().get().getJdbcClient();
	  
	  client.sql("create table test (name char(20), age int)").update();
	  client.sql("insert into test (name, age) values ('homer',8)").update();
	  
	  PrettyQuery.with(client).table("test").show();
	  
  }
}
