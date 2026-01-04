package bx.sql;

import bx.util.BxException;
import bx.util.ConsoleTableRenderer;
import bx.util.S;
import com.google.common.base.Preconditions;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.function.Function;
import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class PrettyQuery {

  JdbcClient jdbc;

  String tableName;

  Logger logger = defaultLogger;
  Level level = defaultLevel;

  static Logger defaultLogger = LoggerFactory.getLogger(PrettyQuery.class);
  static Level defaultLevel = Level.INFO;

  Writer outputWriter;

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

  private Logger getLogger() {
    return logger;
  }

  private Level getLevel() {
    return level;
  }

  public PrettyQuery to(Logger logger) {
    this.logger = logger;
    return this;
  }

  public void show() {
    select();
  }

  public void select() {

    try {
      Preconditions.checkState(S.isNotBlank(tableName), "table name must be set for select()");

      select("select * as cnt from {{table}}");
    } catch (RuntimeException e) {
      getLogger().atWarn().setCause(e).log("failure");
    }
  }

  private void writeOutput(String out) {

    try {
      if (isEnabled()) {

        if (outputWriter != null) {
          outputWriter.append(out);
          outputWriter.append(System.lineSeparator());
        }
        logger.atLevel(level).log("\n{}", out);
      }
    } catch (IOException e) {
      throw new BxException(e);
    }
  }

  public PrettyQuery stderr() {
    return out(System.err);
  }

  public PrettyQuery stdout() {
    return out(System.out);
  }

  public PrettyQuery out(OutputStream out) {
    return out(new OutputStreamWriter(out));
  }

  public PrettyQuery out(Writer out) {

    this.outputWriter = out;

    return this;
  }

  public PrettyQuery out(Logger b, Level level) {
    Preconditions.checkNotNull(b);
    Preconditions.checkNotNull(level);
    this.logger = b;
    this.level = level;
    return this;
  }

  public PrettyQuery out(Logger b) {
    Preconditions.checkNotNull(b);
    this.logger = b;
    this.level = defaultLevel;
    return this;
  }

  public boolean isEnabled() {
    return (outputWriter != null || getLogger().isEnabledForLevel(getLevel()));
  }

  public void select(String sql) {

    if (!isEnabled()) {
      return;
    }
    final String s = interpolateTableName(sql);
    select(c -> c.sql(s));
  }

  public static synchronized void resetDefaultOutput() {
    defaultLogger = LoggerFactory.getLogger(PrettyQuery.class);
    defaultLevel = Level.DEBUG;
  }

  public static synchronized void setDefaultOutput(Logger logger, Level level) {
    defaultLogger = logger;
    defaultLevel = level;
  }

  String interpolateTableName(String sql) {
    return SqlUtil.interpolateTable(sql, this.tableName);
  }

  public void select(Function<JdbcClient, StatementSpec> specFunction) {

    if (!isEnabled()) {
      return;
    }
    try {
      var spec = specFunction.apply(jdbc);

      String out = spec.query(new ConsoleTableRenderer());

      writeOutput(out);
    } catch (RuntimeException e) {
      getLogger().atWarn().setCause(e).log("failure");
    }
  }
}
