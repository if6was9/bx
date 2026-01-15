package bx.sql;

import java.math.BigDecimal;
import java.sql.Types;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class SqlBindTypeConverterTest {

  @Test
  public void testStringTypes() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.VARCHAR)).isEqualTo(null);
    Assertions.assertThat(SqlBindTypeConverter.convert("", Types.VARCHAR)).isEqualTo("");
    Assertions.assertThat(SqlBindTypeConverter.convert(new BigDecimal("123.45"), Types.VARCHAR))
        .isEqualTo("123.45");
    Assertions.assertThat(SqlBindTypeConverter.convert("123.45", Types.VARCHAR))
        .isEqualTo("123.45");
    Assertions.assertThat(SqlBindTypeConverter.convert(123.45d, Types.VARCHAR)).isEqualTo("123.45");
  }

  @Test
  public void testBoolean() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.BOOLEAN)).isEqualTo(null);
    Assertions.assertThat(SqlBindTypeConverter.convert("", Types.BOOLEAN)).isEqualTo(null);

    Assertions.assertThat(SqlBindTypeConverter.convert("true", Types.BOOLEAN)).isEqualTo(true);
    Assertions.assertThat(SqlBindTypeConverter.convert("false", Types.BOOLEAN)).isEqualTo(false);
    Assertions.assertThat(SqlBindTypeConverter.convert("TRUE", Types.BOOLEAN)).isEqualTo(true);
    Assertions.assertThat(SqlBindTypeConverter.convert("FALSE", Types.BOOLEAN)).isEqualTo(false);
    Assertions.assertThat(SqlBindTypeConverter.convert("T", Types.BOOLEAN)).isEqualTo(true);
    Assertions.assertThat(SqlBindTypeConverter.convert("F", Types.BOOLEAN)).isEqualTo(false);
    Assertions.assertThat(SqlBindTypeConverter.convert("t", Types.BOOLEAN)).isEqualTo(true);
    Assertions.assertThat(SqlBindTypeConverter.convert("f", Types.BOOLEAN)).isEqualTo(false);
    Assertions.assertThat(SqlBindTypeConverter.convert("1", Types.BOOLEAN)).isEqualTo(true);
    Assertions.assertThat(SqlBindTypeConverter.convert("0", Types.BOOLEAN)).isEqualTo(false);
    Assertions.assertThat(SqlBindTypeConverter.convert("Y", Types.BOOLEAN)).isEqualTo(true);
    Assertions.assertThat(SqlBindTypeConverter.convert("N", Types.BOOLEAN)).isEqualTo(false);
    Assertions.assertThat(SqlBindTypeConverter.convert("YES", Types.BOOLEAN)).isEqualTo(true);
    Assertions.assertThat(SqlBindTypeConverter.convert("NO", Types.BOOLEAN)).isEqualTo(false);
  }

  @Test
  public void testInteger() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.INTEGER)).isEqualTo(null);
    Assertions.assertThat(SqlBindTypeConverter.convert(1, Types.INTEGER)).isEqualTo(1);
    Assertions.assertThat(SqlBindTypeConverter.convert("1", Types.INTEGER)).isEqualTo(1L);
    Assertions.assertThat(SqlBindTypeConverter.convert("1.0", Types.INTEGER)).isEqualTo(1L);
    Assertions.assertThat(SqlBindTypeConverter.convert("1.0", Types.INTEGER)).isEqualTo(1L);
  }

  @Test
  public void testDouble() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.DOUBLE)).isEqualTo(null);
    Assertions.assertThat(SqlBindTypeConverter.convert(123.45, Types.DOUBLE)).isEqualTo(123.45);
    Assertions.assertThat(SqlBindTypeConverter.convert("123.45", Types.DOUBLE)).isEqualTo(123.45);
  }

  @Test
  public void testFloat() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.FLOAT)).isEqualTo(null);
    Assertions.assertThat(SqlBindTypeConverter.convert(123.45f, Types.FLOAT)).isEqualTo(123.45f);
    Assertions.assertThat(SqlBindTypeConverter.convert(123.45d, Types.FLOAT)).isEqualTo(123.45f);
    Assertions.assertThat(SqlBindTypeConverter.convert("123.45", Types.FLOAT)).isEqualTo(123.45f);
  }

  @Test
  public void testDecimal() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.DECIMAL)).isEqualTo(null);
    Assertions.assertThat(SqlBindTypeConverter.convert(123.45, Types.DECIMAL))
        .isEqualTo(new BigDecimal("123.45"));
    Assertions.assertThat(SqlBindTypeConverter.convert("123.45", Types.DECIMAL))
        .isEqualTo(new BigDecimal("123.45"));
  }
}
