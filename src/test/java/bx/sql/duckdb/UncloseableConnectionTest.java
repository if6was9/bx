package bx.sql.duckdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UncloseableConnectionTest {

  @Test
  public void testIt() throws SQLException {

    Connection c = DriverManager.getConnection("jdbc:duckdb:");

    Assertions.assertThat(c.isClosed()).isFalse();

    var uc = new DuckConnectionWrapper(c);

    uc.close();

    Assertions.assertThat(uc.isClosed()).isFalse();

    c.close();

    Assertions.assertThat(c.isClosed()).isTrue();
    Assertions.assertThat(uc.isClosed()).isTrue();
  }
}
