package bx.sql;

import bx.util.BxException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.spi.LoggingEventBuilder;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class PrettyQuery {

  JdbcClient jdbc;

  String tableName;

  private PrettyQuery() {
    super();
  }

  public static PrettyQuery with(DataSource ds) {
    return with(JdbcClient.create(ds));
  }

  public static PrettyQuery with(JdbcClient c) {

    var q = new PrettyQuery();
    q.jdbc = c;

    return q;
  }

  public PrettyQuery table(String name) {
    this.tableName = name;
    return this;
  }

  public String select(String sql) {

    final String s = sql;
    return select(c -> c.sql(s));
  }

  public String select(Function<JdbcClient, StatementSpec> specFunction) {

    var spec = specFunction.apply(jdbc);

    return spec.query(new ResultSetTextFormatter());
  }

  public void select(String sql, LoggingEventBuilder out) {
    select(c -> c.sql(sql), out);
  }

  public void select(Function<JdbcClient, StatementSpec> specFunction, LoggingEventBuilder api) {

    var spec = specFunction.apply(jdbc);

    String output = spec.query(new ResultSetTextFormatter());

    api.log("query{}{}", System.lineSeparator(), output);
  }

  public void select(String sql, OutputStream out) {
    select(c -> c.sql(sql), out);
  }

  public void select(Function<JdbcClient, StatementSpec> specFunction, OutputStream out) {

    try {
      var spec = specFunction.apply(jdbc);

      String s = spec.query(new ResultSetTextFormatter());

      out.write(s.getBytes());
      out.write(System.lineSeparator().getBytes());
    } catch (IOException e) {
      throw new BxException(e);
    }
  }
}
