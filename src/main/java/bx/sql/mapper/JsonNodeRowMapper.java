package bx.sql.mapper;

import bx.sql.Results;
import bx.util.Json;
import com.google.common.base.Preconditions;
import com.google.common.flogger.FluentLogger;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeType;
import tools.jackson.databind.node.ObjectNode;

public class JsonNodeRowMapper<T> implements org.springframework.jdbc.core.RowMapper<T> {

  ResultSet resultSet;
  Results results;
  ResultSetMetaData md;

  static FluentLogger logger = FluentLogger.forEnclosingClass();
  JsonNodeType targetType = JsonNodeType.OBJECT;

  ArrayNode arrayNodeTarget;
  ObjectNode objectNodeTarget;

  public JsonNodeRowMapper(JsonNodeType type) {
    Preconditions.checkArgument(type == JsonNodeType.OBJECT || type == JsonNodeType.ARRAY);
    this.targetType = type;
  }

  public T mapRow(ResultSet resultSet, int rowNum) throws SQLException {

    Preconditions.checkState(
        this.resultSet == null || this.resultSet == resultSet,
        getClass().getSimpleName() + " cannot be re-used");

    if (this.resultSet == null) {

      this.resultSet = resultSet;
      this.results = Results.create(resultSet);
      this.md = resultSet.getMetaData();
    }

    if (targetType == JsonNodeType.OBJECT) {
      objectNodeTarget = Json.createObjectNode();
    } else if (targetType == JsonNodeType.ARRAY) {
      arrayNodeTarget = Json.createArrayNode();
    }

    for (int i = 1; i <= md.getColumnCount(); i++) {
      String name = md.getColumnName(i);
      int type = md.getColumnType(i);

      switch (type) {
        case (Types.FLOAT):
        case (Types.DOUBLE):
        case (Types.DECIMAL):
          Optional<Double> d = results.getDouble(name);
          setDouble(i, name, d.orElse(null));
          break;
        case (Types.BIGINT):
        case (Types.INTEGER):
          setLong(i, name, results.getLong(name).orElse(null));

          break;
        case (Types.BOOLEAN):
          setBoolean(i, name, results.getBoolean(name).orElse(null));

          break;
        case (Types.TIME):
        case (Types.TIME_WITH_TIMEZONE):
        case (Types.TIMESTAMP):
        case (Types.TIMESTAMP_WITH_TIMEZONE):
        case (Types.DATE):
          setString(i, name, results.getString(name).orElse(null));
          break;
        case (Types.VARCHAR):
        case (Types.NVARCHAR):
          setString(i, name, results.getString(name).orElse(null));
          break;
        default:
          logger.atWarning().atMostEvery(5, TimeUnit.SECONDS).log(
              "no type conversion for name=%s type=%s(%s) (relying on string)",
              name, type, md.getColumnTypeName(i));
          setString(i, name, results.getString(name).orElse(null));
      }
    }

    if (objectNodeTarget != null) {
      return (T) objectNodeTarget;
    }
    return (T) arrayNodeTarget;
  }

  void setLong(int col, String name, Long val) {
    if (arrayNodeTarget != null) {
      arrayNodeTarget.add(val);
    } else {
      objectNodeTarget.put(name, val);
    }
  }

  void setDouble(int col, String name, Double val) {
    if (arrayNodeTarget != null) {
      arrayNodeTarget.add(val);
    } else {
      objectNodeTarget.put(name, val);
    }
  }

  void setBoolean(int col, String name, Boolean val) {
    if (arrayNodeTarget != null) {
      arrayNodeTarget.add(val);
    } else {
      objectNodeTarget.put(name, val);
    }
  }

  void setString(int col, String name, String val) {
    if (arrayNodeTarget != null) {
      arrayNodeTarget.add(val);
    } else {
      objectNodeTarget.put(name, val);
    }
  }
}
