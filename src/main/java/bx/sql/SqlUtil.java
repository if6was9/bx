package bx.sql;

import bx.util.S;
import com.google.common.base.Preconditions;

public class SqlUtil {

  public static final String TABLE_TOKEN = "{{table}}";

  private SqlUtil() {}

  private static String interpolate(String input, String tableName) {
    if (input == null || S.isBlank(tableName)) {
      return input;
    }

    return input.replace(TABLE_TOKEN, tableName);
  }

  public static String generateSqlFromFragment(
      String fragment, String tableName, SqlOperation type) {

    if (S.isBlank(fragment)) {
      if (type == SqlOperation.SELECT) {
        String sql = "SELECT * FROM " + tableName;
        return interpolate(sql, tableName);
      } else if (type == SqlOperation.DELETE) {
        String sql = "DELETE FROM " + tableName;
        return interpolate(sql, tableName);
      } else {
        Preconditions.checkState(false, "SqlOperation not specified and no sql fragement supplied");
      }
    }

    if (fragment.trim().toUpperCase().startsWith("SELECT")) {
      Preconditions.checkState(type == SqlOperation.SELECT);
      String sql = interpolate(fragment, tableName);
      return sql.trim();
    } else if (fragment.trim().toUpperCase().startsWith("DELETE")) {
      Preconditions.checkState(type == SqlOperation.DELETE);
      String sql = interpolate(fragment, tableName);
      return sql.trim();
    } else if (fragment.trim().toUpperCase().startsWith("WHERE")) {

      if (type == null) {
        throw new DbException("cannot infer SQL with only a WHERE clause");
      } else if (type == SqlOperation.SELECT) {

        String sql = String.format("SELECT * FROM %s %s", tableName.trim(), fragment.trim());
        return sql.trim();
      } else if (type == SqlOperation.DELETE) {
        String sql = String.format("DELETE FROM %s %s", tableName.trim(), fragment.trim());
        return sql.trim();
      } else {
        throw new DbException("coannt infer a %s SQL statement from only a WHERE clause");
      }
    } else {
      String sql = interpolate(fragment, tableName);
      return sql.trim();
    }
  }

  public static String interpolateTable(String sql, String table) {

    if (sql == null || S.isBlank(table)) {
      return sql;
    }

    if (table.chars().allMatch(c -> Character.isWhitespace(c))) {
      table = String.format("\"%s\"", table);
    }
    return sql.replace(TABLE_TOKEN, table);
  }
}
