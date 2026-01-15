package bx.sql;

import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
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

  @Test
  public void testDate() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.DATE)).isNull();
    Assertions.assertThat(SqlBindTypeConverter.convert("", Types.DATE)).isNull();
    Assertions.assertThat(SqlBindTypeConverter.convert("2026-01-03", Types.DATE))
        .isEqualTo(java.sql.Date.valueOf("2026-01-03"));
    Assertions.assertThat(SqlBindTypeConverter.convert("20260103", Types.DATE))
        .isEqualTo(java.sql.Date.valueOf("2026-01-03"));
    Assertions.assertThat(SqlBindTypeConverter.convert("2026-01-15T18:47:42.912649Z", Types.DATE))
        .isEqualTo(java.sql.Date.valueOf("2026-01-15"));
    Assertions.assertThat(
            SqlBindTypeConverter.convert(
                "2026-01-15T10:46:43.931357-08:00[America/Los_Angeles]", Types.DATE))
        .isEqualTo(java.sql.Date.valueOf("2026-01-15"));
    Assertions.assertThat(SqlBindTypeConverter.convert("2026-01-15T18:47:42Z", Types.DATE))
        .isEqualTo(java.sql.Date.valueOf("2026-01-15"));
    Assertions.assertThat(
            SqlBindTypeConverter.convert(
                "2026-01-15T10:46:43-08:00[America/Los_Angeles]", Types.DATE))
        .isEqualTo(java.sql.Date.valueOf("2026-01-15"));
  }

  @Test
  public void testTime() {

    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.TIME)).isNull();
    Assertions.assertThat(SqlBindTypeConverter.convert("", Types.DATE)).isNull();

    Assertions.assertThat(SqlBindTypeConverter.convert("2026-01-15T18:47:42.912649Z", Types.TIME))
        .isEqualTo(java.sql.Time.valueOf("18:47:42"));
    Assertions.assertThat(
            SqlBindTypeConverter.convert(
                "2026-01-15T10:46:43.931357-08:00[America/Los_Angeles]", Types.TIME))
        .isEqualTo(java.sql.Time.valueOf("10:46:43"));
    Assertions.assertThat(SqlBindTypeConverter.convert("2026-01-15T18:47:42Z", Types.TIME))
        .isEqualTo(java.sql.Time.valueOf("18:47:42"));
    Assertions.assertThat(
            SqlBindTypeConverter.convert(
                "2026-01-15T10:46:43-08:00[America/Los_Angeles]", Types.TIME))
        .isEqualTo(java.sql.Time.valueOf("10:46:43"));
  }

  @Test
  public void testTimeWithZone() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.TIME_WITH_TIMEZONE)).isNull();
    Assertions.assertThat(SqlBindTypeConverter.convert("", Types.DATE)).isNull();

    // do not really care about this.  time with zone is not commonly useful
  }

  @Test
  public void testTimetamp() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.TIMESTAMP)).isNull();
    Assertions.assertThat(SqlBindTypeConverter.convert("", Types.TIMESTAMP)).isNull();

    Assertions.assertThat(SqlBindTypeConverter.convert("2026-01-15T18:47:42Z", Types.TIMESTAMP))
        .isEqualTo(java.sql.Timestamp.valueOf("2026-01-15 18:47:42"));
    Assertions.assertThat(
            SqlBindTypeConverter.convert("2026-01-15T18:47:42-07:00", Types.TIMESTAMP))
        .isEqualTo(java.sql.Timestamp.valueOf("2026-01-15 18:47:42"));
    Assertions.assertThat(
            SqlBindTypeConverter.convert(
                "2026-01-15T18:47:42-08:00[America/Los_Angeles]", Types.TIMESTAMP))
        .isEqualTo(java.sql.Timestamp.valueOf("2026-01-15 18:47:42"));

    Assertions.assertThat(SqlBindTypeConverter.convert("2026-01-15T18:47:42.001Z", Types.TIMESTAMP))
        .isEqualTo(java.sql.Timestamp.valueOf("2026-01-15 18:47:42.001"));
    Assertions.assertThat(
            SqlBindTypeConverter.convert("2026-01-15T18:47:42.002-07:00", Types.TIMESTAMP))
        .isEqualTo(java.sql.Timestamp.valueOf("2026-01-15 18:47:42.002"));
    Assertions.assertThat(
            SqlBindTypeConverter.convert(
                "2026-01-15T18:47:42.004-08:00[America/Los_Angeles]", Types.TIMESTAMP))
        .isEqualTo(java.sql.Timestamp.valueOf("2026-01-15 18:47:42.004"));
  }

  @Test
  public void testTimetampWithZone() {
    Assertions.assertThat(SqlBindTypeConverter.convert(null, Types.TIMESTAMP_WITH_TIMEZONE))
        .isNull();
    Assertions.assertThat(SqlBindTypeConverter.convert("", Types.TIMESTAMP_WITH_TIMEZONE)).isNull();

    OffsetDateTime expected =
        OffsetDateTime.of(LocalDateTime.of(2026, 1, 15, 18, 47, 42), ZoneOffset.of("-08"));

    Assertions.assertThat(
            SqlBindTypeConverter.convert(
                "2026-01-15T18:47:42-08:00", Types.TIMESTAMP_WITH_TIMEZONE))
        .isEqualTo(expected);
    Assertions.assertThat(
            SqlBindTypeConverter.convert(
                "2026-01-15T18:47:42-08:00", Types.TIMESTAMP_WITH_TIMEZONE))
        .isEqualTo(expected);
    Assertions.assertThat(
            SqlBindTypeConverter.convert(
                "2026-01-15T18:47:42-08:00[America/Los_Angeles]", Types.TIMESTAMP_WITH_TIMEZONE))
        .isEqualTo(expected);
  }
}
