package bx.sql.duckdb;

import bx.sql.PrettyQuery;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Optional;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DuckAdmin {

  DataSource dataSource;

  public DuckAdmin(DataSource dataSource) {
    Preconditions.checkNotNull(dataSource);
    this.dataSource = dataSource;
  }

  public static DuckAdmin of(DataSource ds) {
    return new DuckAdmin(ds);
  }

  public JdbcClient getJdbcClient() {
    return JdbcClient.create(dataSource);
  }

  public void showSettings() {
    PrettyQuery.with(getJdbcClient()).select("select *  FROM duckdb_settings()");
  }

  public List<String> getTableNames() {

    return getJdbcClient()
        .sql("SELECT table_name FROM duckdb_tables order by name")
        .query(String.class)
        .list();
  }

  public Optional<String> getSetting(String name) {
    return getJdbcClient()
        .sql("select value from duckdb_settings() where name=:name")
        .param("name", name)
        .query(String.class)
        .optional();
  }

  public void modifySetting(String key, String val) {
    String sql = String.format("SET %s to :val", key);
    getJdbcClient().sql(sql).param("val", val).update();
  }

  public void modifySetting(String key, int val) {
    String sql = String.format("SET %s to :val", key);
    getJdbcClient().sql(sql).param("val", val).update();
  }

  public void checkpoint() {
    getJdbcClient().sql("CHECKPOINT").query(rs -> {});
  }
}
