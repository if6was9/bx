package bx.sql;

import bx.util.S;
import com.google.common.base.Preconditions;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZonedDateTime;

public class SqlUtil {

  public static final String TABLE_TOKEN = "{{TABLE}}";

  public static Timestamp toTimestamp(ZonedDateTime d) {
    if (d == null) {
      return null;
    }
    return toTimestamp(d.toInstant());
  }

  public static Timestamp toTimestamp(Instant t) {
    if (t == null) {
      return null;
    }
    return Timestamp.from(t);
  }

  public static Object toSqlBindType(Object val) {
    if (val == null) {
      return val;
    }
    if (val instanceof Instant) {
      Instant t = (Instant) val;
      java.sql.Timestamp x = new java.sql.Timestamp(t.toEpochMilli());
      return x;
    }
    if (val instanceof ZonedDateTime) {
      ZonedDateTime t = (ZonedDateTime) val;
      java.sql.Timestamp x = new java.sql.Timestamp(t.toInstant().toEpochMilli());
      return x;
    }
    if (val instanceof LocalDate) {
      LocalDate d = (LocalDate) val;
      return d.toString();
    }

    return val;
  }

  private static String interpolate(String input, String tableName) {
    if (input == null || S.isBlank(tableName)) {
      return input;
    }

    return input.replace(TABLE_TOKEN, tableName);
  }

  private static String generateSqlFromFragment(
      String fragment, String tableName, SqlOperation type) {

    if (S.isBlank(fragment)) {
      if (type == SqlOperation.SELECT) {
        String sql = "select * from " + tableName;
        return interpolate(sql, tableName);
      } else if (type == SqlOperation.DELETE) {
        String sql = "delete from " + tableName;
        return interpolate(sql, tableName);
      } else {
        Preconditions.checkState(false, "SqlOperation not specified and no sql fragement supplied");
      }
    }

    if (fragment.trim().toUpperCase().startsWith("SELECT")) {
      Preconditions.checkState(type == SqlOperation.SELECT);
      String sql = interpolate(fragment, tableName);
      return sql;
    } else if (fragment.trim().toUpperCase().startsWith("DELETE")) {
      Preconditions.checkState(type == SqlOperation.DELETE);
      String sql = interpolate(fragment, tableName);
      return sql;
    } else if (fragment.trim().toUpperCase().startsWith("WHERE")) {

      if (type == null) {
        throw new DbException("cannot infer SQL with only a WHERE clause");
      } else if (type == SqlOperation.SELECT) {

        String sql = String.format("SELECT * FROM %s %s", tableName, fragment);
        return sql;
      } else if (type == SqlOperation.DELETE) {
        String sql = String.format("DELETE FROM %s %s", tableName, fragment);
        return sql;
      } else {
        throw new DbException("coannt infer a %s SQL statement from only a WHERE clause");
      }
    } else {
      String sql = interpolate(fragment, tableName);
      return sql;
    }
  }
}
