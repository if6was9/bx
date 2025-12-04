package bx.sql.duckdb;

import bx.util.BxTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DuckDbTest extends BxTest {

  @Test
  public void testIt() throws Exception {

    var t = DuckTable.of(db().getDataSource(), "test");

    Assertions.assertThat(t.exists()).isFalse();

    db().getJdbcClient().sql("create table test (abc int)").update();

    Assertions.assertThat(t.exists()).isTrue();

    Assertions.assertThat(t.rowCount()).isEqualTo(0);
  }
}
