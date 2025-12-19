package bx.sql.duckdb;

import bx.sql.DbException;
import bx.sql.PrettyQuery;
import bx.util.BxException;
import bx.util.S;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.io.CharSource;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.duckdb.DuckDBAppender;
import org.duckdb.DuckDBConnection;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class DuckTable {

  DataSource dataSource;
  String table;
  JdbcClient client;

  public static DuckTable of(DataSource ds, String name) {
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

  public StatementSpec select(String sql) {
	  return getJdbcClient().sql(sql);
  }
  public void show() {
	  prettyQuery().select();
  }
  public long rowCount() {
    String sql = String.format("select count(*) as cnt from %s", getTableName());

    return (Long) getJdbcClient().sql(sql).query().singleValue();
  }

  public boolean exists() {

    if (S.isBlank(getTableName())) {
      return false;
    }

    AtomicBoolean b = new AtomicBoolean(false);
    getJdbcClient()
        .sql("show tables")
        .query(
            c -> {
              String name = c.getString("name");
              if (name.equalsIgnoreCase(getTableName())) {
                b.set(true);
              }
            });

    return b.get();
  }

  public void drop() {

    String sql = String.format("DROP TABLE IF EXISTS %s", getTableName());

    getJdbcClient().sql(sql).update();
  }

  public List<String> getColumnNames() {
    if (!exists()) {
      throw new DbException("table does not exist: " + getTableName());
    }

    String sql = String.format("select * from %s limit 1", getTableName());

    var names =
        getJdbcClient()
            .sql(sql)
            .query(
                rs -> {
                  var md = rs.getMetaData();
                  List<String> list = Lists.newArrayList();
                  for (int i = 1; i <= md.getColumnCount(); i++) {
                    String name = md.getColumnName(i);
                    list.add(name);
                  }
                  return list;
                });

    return List.copyOf(names);
  }

  public DuckTable renameTable(String newName) {

    String sql = String.format("ALTER TABLE %s RENAME TO %s", getTableName(), newName);

    getJdbcClient().sql(sql).update();

    return DuckTable.of(getDataSource(), newName);
  }

  public void renameColumn(String oldName, String newName) {

    String sql = String.format("ALTER TABLE %s RENAME %s to %s", getTableName(), oldName, newName);

    getJdbcClient().sql(sql).update();
  }

  public boolean hasColumn(String name) {
    return getColumnNames().stream().filter(n -> n.equalsIgnoreCase(name)).findAny().isPresent();
  }

  public void dropColumnsExcept(Collection<String> colsToKeep) {

    Set<String> keep = colsToKeep.stream().map(c -> c.toUpperCase()).collect(Collectors.toSet());
    for (String col : getColumnNames()) {
      if (!keep.contains(col.toUpperCase())) {
        dropColumn(col);
      }
    }
  }

  public void dropColumnsExcept(String... cols) {

    dropColumnsExcept(List.of(cols));
  }

  public void dropColumns(Collection<String> cols) {

    for (String col : cols) {
      dropColumn(col);
    }
  }

  public void dropColumns(String... cols) {

    dropColumns(List.of(cols));
  }

  public void dropColumn(String column) {

    if (!hasColumn(column)) {
      return;
    }

    String sql = String.format("ALTER TABLE %s DROP %s", getTableName(), column);
    getJdbcClient().sql(sql).update();
  }

  public void addColumn(String columnSpec) {
    Preconditions.checkNotNull(columnSpec, "columnSpec");
    String sql = String.format("ALTER TABLE %s ADD COLUMN %s", table, columnSpec);
    getJdbcClient().sql(sql).update();
  }

  public int deleteRow(long id) {
    String sql = String.format("DELETE FROM %s where rowid=:rowid", getTableName());

    return getJdbcClient().sql(sql).param("rowid", id).update();
  }

  public int update(long rowId, String column, Object val) {

    String sql = String.format("UPDATE %s set %s=:val where rowid=:rowid", getTableName(), column);

    return getJdbcClient().sql(sql).param("val", val).param("rowid", rowId).update();
  }

  public PrettyQuery prettyQuery() {
    return PrettyQuery.with(getJdbcClient()).table(getTableName());
  }

  public DuckDBAppender createAppender() {

    try {
      Connection c = dataSource.getConnection();

      DuckDBConnection dc = c.unwrap(DuckDBConnection.class);

      DuckDBAppender appender = dc.createAppender(DuckDBConnection.DEFAULT_SCHEMA, table);

      return appender;
    } catch (SQLException e) {
      throw new DbException(e);
    }
  }

  public void toCsv(Consumer<CharSource> consumer) {

    Path p = null;
    try {
      p = Files.createTempFile(getTableName(), ".csv");

      writeCsv(p.toFile());

      consumer.accept(com.google.common.io.Files.asCharSource(p.toFile(), StandardCharsets.UTF_8));

    } catch (IOException e) {
      throw new BxException(e);
    } finally {
      if (p != null) {
        p.toFile().delete();
      }
    }
  }

  public void toCsv(Consumer<CharSource> consumer, String sql) {

    Path p = null;
    try {
      p = Files.createTempFile(getTableName(), ".csv");

      writeCsv(p.toFile(), sql);

      consumer.accept(com.google.common.io.Files.asCharSource(p.toFile(), StandardCharsets.UTF_8));
    } catch (IOException e) {
      throw new BxException(e);
    } finally {
      if (p != null) {
        p.toFile().delete();
      }
    }
  }

  public void writeCsv(File f, String sql) {
    String exportSql =
        String.format("COPY (%s) TO '%s' (HEADER, DELIMITER ',')", sql, f.getAbsolutePath());
    getJdbcClient().sql(exportSql).update();
  }

  public void writeCsv(File f) {
    String sql =
        String.format(
            "COPY %s TO '%s' (HEADER, DELIMITER ',')", getTableName(), f.getAbsolutePath());

    getJdbcClient().sql(sql).update();
  }

  public DuckTable createTableFromCsv(CharSource s) {

    return createTableFromCsv(s, getTableName());
  }

  public DuckTable createTableFromCsv(CharSource s, String tableName) {

    Path tmp = null;
    try {
      tmp = Files.createTempFile("table", ".csv");

      var cs = com.google.common.io.Files.asCharSink(tmp.toFile(), StandardCharsets.UTF_8);
      s.copyTo(cs);

      return createTableFromCsv(tmp.toFile());
    } catch (IOException e) {
      throw new BxException(e);
    } finally {
      if (tmp != null && tmp.toFile().exists()) {
        tmp.toFile().delete();
      }
    }
  }

  public DuckTable createTableFromCsv(File f) {

    return createTableFromCsv(f, getTableName());
  }

  public DuckTable createTableFromCsv(File f, String tableName) {

    String sql =
        String.format("CREATE TABLE %s as (select * from '%s')", tableName, f.getAbsolutePath());

    getJdbcClient().sql(sql).update();

    return DuckTable.of(getDataSource(), tableName);
  }

  public void addPrimaryKey(String column) {

    String sql = String.format("ALTER TABLE %s ADD PRIMARY KEY (%s)", getTableName(), column);

    getJdbcClient().sql(sql).update();
  }

  public DuckTable table(String name) {
    return DuckTable.of(getDataSource(), name);
  }

  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", getTableName())
        .add("db", getDataSource())
        .toString();
  }
}
