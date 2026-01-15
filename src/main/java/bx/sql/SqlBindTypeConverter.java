package bx.sql;

import bx.util.S;
import bx.util.Slogger;
import java.math.BigDecimal;
import java.sql.Types;
import java.util.Objects;
import java.util.Set;
import org.slf4j.Logger;

public abstract class SqlBindTypeConverter {

  static Logger logger = Slogger.forEnclosingClass();

  static SqlBindTypeConverter STRING_CONVERTER = new StringConverter();
  static SqlBindTypeConverter BOOLEAN_CONVERTER = new BooleanConverter();
  static SqlBindTypeConverter DOUBLE_CONVERTER = new DoubleConverter();
  static SqlBindTypeConverter FLOAT_CONVERTER = new FloatConverter();
  static SqlBindTypeConverter DECIMAL_CONVERTER = new DecimalConverter();
  static SqlBindTypeConverter INTEGER_CONVERTER = new IntegerConverter();

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
    if (type == Types.CHAR
        || type == Types.VARCHAR
        || type == Types.NVARCHAR
        || type == Types.LONGNVARCHAR
        || type == Types.LONGVARCHAR) {
      return STRING_CONVERTER.convert(val);
    }
    if (type == Types.BOOLEAN) {
      return BOOLEAN_CONVERTER.convert(val);
    }
    if (type == Types.DOUBLE) {
      return DOUBLE_CONVERTER.convert(val);
    }
    if (type == Types.FLOAT) {
      return FLOAT_CONVERTER.convert(val);
    }
    if (type == Types.DECIMAL) {
      return DECIMAL_CONVERTER.convert(val);
    }
    if (type == Types.INTEGER) {
      return INTEGER_CONVERTER.convert(val);
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
