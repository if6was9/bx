package bx.sql;

import bx.util.BxTest;
import bx.util.Dates;
import bx.util.Slogger;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.dao.DataAccessException;

public class ResultsTest extends BxTest {

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testIt() {

    db().sql("create table test (int_col int, string_col varchar, double_col double)").update();

    db().sql("insert into test (int_col, string_col, double_col) values (?,?,?)")
        .param(0)
        .param("test")
        .param(3.14)
        .update();
    db().sql("insert into test (int_col, string_col, double_col) values (?,?,?)")
        .param(1)
        .param(null)
        .param(null)
        .update();
    db().sql("insert into test (int_col, string_col, double_col) values (?,?,?)")
        .param(null)
        .param("foo")
        .param(null)
        .update();

    db().sql("select * from test where int_col=1")
        .query(
            rse -> {
              Results rs = Results.create(rse);
              Assertions.assertThat(rs.next()).isTrue();
              Assertions.assertThat(rs.getDouble("double_col")).isEmpty();
              Assertions.assertThat(rs.getString(2).isEmpty());
              Assertions.assertThat(rs.getString("string_col").isEmpty());
              Assertions.assertThat(rs.getDouble(1).get()).isEqualTo(1.0);
              Assertions.assertThat(rs.getDouble("int_col").get()).isEqualTo(1.0);
              Assertions.assertThat(rs.getString("int_col").get()).isEqualTo("1");
              Assertions.assertThat(rs.getLong(1).get()).isEqualTo(1L);
              Assertions.assertThat(rs.getBigDecimal(1).get()).isEqualTo(new BigDecimal(1.0));
              Assertions.assertThat(rs.next()).isFalse();

              return 1;
            });

    db().sql("select cast(null as double) as d")
        .query(
            rse -> {
              Results rs = Results.create(rse);
              rs.next();
              Assertions.assertThat(rse.getDouble(1)).isNaN();
              Assertions.assertThat(rse.getString(1)).isNull();
              Assertions.assertThat(rs.getDouble(1)).isEmpty();
              Assertions.assertThat(rs.getDouble("d")).isEmpty();
              Assertions.assertThat(rs.getInt("d")).isEmpty();
              Assertions.assertThat(rs.getString("d")).isEmpty();
              Assertions.assertThat(rs.getBigDecimal("d")).isEmpty();
              return 1;
            });
  }

  @Test
  public void testDouble() {
    db().sql("select cast(null as double) as d")
        .query(
            rse -> {
              Results rs = Results.create(rse);
              rs.next();
              Assertions.assertThat(rse.getDouble(1)).isNaN();
              Assertions.assertThat(rse.getString(1)).isNull();
              Assertions.assertThat(rs.getDouble(1)).isEmpty();
              Assertions.assertThat(rs.getDouble("d")).isEmpty();
              Assertions.assertThat(rs.getInt("d")).isEmpty();
              Assertions.assertThat(rs.getString("d")).isEmpty();
              Assertions.assertThat(rs.getBigDecimal("d")).isEmpty();
              Assertions.assertThat(rs.next()).isFalse();
              return 1;
            });
    db().sql("select 3.1415 as d")
        .query(
            rse -> {
              Results rs = Results.create(rse);
              rs.next();
              Assertions.assertThat(rse.getDouble(1)).isEqualTo(3.1415);
              Assertions.assertThat(rse.getString(1)).isEqualTo("3.1415");
              Assertions.assertThat(rs.getDouble(1).get()).isEqualTo(3.1415);
              Assertions.assertThat(rs.getDouble("d").get()).isEqualTo(3.1415);
              Assertions.assertThat(rs.getInt("d").get()).isEqualTo(3);
              Assertions.assertThat(rs.getString("d").get()).isEqualTo("3.1415");
              Assertions.assertThat(rs.getBigDecimal("d").get()).isEqualByComparingTo("3.1415");
              Assertions.assertThat(rs.next()).isFalse();
              return 1;
            });
  }

  @Test
  public void testBoolean() {
    db().sql("select cast(true as boolean) as b")
        .query(
            rse -> {
              Assertions.assertThat(rse.next()).isTrue();
              Results rs = Results.create(rse);
              Assertions.assertThat(rs.getBoolean("b").get()).isTrue();

              return 1;
            });

    db().sql("select cast(false as boolean) as b")
        .query(
            rse -> {
              Assertions.assertThat(rse.next()).isTrue();
              Results rs = Results.create(rse);
              Assertions.assertThat(rs.getBoolean("b").get()).isFalse();
              return 1;
            });

    db().sql("select cast(null as boolean) as b")
        .query(
            rse -> {
              Assertions.assertThat(rse.next()).isTrue();
              Results rs = Results.create(rse);
              Assertions.assertThat(rs.getBoolean("b")).isEmpty();
              return 1;
            });
  }

  void checkTimestampMatch(String col, Results results, ResultSet rs) throws SQLException {

    Exception resultSetException = null;
    Throwable resultsException = null;
    try {
      Timestamp rts = rs.getTimestamp(col);

    } catch (SQLException e) {
      resultSetException = e;
    }

    try {
      results.getTimestamp(col);
    } catch (DbException e) {
      resultsException = e.getCause();
    }

    if (resultSetException == null) {
      Assertions.assertThat(resultsException).isNull();
    } else {
      Assertions.assertThat(resultsException.getClass()).isEqualTo(resultSetException.getClass());
    }
    if (resultsException == null) {
      Assertions.assertThat(resultSetException).isNull();
    } else {
      Assertions.assertThat(resultSetException.getClass()).isEqualTo(resultsException.getClass());
    }

    Assertions.assertThat(results.getTimestamp(col).orElse(null)).isEqualTo(rs.getTimestamp(col));
  }

  void checkExceptionsMatch(
      Throwable resultsException, Throwable resultSetException, boolean strictCheck) {
    if (resultSetException == null && resultsException == null) {
      return;
    }

    if (resultSetException == null) {
      Assertions.assertThat(resultsException).isNull();
    }
    if (resultSetException != null) {
      Assertions.assertThat(resultsException).isNotNull();
    }

    if (strictCheck == false) {
      return;
    }

    if (resultsException instanceof DbException) {
      Assertions.assertThat(((DbException) resultsException).getCause().getClass())
          .isEqualTo(resultSetException.getClass());
    }
  }

  void checkGetDateMatch(String col, Results results, ResultSet rs) throws SQLException {

    Exception resultSetException = null;
    Throwable resultsException = null;
    try {
      Date d = rs.getDate(col);

    } catch (SQLException e) {
      resultSetException = e;
    }

    try {
      results.getDate(col);
    } catch (DbException e) {
      resultsException = e.getCause();
    }

    if (resultSetException == null) {
      Assertions.assertThat(resultsException).isNull();
    } else {
      Assertions.assertThat(resultsException.getClass()).isEqualTo(resultSetException.getClass());
    }
    if (resultsException == null) {
      Assertions.assertThat(resultSetException).isNull();
    } else {
      Assertions.assertThat(resultSetException.getClass()).isEqualTo(resultsException.getClass());
    }

    Assertions.assertThat(results.getDate(col).orElse(null)).isEqualTo(rs.getDate(col));
  }

  void checkGetStringMatch(String col, Results results, ResultSet rs) throws SQLException {

    Exception resultSetException = null;
    Throwable resultsException = null;
    try {
      rs.getString(col);

    } catch (SQLException e) {
      resultSetException = e;
    }

    try {
      results.getString(col);
    } catch (DbException e) {
      resultsException = e.getCause();
    }

    if (resultSetException == null) {
      Assertions.assertThat(resultsException).isNull();
    } else {
      Assertions.assertThat(resultsException.getClass()).isEqualTo(resultSetException.getClass());
    }
    if (resultsException == null) {
      Assertions.assertThat(resultSetException).isNull();
    } else {
      Assertions.assertThat(resultSetException.getClass()).isEqualTo(resultsException.getClass());
    }

    if (resultsException != null || resultSetException != null) {
      return;
    }

    Assertions.assertThat(results.getString(col).orElse(null)).isEqualTo(rs.getString(col));
  }

  void checkGetTimeMatch(String col, Results results, ResultSet rs) throws SQLException {

    Exception resultSetException = null;
    Throwable resultsException = null;
    try {
      rs.getTime(col);

    } catch (Exception e) {
      resultSetException = e;
    }

    try {
      results.getTime(col);
    } catch (DbException e) {
      resultsException = e.getCause();
    } catch (Throwable e) {
      resultsException = e;
    }

    if (resultSetException == null) {
      Assertions.assertThat(resultsException).isNull();
    } else {
      Assertions.assertThat(resultsException.getClass()).isEqualTo(resultSetException.getClass());
    }
    if (resultsException == null) {
      Assertions.assertThat(resultSetException).isNull();
    } else {
      Assertions.assertThat(resultSetException.getClass()).isEqualTo(resultsException.getClass());
    }

    if (resultsException != null || resultSetException != null) {
      return;
    }

    Assertions.assertThat(results.getTime(col).orElse(null)).isEqualTo(rs.getTime(col));
  }

  void checkGetLongMatch(String col, Results results, ResultSet rs) throws SQLException {
    checkGetLongMatch(col, results, rs, true);
  }

  void checkGetLongMatch(String col, Results results, ResultSet rs, boolean strictExceptonCheck)
      throws SQLException {

    Throwable resultSetException = null;
    Throwable resultsException = null;
    try {
      rs.getLong(col);

    } catch (SQLException e) {
      resultSetException = e;
    } catch (Throwable e) {
      resultSetException = e;
    }

    try {
      results.getLong(col);
    } catch (DbException e) {
      resultsException = e.getCause();
    } catch (Throwable e) {
      resultsException = e;
    }

    checkExceptionsMatch(resultsException, resultSetException, strictExceptonCheck);

    if (resultsException != null || resultSetException != null) {
      return;
    }

    Long rsLong = rs.getLong(col);
    if (rs.wasNull()) {
      rsLong = null;
    }

    Assertions.assertThat(results.getLong(col).orElse(null)).isEqualTo(rsLong);
  }

  @Test
  public void testTimestampWithZone9() throws InterruptedException {

    CountDownLatch latch = new CountDownLatch(1);
    db().sql(
            "SELECT CAST('2023-10-27 10:30:00Z' AS TIMESTAMPTZ) as ts, cast(null as TIMESTAMPTZ) as"
                + " null_ts")
        .query(
            rse -> {
              long expectedEpochSecond = 1698402600L;
              Results rs = Results.create(rse);
              Assertions.assertThat(rs.getInstant("ts").get().getEpochSecond())
                  .isEqualTo(1698402600L);

              Assertions.assertThat(Instant.ofEpochSecond(1698402600L).toString())
                  .isEqualTo("2023-10-27T10:30:00Z");

              // This is where things get squirrelly.  getObject() will return an OffsetDateTime
              // that is correct but
              // it doesn't necessarily preserve the timezone that was set.  At least here it seems
              // to return a value in the current
              // session's TZ
              OffsetDateTime odt = (OffsetDateTime) rse.getObject("ts");
              Assertions.assertThat(odt.toInstant().getEpochSecond()).isEqualTo(1698402600L);

              Assertions.assertThat(rs.getOffsetDateTime("ts").get().toInstant().getEpochSecond())
                  .isEqualTo(1698402600L);

              checkTimestampMatch("ts", rs, rse);
              checkGetDateMatch("ts", rs, rse);
              checkGetTimeMatch("ts", rs, rse);
              checkGetLongMatch("ts", rs, rse);
              checkGetStringMatch("ts", rs, rse);

              Assertions.assertThat(rse.getDate("ts")).isEqualTo(rs.getDate("ts").orElse(null));
              Assertions.assertThat(rse.getTime("ts")).isEqualTo(rs.getTime("ts").orElse(null));
              Assertions.assertThat(rse.getTimestamp("ts").toInstant().getEpochSecond())
                  .isEqualTo(expectedEpochSecond);

              Assertions.assertThat(rs.getTimestamp("ts").get().toInstant().getEpochSecond())
                  .isEqualTo(expectedEpochSecond);

              Assertions.assertThat(rse.getLong("ts")).isEqualTo(1698402600000000L);
              Assertions.assertThat(rse.wasNull()).isFalse();
              Assertions.assertThat(rs.getLong("ts").get()).isEqualTo(rse.getLong("ts"));

              Assertions.assertThat(rse.getObject("null_ts")).isNull();
              Assertions.assertThat(rs.getInstant("null_ts")).isEmpty();

              Assertions.assertThat(odt.toEpochSecond())
                  .isEqualTo(rs.getInstant("ts").get().getEpochSecond());

              latch.countDown();
            });

    // this just makes sure that the checks actually executed
    Assertions.assertThat(latch.await(5, TimeUnit.SECONDS))
        .withFailMessage("code should have executed")
        .isTrue();
  }

  @Test
  public void testTimestamp() throws InterruptedException {

    long epochSecond = 1698427800L;
    Instant instant = Instant.ofEpochSecond(epochSecond);
    String tsString = "2023-10-27T17:30:00";

    // make sure the above all align
    Assertions.assertThat(Dates.asInstant(tsString + "Z").get().getEpochSecond())
        .isEqualTo(epochSecond);

    CountDownLatch latch = new CountDownLatch(1);
    db().sql(
            "SELECT CAST('2023-10-27 10:30:00' AS TIMESTAMP) as ts, cast(null as TIMESTAMP) as"
                + " null_ts")
        .query(
            rs -> {
              Results results = Results.create(rs);

              Instant t0 = results.getInstant("ts").get();

              System.out.println(t0);

              Assertions.assertThat(rs.getObject("ts")).isInstanceOf(java.sql.Timestamp.class);

              Timestamp ts = (Timestamp) rs.getObject("ts");

              checkGetLongMatch("ts", results, rs);
              checkGetStringMatch("ts", results, rs);
              checkGetTimeMatch("ts", results, rs);
              checkGetDateMatch("ts", results, rs);
              checkGetTimeMatch("ts", results, rs);

              checkGetLongMatch("null_ts", results, rs);
              checkGetStringMatch("null_ts", results, rs);
              checkGetTimeMatch("null_ts", results, rs);
              checkGetDateMatch("null_ts", results, rs);
              checkGetTimeMatch("null_ts", results, rs);

              latch.countDown();
            });

    // this just makes sure that the checks actually executed
    Assertions.assertThat(latch.await(5, TimeUnit.SECONDS))
        .withFailMessage("code should have executed")
        .isTrue();

    sql("select cast('2025-12-25T10:00:00Z' as TIMESTAMPTZ) as ts")
        .query(
            rs -> {
              Results results = Results.create(rs);

              Assertions.assertThat(results.getOffsetDateTime(1).get())
                  .isEqualTo(rs.getObject(1))
                  .isInstanceOf(OffsetDateTime.class);
              Assertions.assertThat(results.getZonedDateTime(1).get().toInstant())
                  .isEqualTo(((OffsetDateTime) rs.getObject(1)).toInstant());
            });
  }

  @Test
  public void testDate() throws InterruptedException {

    CountDownLatch latch = new CountDownLatch(1);
    db().sql("SELECT CAST('2023-10-27' AS DATE) as dt, cast(null as DATE) as null_dt")
        .query(
            rs -> {
              Results results = Results.create(rs);

              Assertions.assertThat(rs.getObject("dt")).isInstanceOf(LocalDate.class);

              checkGetLongMatch("dt", results, rs);
              checkGetStringMatch("dt", results, rs);
              checkGetTimeMatch("dt", results, rs);
              checkGetDateMatch("dt", results, rs);
              checkGetTimeMatch("dt", results, rs);

              checkGetLongMatch("null_dt", results, rs);
              checkGetStringMatch("null_dt", results, rs);
              checkGetTimeMatch("null_dt", results, rs);
              checkGetDateMatch("null_dt", results, rs);
              checkGetTimeMatch("null_dt", results, rs);

              latch.countDown();
            });

    // this just makes sure that the checks actually executed
    Assertions.assertThat(latch.await(5, TimeUnit.SECONDS))
        .withFailMessage("code should have executed")
        .isTrue();
  }

  @Test
  public void testInvalidColumn() {
    db().sql("select 1 as foo")
        .query(
            rs -> {
              Results results = Results.create(rs);
              expect(
                  DataAccessException.class,
                  () -> {
                    results.getString(0);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getString(2);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getString("invalid");
                  });
              expect(
                  DataAccessException.class,
                  () -> {
                    results.getInt(0);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getInt(2);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getInt("invalid");
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getTimestamp(0);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getTimestamp(2);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getTimestamp("invalid");
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getOffsetDateTime(0);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getOffsetDateTime(2);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getOffsetDateTime("invalid");
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getZonedDateTime(0);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getZonedDateTime(2);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getZonedDateTime("invalid");
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getBoolean(0);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getBoolean(2);
                  });

              expect(
                  DataAccessException.class,
                  () -> {
                    results.getBoolean("invalid");
                  });
            });
  }

  @Test
  public void testTimestampTz() {

    String ts = "2025-12-25T03:00:00Z";
    long epochMilli = Dates.asInstant(ts).get().toEpochMilli();

    db().sql(String.format("select cast('%s' as TIMESTAMPTZ) as ts", ts))
        .query(
            rs -> {
              Assertions.assertThat(rs.getTimestamp(1).toInstant().toEpochMilli())
                  .isEqualTo(epochMilli);
              Results results = Results.create(rs);

              // the actual LocalDate values are dependent on the server TZ!!!
              Assertions.assertThat(results.getLocalDate(1).get().toString())
                  .isEqualTo(rs.getDate(1).toLocalDate().toString());
            });
  }

  @Test
  public void testTimestampWithoutZone() {

    String ts = "2025-12-25T03:00:00";

    db().sql(String.format("select cast('%s' as TIMESTAMP) as ts", ts))
        .query(
            rs -> {
              Results results = Results.create(rs);

              // the actual LocalDate values are dependent on the server TZ!!!
              Assertions.assertThat(results.getLocalDate(1).get().toString())
                  .isEqualTo(rs.getDate(1).toLocalDate().toString());

              logger.atInfo().log("ResultSet.getDate(): {}", rs.getDate(1));
              logger.atInfo().log("ResultSet.getTimestamp(): {}", rs.getTimestamp(1));

              // Timestamp.toInstant() will assume that the date in Timestamp is for the LOCAL ZONE
              // On my machine set to America/Los_Angeles, this means taht the instant will be set
              // to 2025-12-25T11:00:00Z
              logger.atInfo().log(
                  "ResultSet.getTimestamp().toInstant(): {}", rs.getTimestamp(1).toInstant());

              // the un-zoned date/time string will be handled using the system's timezone
              Assertions.assertThat(rs.getTimestamp(1).toInstant())
                  .isEqualTo(Dates.asInstant(ts, ZoneId.systemDefault()).get());

              Assertions.assertThat(rs.getDate(1).toString()).isEqualTo("2025-12-25");
              Assertions.assertThat(rs.getString(1)).isEqualTo("2025-12-25 03:00:00.0");
              // now start tests for Results interface:

              Assertions.assertThat(results.getLocalDate(1).get())
                  .isEqualTo(rs.getTimestamp(1).toLocalDateTime().toLocalDate());
              Assertions.assertThat(results.getInstant(1).get())
                  .isEqualTo(rs.getTimestamp(1).toInstant());

              Assertions.assertThat(results.getZonedDateTime(1).get().toInstant())
                  .isEqualTo(rs.getTimestamp(1).toInstant());

              Assertions.assertThat(results.getOffsetDateTime(1).get().toInstant())
                  .isEqualTo(rs.getTimestamp(1).toInstant());
            });
  }

  @Test
  public void testNow() {
    db().sql(String.format("select cast(now() as TIMESTAMPTZ) as ts"))
        .query(
            rs -> {
              System.out.println(rs.getString(1));
            });
  }

  @Test
  public void testTimestampWithZone() {

    String ts = "2025-12-25T03:00:00Z";
    long epochMilli = Dates.asInstant(ts).get().toEpochMilli();
    Assertions.assertThat(epochMilli).isEqualTo(1766631600000L);

    db().sql(String.format("select cast('%s' as TIMESTAMPTZ) as ts", ts))
        .query(
            rs -> {
              Results results = Results.create(rs);

              // the actual LocalDate values are dependent on the server TZ!!!
              Assertions.assertThat(results.getLocalDate(1).get().toString())
                  .isEqualTo(rs.getDate(1).toLocalDate().toString());

              // These will come back in local zone.  On my machine in America/Los_Angeles
              logger.atInfo().log("ResultSet.getDate(): {}", rs.getDate(1));
              logger.atInfo().log(
                  "ResultSet.getTimestamp(): {}", rs.getTimestamp(1)); // 2025-12-24 19:00:00.0

              // Timestamp.toInstant() will assume that the date in Timestamp is for the LOCAL ZONE
              // On my machine set to America/Los_Angeles, this means taht the instant will be set
              // to 2025-12-25T11:00:00Z
              logger.atInfo().log(
                  "ResultSet.getTimestamp().toInstant(): {}", rs.getTimestamp(1).toInstant());

              Assertions.assertThat(results.getInstant("ts").get().toEpochMilli())
                  .isEqualTo(epochMilli);
              Assertions.assertThat(results.getZonedDateTime(1).get().toInstant().toString())
                  .isEqualTo(ts);
            });
  }
}
