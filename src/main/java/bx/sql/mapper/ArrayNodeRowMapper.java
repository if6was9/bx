package bx.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.JsonNodeType;

public class ArrayNodeRowMapper extends JsonNodeRowMapper<ArrayNode> {

  public ArrayNodeRowMapper() {
    super(JsonNodeType.ARRAY);
  }

  @Override
  public ArrayNode mapRow(ResultSet rs, int rowNum) throws SQLException {
    return (ArrayNode) super.mapRow(rs, rowNum);
  }

  @Override
  void setLong(int col, String name, Long val) {
    arrayNodeTarget.set(col - 1, val);
  }

  @Override
  void setDouble(int col, String name, Double val) {
    arrayNodeTarget.set(col - 1, val);
  }

  @Override
  void setBoolean(int col, String name, Boolean val) {
    arrayNodeTarget.set(col - 1, val);
  }

  @Override
  void setString(int col, String name, String val) {
    arrayNodeTarget.set(col - 1, val);
  }
}
