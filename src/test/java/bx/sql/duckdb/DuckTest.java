package bx.sql.duckdb;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.duckdb.DuckDBConnection;
import org.junit.jupiter.api.Test;

public class DuckTest {

  @Test
  public void testIt() throws SQLException {
    Connection c = DriverManager.getConnection("jdbc:duckdb:");

    DuckDBConnection dc = (DuckDBConnection) c;

    DuckDBConnection dc2 = dc.duplicate();

    Assertions.assertThat(dc2).isNotSameAs(dc);
    Assertions.assertThat(dc2.isClosed()).isFalse();
    Assertions.assertThat(dc.isClosed()).isFalse();

    dc2.close();
    Assertions.assertThat(dc2.isClosed()).isTrue();

    Assertions.assertThat(dc.isClosed()).isFalse();
  }
}
