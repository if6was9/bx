package bx.sql.mapper;

import bx.sql.Results;
import bx.util.DateNumberPoint;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.springframework.jdbc.core.RowMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

public class Mappers {

  public static org.springframework.jdbc.core.RowMapper<ObjectNode> jsonObjectMapper() {
    return new ObjectNodeRowMapper();
  }

  public static RowMapper<ArrayNode> jsonArrayMapper() {
    return new ArrayNodeRowMapper();
  }

  public static RowMapper<DateNumberPoint> dateNumberPointMapper() {
    return dateNumberPointMapper(1, 2);
  }

  public static RowMapper<DateNumberPoint> dateNumberPointMapper(String dateCol, String numberCol) {

    RowMapper<DateNumberPoint> mapper =
        new RowMapper<>() {

          @Override
          public DateNumberPoint mapRow(ResultSet rs, int row) throws SQLException {

            Results rsx = Results.create(rs);
            DateNumberPoint p =
                new DateNumberPoint(
                    rsx.getLocalDate(dateCol).get(), rsx.getDouble(numberCol).orElse(null));
            return p;
          }
        };
    return mapper;
  }

  public static RowMapper<DateNumberPoint> dateNumberPointMapper(int dateCol, int numberCol) {

    RowMapper<DateNumberPoint> mapper =
        new RowMapper<>() {

          @Override
          public DateNumberPoint mapRow(ResultSet rs, int rowNum) throws SQLException {
            Results rsx = Results.create(rs);
            DateNumberPoint p =
                new DateNumberPoint(
                    rsx.getLocalDate(dateCol).get(), rsx.getDouble(numberCol).orElse(null));
            return p;
          }
        };

    return mapper;
  }
}
