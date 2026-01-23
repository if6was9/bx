package bx.util;

import bx.sql.duckdb.DuckTable;
import bx.sql.mapper.Mappers;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class TimeSeriesValueTest extends BxTest {

  @Test
  public void testIt() {

    ZonedDateTime zdt = ZonedDateTime.now();

    TimeSeriesValue v = new TimeSeriesValue(zdt.truncatedTo(ChronoUnit.DAYS), 12);

    Assertions.assertThat(v.toString())
        .isEqualTo(String.format("(%s, %s)", zdt.toString().substring(0, 10), 12));
  }

  @Test
  public void testNaN() {

    TimeSeriesValue v = new TimeSeriesValue(ZonedDateTime.now(), Double.NaN);
    Assertions.assertThat(v.getValue().get()).isEqualTo(Double.NaN);
  }

  @Test
  public void testSort() {
    List<TimeSeriesValue> list = Lists.newArrayList();

    for (int i = 0; i < 100; i++) {
      TimeSeriesValue v = new TimeSeriesValue(ZonedDateTime.now(), i);
      list.add(v);
      Sleep.sleepMillis(2);
    }

    Collections.shuffle(list);
    Collections.sort(list);

    for (int i = 0; i < list.size(); i++) {

      System.out.println(list.get(i));
      if (i > 1) {
        Assertions.assertThat(
                list.get(i).getZonedDateTime().isAfter(list.get(i - 1).getZonedDateTime()))
            .isTrue();
        Assertions.assertThat(
                list.get(i).getZonedDateTime().compareTo(list.get(i - 1).getZonedDateTime()))
            .isGreaterThan(0);
      }
    }
  }

  @Test
  public void testX() {
    var zdt = ZonedDateTime.of(2026, 1, 23, 0, 0, 0, 0, Zones.UTC);

    Assertions.assertThat(new TimeSeriesValue(zdt, 0).getLocalString()).isEqualTo("2026-01-23");
    Assertions.assertThat(new TimeSeriesValue(zdt.plusHours(1), 0).getLocalString())
        .isEqualTo("2026-01-23T01:00");
  }

  @Test
  public void testY() {
    // UTC: 2026-01-23T05:18:33Z
    // SFO: 2026-01-22T21:18:33-08:00
    long epochSecond = 1769145513;

    Assertions.assertThat(
            new TimeSeriesValue(Instant.ofEpochSecond(epochSecond), 10).getLocalDate().toString())
        .isEqualTo("2026-01-23");
    Assertions.assertThat(
            new TimeSeriesValue(Instant.ofEpochSecond(epochSecond).atZone(Zones.LAX), 10)
                .getLocalDate()
                .toString())
        .isEqualTo("2026-01-22");

    Assertions.assertThat(
            new TimeSeriesValue(Instant.ofEpochSecond(epochSecond), 10).getLocalString())
        .isEqualTo("2026-01-23T05:18:33");
    Assertions.assertThat(
            new TimeSeriesValue(Instant.ofEpochSecond(epochSecond).atZone(Zones.LAX), 10)
                .getLocalString())
        .isEqualTo("2026-01-22T21:18:33");
  }

  @Test
  public void testMapDate() {
    DuckTable t = DuckTable.of(getDataSource(), "test");
    String csv =
        """
        date,val
        2026-01-03,10
        2026-01-04,11
        2026-01-05,12
        """;
    t.csvImport().fromString(csv).importData();

    t.sql("select date,val from test")
        .query(Mappers.timeSeriesMapper(1, 2))
        .list()
        .forEach(
            it -> {
              System.out.println(it);
            });
  }

  @Test
  public void testMapDateTime() {
    DuckTable t = DuckTable.of(getDataSource(), "test");
    String csv =
        """
        date,val
        2026-01-03 10:00:00,10
        2026-01-04 11:00:00,11
        2026-01-05 12:00:00,12
        """;
    t.csvImport().fromString(csv).importData();

    t.sql("select date,val from test")
        .query(Mappers.timeSeriesMapper(1, 2))
        .list()
        .forEach(
            it -> {
              System.out.println(it + " " + it.getZonedDateTime());
            });

    System.out.println(t.getCreateTableSql());
  }

  @Test
  public void testMapDateTimeWithZone() {
    DuckTable t = DuckTable.of(getDataSource(), "test");
    String csv =
        """
        date,val
        2026-01-03 10:00:00Z,10
        2026-01-04 11:00:00Z,11
        2026-01-05 12:00:00Z,12
        """;
    t.csvImport().fromString(csv).importData();

    t.sql("select date,val from test")
        .query(Mappers.timeSeriesMapper(1, 2))
        .list()
        .forEach(
            it -> {
              System.out.println(it + " " + it.getZonedDateTime());
            });

    System.out.println(t.getCreateTableSql());
  }
}
