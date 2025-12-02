package bx.sql;

import bx.sql.mapper.ObjectNodeRowMapper;
import bx.util.BxTest;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SpringJdbcTest extends BxTest {

  @Test
  public void testDataSourceIsSame() throws SQLException {

    var c0 = testDataSource();
    var c1 = testDataSource();

    Assertions.assertThat(c1).isSameAs(c0);
  }

  @Test
  public void testAdsb() {
    loadAdsbTable("adsb");

    testJdbcClient()
        .sql("select * from adsb")
        .query(new ObjectNodeRowMapper())
        .list()
        .forEach(
            it -> {
              System.out.println(it);
            });
  }

  @Test
  public void testIt() throws SQLException {

    var client = testJdbcClient();

    client.sql("create table test as (select * from 'src/test/resources/adsb.csv')").update();

    var rs = client.sql("select flight,gs from test").query(new ResultSetTextFormatter());

    System.out.println(rs);
  }
}
