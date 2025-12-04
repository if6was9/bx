package bx.sql;

import bx.sql.mapper.ObjectNodeRowMapper;
import bx.util.BxTest;
import java.sql.SQLException;
import org.junit.jupiter.api.Test;

public class SpringJdbcTest extends BxTest {

  @Test
  public void testAdsb() {
    loadAdsbTable("adsb");

    db().getJdbcClient()
        .sql("select * from adsb limit 5")
        .query(new ObjectNodeRowMapper())
        .list()
        .forEach(
            it -> {
              System.out.println(it);
            });
  }

  @Test
  public void testIt() throws SQLException {

    var client = db().getJdbcClient();

    client.sql("create table test as (select * from 'src/test/resources/adsb.csv')").update();

    var rs = client.sql("select flight,gs from test").query(new ResultSetTextFormatter());

    System.out.println(rs);
  }
}
