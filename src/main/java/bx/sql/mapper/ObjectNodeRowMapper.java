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
}
