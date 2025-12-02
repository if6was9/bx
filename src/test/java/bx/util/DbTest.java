package bx.util;

import bx.sql.Db;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DbTest extends BxTest {

  @Test
  public void testIt() {

    Db db = db();

    Assertions.assertThat(db()).isSameAs(db);
    Assertions.assertThat(Db.get()).isSameAs(db);

    Assertions.assertThat(db.getDataSource()).isSameAs(db().getDataSource());
  }

  @Test
  public void testIt2() {

    var m = Map.of("DB_JDBC_URL", "jdbc:foo:bar");

    Config cfg = new Config(m);
    Db.toHikariConfig(cfg);
  }
}
