package bx.sql.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import tools.jackson.databind.node.JsonNodeType;
import tools.jackson.databind.node.ObjectNode;

public class ObjectNodeRowMapper extends JsonNodeRowMapper<ObjectNode> {

  public ObjectNodeRowMapper() {
    super(JsonNodeType.OBJECT);
  }

  @Override
  public ObjectNode mapRow(ResultSet rs, int rowNum) throws SQLException {
    return (ObjectNode) super.mapRow(rs, rowNum);
  }

  @Override
  void setLong(int col, String name, Long val) {
    objectNodeTarget.put(name, val);
  }

  @Override
  void setDouble(int col, String name, Double val) {
    objectNodeTarget.put(name, val);
  }

  @Override
  void setBoolean(int col, String name, Boolean val) {
    objectNodeTarget.put(name, val);
  }

  @Override
  void setString(int col, String name, String val) {
    objectNodeTarget.put(name, val);
  }
}
