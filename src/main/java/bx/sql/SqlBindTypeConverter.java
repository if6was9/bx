package bx.sql;

import bx.util.Dates;
import bx.util.S;
import bx.util.Slogger;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.springframework.asm.Type;

public abstract class SqlBindTypeConverter {

  static Logger logger = Slogger.forEnclosingClass();

  static SqlBindTypeConverter STRING_CONVERTER = new StringConverter();
  static SqlBindTypeConverter BOOLEAN_CONVERTER = new BooleanConverter();
  static SqlBindTypeConverter DOUBLE_CONVERTER = new DoubleConverter();
  static SqlBindTypeConverter FLOAT_CONVERTER = new FloatConverter();
  static SqlBindTypeConverter DECIMAL_CONVERTER = new DecimalConverter();
  static SqlBindTypeConverter INTEGER_CONVERTER = new IntegerConverter();
  static SqlBindTypeConverter DATE_CONVERTER = new DateConverter();
  static SqlBindTypeConverter TIME_CONVERTER = new TimeConverter();
  static SqlBindTypeConverter TIMESTAMP_CONVERTER = new TimestampConverter();
  static SqlBindTypeConverter TIMESTAMP_WITH_TIMEZONE_CONVERTER =
      new TimestampWithTimezoneConverter();
  static DateTimeFormatter YYMMDD = DateTimeFormatter.ofPattern("YYMMDD");
  static DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("YYYYMMDD");

  public abstract Object doConvert(Object val);

  public final Object convert(Object val) {
    if (val == null) {
      return val;
    }
    return doConvert(val);
  }

  public static Object convert(Object val, int type) {

    if (val == null) {
      return val;
    }

    switch (type) {
      case Types.CHAR:
      case Types.VARCHAR:
      case Types.NVARCHAR:
      case Types.LONGVARCHAR:
      case Types.LONGNVARCHAR:
        return STRING_CONVERTER.convert(val);
      case Types.BOOLEAN:
        return BOOLEAN_CONVERTER.convert(val);

      case Type.DOUBLE:
        return DOUBLE_CONVERTER.convert(val);
      case Type.FLOAT:
        return FLOAT_CONVERTER.convert(val);
      case Types.DECIMAL:
      case Types.NUMERIC:
        return DECIMAL_CONVERTER.convert(val);
      case Types.INTEGER:
        return INTEGER_CONVERTER.convert(val);

      case Types.DATE:
        return DATE_CONVERTER.convert(val);
      case Types.TIME:
        return TIME_CONVERTER.convert(val);
      case Types.TIME_WITH_TIMEZONE:
      case Types.TIMESTAMP:
        return TIMESTAMP_CONVERTER.convert(val);
      case Types.TIMESTAMP_WITH_TIMEZONE:
        return TIMESTAMP_WITH_TIMEZONE_CONVERTER.convert(val);
    }

    if (val instanceof String && S.isBlank((String) val)) {
      return null;
    }

    logger.atWarn().log("unhandled sql type: {}", type);
    return val;
  }

  public static class DecimalConverter extends SqlBindTypeConverter {
    public Object doConvert(Object val) {
      if (val == null) {
        return val;
      } else if (val instanceof BigDecimal) {
        return val;
      } else if (val instanceof Number) {

        return new BigDecimal("" + val);

      } else if (val instanceof String) {
        return new BigDecimal((String) val);
      } else if (val instanceof Boolean) {
        return ((Boolean) val) ? new BigDecimal("1") : new BigDecimal("0");
      }
      return val;
    }
  }

  public static class DoubleConverter extends SqlBindTypeConverter {
    public Object doConvert(Object val) {
      if (val == null) {
        return val;
      } else if (val instanceof Double) {
        return val;
      } else if (val instanceof Number) {
        return ((Number) val).doubleValue();
      } else if (val instanceof String) {
        return Double.parseDouble((String) val);
      } else if (val instanceof Boolean) {
        return ((Boolean) val) ? 1.0 : 0.0;
      }
      return val;
    }
  }

  public static class IntegerConverter extends SqlBindTypeConverter {
    public Object doConvert(Object val) {
      if (val == null) {
        return val;
      } else if (val instanceof Long) {
        // better to pass through longs because what is known as "INTEGER"
        // is likely 64-bit capable
        return val;
      } else if (val instanceof Integer) {
        return val;
      } else if (val instanceof Number) {
        return ((Number) val).longValue();
      } else if (val instanceof String) {
        return new BigDecimal((String) val).longValue();

      } else if (val instanceof Boolean) {
        return ((Boolean) val) ? 1 : 0;
      }
      return val;
    }
  }

  public static class DateConverter extends SqlBindTypeConverter {
    public Object doConvert(Object val) {
      if (val == null) {
        return val;
      } else if (val instanceof java.sql.Date) {
        return val;
      } else if (val instanceof java.sql.Timestamp) {
        Timestamp ts = (Timestamp) val;
        return java.sql.Date.valueOf(ts.toLocalDateTime().toLocalDate());
      } else if (val instanceof java.util.Date) {
        Date d = (Date) val;
        return java.sql.Date.valueOf(d.toString());

      } else if (val instanceof String) {

        String sval = (String) val;
        if (S.isBlank(sval)) {
          return null;
        }
        Optional<LocalDate> localDate = parseLocalDate((String) val);
        if (localDate.isPresent()) {
          return java.sql.Date.valueOf(localDate.get());
        }
      }
      return val;
    }
  }

  public static class TimeConverter extends SqlBindTypeConverter {
    public Object doConvert(Object val) {

      if (val == null) {
        return val;
      } else if (val instanceof java.sql.Time) {
        return val;
      } else if (val instanceof java.sql.Timestamp) {
        Timestamp ts = (Timestamp) val;

        return java.sql.Time.valueOf(ts.toLocalDateTime().toLocalTime());
      } else if (val instanceof String) {

        String sval = (String) val;
        if (S.isBlank(sval)) {
          return null;
        }
        Optional<LocalTime> localTime = Dates.asLocalTime((String) val);

        if (localTime.isPresent()) {
          return java.sql.Time.valueOf(localTime.get());
        }
      }
      return val;
    }
  }

  public static class TimestampConverter extends SqlBindTypeConverter {
    public Object doConvert(Object val) {

      if (val == null) {
        return val;
      } else if (val instanceof java.sql.Timestamp) {
        return val;
      } else if (val instanceof String) {

        String sval = (String) val;
        if (S.isBlank(sval)) {
          return null;
        }
        Optional<LocalDateTime> localDateTime = Dates.asLocalDateTime((String) val);

        if (localDateTime.isPresent()) {
          return java.sql.Timestamp.valueOf(localDateTime.get());
        }
      }
      return val;
    }
  }

  public static class TimestampWithTimezoneConverter extends SqlBindTypeConverter {
    public Object doConvert(Object val) {

      if (val == null) {
        return val;
      } else if (val instanceof OffsetDateTime) {
        return val;
      } else if (val instanceof ZonedDateTime) {
        ZonedDateTime zdt = (ZonedDateTime) val;
        return zdt.toOffsetDateTime();
      } else if (val instanceof Instant) {
        ((Instant) val).atOffset(ZoneOffset.UTC);
      } else if (val instanceof Timestamp) {
        ((Timestamp) val).toInstant().atOffset(ZoneOffset.UTC);
      } else if (val instanceof String) {

        String sval = (String) val;
        if (S.isBlank(sval)) {
          return null;
        }

        Optional<OffsetDateTime> odt = Dates.asOffsetDateTime(sval);

        if (odt.isPresent()) {

          return odt.get();
        }
      }
      return val;
    }
  }

  public Optional<LocalDate> parseLocalDate(String val) {
    if (S.isBlank(val)) {
      return Optional.empty();
    }

    if (val.length() > 10) {
      val = val.substring(0, 10);
    }
    Optional<TemporalAccessor> ta =
        Dates.parse(
            val,
            DateTimeFormatter.BASIC_ISO_DATE,
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ISO_DATE,
            YYMMDD,
            YYYYMMDD);
    if (ta.isPresent()) {
      return Optional.of(LocalDate.from(ta.get()));
    }

    return Optional.empty();
  }

  public static class FloatConverter extends SqlBindTypeConverter {
    public Object doConvert(Object val) {
      if (val == null) {
        return val;
      } else if (val instanceof Float) {
        return val;
      } else if (val instanceof Number) {
        return ((Number) val).floatValue();
      } else if (val instanceof String) {
        return Float.parseFloat((String) val);
      } else if (val instanceof Boolean) {
        return ((Boolean) val) ? 1.0 : 0.0;
      }
      return val;
    }
  }

  public static class BooleanConverter extends SqlBindTypeConverter {

    Set<String> TRUE_VALUES = Set.of("1", "true", "yes", "y", "t");
    Set<String> FALSE_VALUES = Set.of("0", "false", "no", "n", "f");

    @Override
    public Object doConvert(Object val) {

      if (val instanceof Boolean) {
        return val;
      } else if (val instanceof String) {
        String stringVal = (String) val;
        if (S.isBlank(stringVal)) {
          return null;
        }
        stringVal = stringVal.trim().toLowerCase();
        if (TRUE_VALUES.contains(stringVal)) {
          return true;
        } else if (FALSE_VALUES.contains(stringVal)) {
          return false;
        }
        throw new IllegalArgumentException("cannot convert '" + val + "' to boolean");

      } else if (val instanceof Number) {
        return ((Number) val).intValue() != 0;
      }
      return val;
    }
  }

  public static class StringConverter extends SqlBindTypeConverter {

    @Override
    public Object doConvert(Object val) {
      return Objects.toString(val, null);
    }
  }
}
