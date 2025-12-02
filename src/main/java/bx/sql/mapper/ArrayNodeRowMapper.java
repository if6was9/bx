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
}
