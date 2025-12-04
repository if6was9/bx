package bx.sql;

import bx.util.BxException;
import com.google.common.flogger.FluentLogger.Api;
import java.io.IOException;
import java.io.OutputStream;
import java.util.function.Function;
import javax.sql.DataSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class PrettyQuery {

  JdbcClient jdbc;

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

  public String select(String sql) {

    return select(c -> c.sql(sql));
  }

  public String select(Function<JdbcClient, StatementSpec> specFunction) {

    var spec = specFunction.apply(jdbc);

    return spec.query(new ResultSetTextFormatter());
  }

  public void select(String sql, Api out) {
    select(c -> c.sql(sql), out);
  }

  public void select(Function<JdbcClient, StatementSpec> specFunction, Api api) {

    var spec = specFunction.apply(jdbc);

    String output = spec.query(new ResultSetTextFormatter());

    api.log("query%s%s", System.lineSeparator(), output);
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
