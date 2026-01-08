package bx.sql.duckdb;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import javax.sql.DataSource;
import org.assertj.core.api.Assertions;
import org.duckdb.DuckDBConnection;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DuckDataSourceTest {

  @Test
  public void testCloseDataSource() throws SQLException {
    DuckDataSource ds = DuckDataSource.createInMemory();

    Connection c = ds.getConnection();

    Assertions.assertThat(c.isClosed()).isFalse();

    ds.close();
    ds.close();

    try {
      ds.getConnection();
      Assertions.failBecauseExceptionWasNotThrown(SQLException.class);
    } catch (SQLException e) {
      Assertions.assertThat(e.getMessage().toLowerCase()).contains("is closed");
    }
  }

  @Test
  public void testCloseIsolation() throws SQLException {

    DuckDataSource ds = DuckDataSource.createInMemory();

    Connection c0 = ds.getConnection();
    Connection c1 = ds.getConnection();

    Assertions.assertThat(c0).isNotSameAs(c1);

    Assertions.assertThat(c0).isInstanceOf(DuckDBConnection.class);

    Assertions.assertThat(c0.isClosed()).isFalse();
    Assertions.assertThat(c1.isClosed()).isFalse();

    c0.close();

    Assertions.assertThat(c0.isClosed()).isTrue();

    Assertions.assertThat(c1.isClosed()).isFalse();

    c1.close();
  }

  @Test
  public void testSharedSchema() throws SQLException {
    DataSource ds = DuckDataSource.createInMemory();

    Connection c0 = ds.getConnection();

    Connection c1 = ds.getConnection();

    Statement st = c1.createStatement();
    st.execute("create table dog (name varchar)");
    st.close();

    {
      PreparedStatement ps = c1.prepareStatement("select * from dog");
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        // do nothing;
      }
      rs.close();
      ps.close();
    }
    {
      PreparedStatement ps = c0.prepareStatement("select * from dog");
      ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        // do nothing;
      }
      rs.close();
      ps.close();
    }
    DuckTable table = DuckTable.of(ds, "dog");
    Assertions.assertThat(table.exists());
  }

  @Test
  public void testExtractUrlInMemory() throws SQLException {
    DuckDataSource dds = DuckDataSource.createInMemory();

    Assertions.assertThat(dds.urlSupplier.get()).isEqualTo("jdbc:duckdb:");
    Assertions.assertThat(dds.toString()).isEqualTo("DuckDataSource{url=jdbc:duckdb:}");

    dds.close();
  }

  @Test
  public void testExtractUrlViaConnection() throws SQLException, IOException {

    File ddb = new File(java.nio.file.Files.createTempDirectory("tmp").toFile(), "test.duckdb");

    Connection c = DriverManager.getConnection("jdbc:duckdb:" + ddb.getAbsolutePath());

    DuckDataSource dds = DuckDataSource.create(c);

    Assertions.assertThat(dds.urlSupplier.get()).isEqualTo("jdbc:duckdb:" + ddb.getAbsolutePath());
    Assertions.assertThat(dds.toString())
        .isEqualTo("DuckDataSource{url=jdbc:duckdb:" + ddb.getAbsolutePath() + "}");

    dds.close();

    Assertions.assertThat(c.isClosed()).isTrue();
  }

  @Test
  public void testMemoryLimit() throws SQLException {

    DuckDataSource ds = DuckDataSource.create("jdbc:duckdb:;memory_limit=500MiB");

    Assertions.assertThat(
            JdbcClient.create(ds)
                .sql("SELECT current_setting('memory_limit') AS memlimit")
                .query(String.class)
                .single())
        .isEqualTo("500.0 MiB");

    ds.close();
  }

  @Test
  public void testThreads() throws SQLException {

    DuckDataSource ds = DuckDataSource.create("jdbc:duckdb:;threads=7");

    Assertions.assertThat(
            JdbcClient.create(ds)
                .sql("SELECT current_setting('threads') AS memlimit")
                .query(String.class)
                .single())
        .isEqualTo("7");

    ds.close();
  }
}
