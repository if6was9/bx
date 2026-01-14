package bx.sql;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Supplier;

import javax.sql.DataSource;

import org.springframework.jdbc.core.simple.JdbcClient;

import com.google.common.base.CaseFormat;
import com.google.common.base.MoreObjects;
import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import bx.sql.duckdb.DuckDataSource;
import bx.util.BxException;
import bx.util.Config;
import bx.util.S;

public class Db implements AutoCloseable {

  private static Supplier<Db> supplier = Suppliers.memoize(Db::createStandard);

  private DataSource dataSource;
  private JdbcClient jdbcClient;

  private String urlForToString;

  public static Db getInstance() {

    return supplier.get();
  }

  static Db createStandard() {

    try {

      Config config = Config.get();

      var cfg = toHikariConfig(config);

      if (S.isEmpty(cfg.getJdbcUrl())) {
        throw new DbException("DB_URL not set");
      }

      if (cfg.getJdbcUrl().contains("jdbc:duckdb")) {

        var dds = DuckDataSource.create(DriverManager.getConnection(cfg.getJdbcUrl()));
        Db db = new Db(dds);
        db.urlForToString = cfg.getJdbcUrl();
        return db;
      } else {
        var ds = new HikariDataSource(cfg);

        Db db = new Db(ds);
        db.urlForToString = ds.getJdbcUrl();
        return db;
      }
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public static HikariConfig toHikariConfig(Config config) {

    var hikariKeys =
        List.of(
            "jdbcUrl",
            "dataSourceClassName",
            "username",
            "password",
            "autoCommit",
            "connectionTimeout",
            "idleTimeout",
            "keepaliveTime",
            "maxLifetime",
            "connectionTestQuery",
            "minimumIdle",
            "maximumPoolSize",
            "poolName",
            "initializationFailTimeout",
            "isolateInternalQueries",
            "allowPoolSuspension",
            "readOnly",
            "registerMbeans",
            "catalog",
            "connectionInitSql",
            "driverClassName",
            "transactionIsolation",
            "validationTimeout",
            "leakDetectionThreshold",
            "schema",
            "exceptionOverrideClassName");

    String prefix = "DB";

    var convert = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_UNDERSCORE);

    Properties props = new Properties();
    hikariKeys.stream()
        .forEach(
            hikariKeyName -> {
              String envName =
                  String.format("%s_%s", prefix.toUpperCase(), convert.convert(hikariKeyName));

              Optional<String> val = config.get(envName);

              if (val.isPresent()) {
                props.put(hikariKeyName, val.get());
              }
              if (envName.equals(prefix + "_JDBC_URL")) {
                envName = envName.replace("JDBC_URL", "URL");
                val = config.get(envName);
                if (val.isPresent()) {
                  props.put(hikariKeyName, val.get());
                }
              }
            });

    return new HikariConfig(props);
  }

  public static Db create(DataSource ds) {
    Db db = new Db(ds);
    return db;
  }

  public static void reset(Db db) {
    Db.supplier = Suppliers.ofInstance(db);
  }

  public static Db create(String url, String username, String password) {
    if (url.startsWith("jdbc:duckdb:")) {

      try {
        Class<?> clazz = Class.forName("bx.sql.duckdb.DuckDataSource");

        Method m = clazz.getDeclaredMethod("create", String.class);

        DataSource ds = (DataSource) m.invoke(clazz, url);

        Preconditions.checkNotNull(ds);

        return new Db(ds);

      } catch (ClassNotFoundException
          | NoSuchMethodException
          | IllegalAccessException
          | InvocationTargetException e) {
        throw new BxException(e);
      }
    }
    HikariConfig cfg = new HikariConfig();
    cfg.setJdbcUrl(url);
    cfg.setAutoCommit(true);
    cfg.setUsername(username);
    cfg.setPassword(password);
    DataSource ds = new HikariDataSource(cfg);

    Db db = new Db(ds);

    return db;
  }

  public static Db create(String url) {
    return create(url, null, null);
  }

  Db(DataSource dataSource) {
    this.dataSource = dataSource;
    if (this.dataSource != null) {
      this.urlForToString = extractUrl(dataSource);
    }
  }

  public JdbcClient getJdbcClient() {
    if (jdbcClient == null) {
      jdbcClient = JdbcClient.create(getDataSource());
    }

    return jdbcClient;
  }

  public void close() {
    if (this.dataSource == null) {
      return;
    }
    try {
      AutoCloseable ac = (AutoCloseable) this.dataSource;
      ac.close();
    } catch (ClassCastException e) {
      throw new BxException("cannot close dataSource: " + dataSource);
    } catch (BxException | DbException e) {
      throw e;
    } catch (Exception e) {
      throw new BxException(e);
    }
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  private String extractUrl(DataSource ds) {

    try (SqlCloser closer = SqlCloser.create()) {
      Connection c = dataSource.getConnection();
      closer.register(c);
      return c.getMetaData().getURL();
    } catch (SQLException e) {
      return null;
    }
  }

  public ConsoleQuery consoleQuery() {
    return ConsoleQuery.with(getDataSource());
  }

  public String toString() {
    ToStringHelper h = MoreObjects.toStringHelper(this);

    try {
      h.add("dataSource", dataSource);
    } catch (Throwable t) {
      h.add("dataSource", "unknown");
    }

    h.add("url", urlForToString);

    return h.toString();
  }
}
