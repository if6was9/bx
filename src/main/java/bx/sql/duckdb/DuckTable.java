package bx.sql.duckdb;

import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;

public class DuckTable {

  DataSource dataSource;
  String table;
  JdbcClient client;

  public static DuckTable create(DataSource ds, String name) {

    DuckTable table = new DuckTable();
    table.table = name;
    table.dataSource = ds;

    return table;
  }

  public JdbcClient getJdbcClient() {
    if (client == null) {
      client = JdbcClient.create(getDataSource());
    }
    return client;
  }

  public String getTableName() {
    return table;
  }

  public DataSource getDataSource() {
    return dataSource;
  }
}
