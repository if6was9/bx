package bx.sql;

import bx.util.BxTest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlCloserTest extends BxTest {

  @Test
  public void testIt() throws SQLException {

    Connection c = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    Results results = null;
    ResultSet rs2 = null;
    try (SqlCloser closer = SqlCloser.create()) {
      c = db().getDataSource().getConnection();
      Assertions.assertThat(c.isClosed()).isFalse();
      closer.register(c);
      ps = c.prepareStatement("create table test (abc int)");
      ps.executeUpdate();
      Assertions.assertThat(ps.isClosed()).isFalse();
      closer.register(ps);

      PreparedStatement ps2 = c.prepareStatement("select * from test");
      closer.register(ps2);
      rs = ps2.executeQuery();
      closer.register(rs);
      while (rs.next()) {
        //
      }

      rs2 = ps2.executeQuery();

      results = Results.create(rs2);
      closer.register(results);
      while (results.next()) {
        //
      }
    }

    Assertions.assertThat(c.isClosed()).isTrue();
    Assertions.assertThat(ps.isClosed()).isTrue();
    Assertions.assertThat(rs.isClosed()).isTrue();
    Assertions.assertThat(rs2.isClosed()).isTrue();
    Assertions.assertThat(results.isClosed()).isTrue();
  }
}
