package bx.sql;

import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;

public class BxJdbcClient implements JdbcClient {

  JdbcClient client;
  String table;

  public static JdbcClient create(DataSource ds, String table) {
    JdbcClient wrapped = JdbcClient.create(ds);

    BxJdbcClient client = new BxJdbcClient();
    client.client = wrapped;
    client.table = table;
    return client;
  }

  public static JdbcClient create(DataSource ds) {
    return create(ds, null);
  }

  @Override
  public StatementSpec sql(String sql) {
    if (table != null) {
      sql = SqlUtil.interpolateTable(sql, table);
    }
    return client.sql(sql);
  }
}
