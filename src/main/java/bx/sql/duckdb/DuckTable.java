package bx.sql.duckdb;

import bx.sql.ConsoleQuery;
import bx.sql.DbException;
import bx.sql.SqlUtil;
import bx.util.S;
import bx.util.Slogger;
import com.google.common.base.Joiner;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import javax.sql.DataSource;
import org.duckdb.DuckDBAppender;
import org.duckdb.DuckDBConnection;
import org.slf4j.Logger;
import org.springframework.dao.InvalidDataAccessResourceUsageException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;

public class DuckTable {

  static Logger logger = Slogger.forEnclosingClass();
  DataSource dataSource;
  String table;
  JdbcClient client;

  public DuckTable(DataSource ds, String name) {
    Preconditions.checkNotNull(ds);
    Preconditions.checkNotNull(name);
    this.dataSource = ds;
    this.table = name;
  }

  public static DuckTable of(DataSource ds, String name) {
    DuckTable table = new DuckTable(ds, name);
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

  private String interpolateTable(String sql) {
    return SqlUtil.interpolateTable(sql, getTableName());
  }

  public String getTableName() {
    return table;
  }

  public String getName() {
    return table;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public StatementSpec select(String sql) {
    return sql(sql);
  }

  public StatementSpec sql(String sql) {
    return getJdbcClient().sql(interpolateTable(sql));
  }

  public void describe() {

    consoleQuery().select("describe {{table}}");
  }

  public void show() {
    consoleQuery().show();
  }

  public String getCreateTableSql() {
    String sql = "select sql from duckdb_tables where table_name=:name";
    Optional<String> createSql =
        sql(sql).param("name", getTableName()).query(String.class).optional();
    if (createSql.isEmpty()) {
      throw new InvalidDataAccessResourceUsageException("table not found: " + getName());
    }
    return createSql.get();
  }

  public long rowCount() {
    String sql = "select count(*) as cnt from {{table}}";

    return (Long) sql(sql).query(Long.class).single();
  }

  public boolean exists() {

    if (S.isBlank(getTableName())) {
      return false;
    }

    AtomicBoolean b = new AtomicBoolean(false);

    String sql = "SELECT name from (SHOW TABLES) where name=:name";
    logger.atDebug().log("SQL: {}", sql);

    Optional<String> name = sql(sql).param("name", getTableName()).query(String.class).optional();

    return name.isPresent();
  }

  public void drop() {

    String sql = String.format("DROP TABLE IF EXISTS %s", getTableName());

    getJdbcClient().sql(sql).update();
  }

  public List<String> getColumnNames() {

    String sql = String.format("select column_name from (describe %s)", getTableName());
    logger.atDebug().log("SQL: {}", sql);
    var names = getJdbcClient().sql(sql).query(String.class).list();

    return List.copyOf(names);
  }

  public DuckTable renameTable(String newName) {

    String sql = String.format("ALTER TABLE %s RENAME TO %s", getTableName(), newName);
    logger.atDebug().log("SQL: {}", sql);
    getJdbcClient().sql(sql).update();

    return DuckTable.of(getDataSource(), newName);
  }

  public void renameColumn(String oldName, String newName) {

    String sql = String.format("ALTER TABLE %s RENAME %s to %s", getTableName(), oldName, newName);
    logger.atDebug().log("SQL: {}", sql);
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
    logger.atDebug().log("SQL: {}", sql);
    getJdbcClient().sql(sql).update();
  }

  public void addColumn(String columnSpec) {
    Preconditions.checkNotNull(columnSpec, "columnSpec");
    String sql = String.format("ALTER TABLE %s ADD COLUMN %s", getTableName(), columnSpec);
    getJdbcClient().sql(sql).update();
  }

  public int deleteRow(long id) {
    String sql = String.format("DELETE FROM %s where rowid=:rowid", getTableName());
    logger.atDebug().log("SQL: {}", sql);
    return getJdbcClient().sql(sql).param("rowid", id).update();
  }

  public int update(long rowId, String column, Object val) {

    String sql = String.format("UPDATE %s set %s=:val where rowid=:rowid", getTableName(), column);
    logger.atDebug().log("SQL: {}", sql);
    return getJdbcClient().sql(sql).param("val", val).param("rowid", rowId).update();
  }

  public ConsoleQuery consoleQuery() {
    return ConsoleQuery.with(getJdbcClient()).table(getTableName());
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

  public void addPrimaryKey(String... columns) {

    Preconditions.checkArgument(
        columns != null && columns.length > 0, "must provide at least one column");
    String columnSpec = Joiner.on(", ").join(List.of(columns));

    String sql = String.format("ALTER TABLE '%s' ADD PRIMARY KEY (%s)", getTableName(), columnSpec);
    logger.atDebug().log("SQL: {}", sql);
    getJdbcClient().sql(sql).update();
    getJdbcClient().sql("CHECKPOINT").query().listOfRows();
  }

  public DuckTable table(String name) {
    return DuckTable.of(getDataSource(), name);
  }

  public DuckCsvExport csvExport() {
    return new DuckCsvExport(this);
  }

  public DuckCsvImport csvImport() {
    return new DuckCsvImport(this);
  }

  public String toString() {
    return MoreObjects.toStringHelper(this)
        .add("name", getTableName())
        .add("dataSource", getDataSource())
        .toString();
  }
}
