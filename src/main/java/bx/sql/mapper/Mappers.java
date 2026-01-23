package bx.sql.mapper;

import bx.sql.Results;
import bx.util.TimeSeriesValue;
import bx.util.Zones;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
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

  public static RowMapper<TimeSeriesValue> timeSeriesMapper() {
    return timeSeriesMapper(1, 2);
  }

  public static RowMapper<TimeSeriesValue> timeSeriesMapper(String dateCol, String numberCol) {
    return timeSeriesMapper(dateCol, numberCol, null);
  }

  public static RowMapper<TimeSeriesValue> timeSeriesMapper(
      String dateCol, String numberCol, ZoneId zone) {

    RowMapper<TimeSeriesValue> mapper =
        new RowMapper<>() {

          @Override
          public TimeSeriesValue mapRow(ResultSet rs, int row) throws SQLException {

            Results rsx = Results.create(rs);

            Optional<Double> val = rsx.getDouble(numberCol);

            Object dateObj = rs.getObject(dateCol);

            if (dateObj instanceof LocalDate) {
              return new TimeSeriesValue((LocalDate) dateObj, val.orElse(null));
            } else if (dateObj instanceof LocalDateTime) {
              LocalDateTime ldt = (LocalDateTime) dateObj;
              return new TimeSeriesValue(
                  ldt.atZone(zone != null ? zone : Zones.UTC), val.orElse(null));
            } else if (dateObj instanceof Timestamp) {
              Instant t = ((Timestamp) dateObj).toInstant();
              if (zone != null) {
                return new TimeSeriesValue(t.atZone(zone), val.orElse(null));
              } else {
                return new TimeSeriesValue(t, val.orElse(null));
              }
            } else if (dateObj instanceof OffsetDateTime) {
              ZonedDateTime zdt = ((OffsetDateTime) dateObj).toZonedDateTime();
              if (zone != null) {
                zdt = zdt.withZoneSameInstant(zone);
              }
              return new TimeSeriesValue(zdt, val.orElse(null));
            } else if (dateObj instanceof ZonedDateTime) {
              ZonedDateTime zdt = (ZonedDateTime) dateObj;
              if (zone != null) {
                zdt = zdt.withZoneSameInstant(zone);
              }
              return new TimeSeriesValue(zdt, val.orElse(null));
            }

            throw new IllegalStateException("unsupported date/time type: " + dateObj.getClass());
          }
        };
    return mapper;
  }

  public static RowMapper<TimeSeriesValue> timeSeriesMapper(int dateCol, int numberCol) {
    return timeSeriesMapper(dateCol, numberCol, null);
  }

  public static RowMapper<TimeSeriesValue> timeSeriesMapper(
      int dateCol, int numberCol, ZoneId zone) {

    RowMapper<TimeSeriesValue> mapper =
        new RowMapper<>() {

          RowMapper<TimeSeriesValue> byNameMapper = null;

          @Override
          public TimeSeriesValue mapRow(ResultSet rs, int rowNum) throws SQLException {

            if (byNameMapper == null) {
              String dateColName = rs.getMetaData().getColumnName(dateCol);
              String valColName = rs.getMetaData().getColumnName(numberCol);
              byNameMapper = timeSeriesMapper(dateColName, valColName, zone);
            }
            return byNameMapper.mapRow(rs, rowNum);
          }
        };

    return mapper;
  }
}
