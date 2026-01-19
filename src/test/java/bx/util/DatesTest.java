package bx.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Random;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class DatesTest {

  @Test
  public void testIt() {
    ZonedDateTime dt = ZonedDateTime.now();

    Assertions.assertThat(Dates.asZonedDateTime(dt.toString()).get().toEpochSecond())
        .isEqualTo(dt.toEpochSecond());
  }

  @Test
  public void testX() {
    Assertions.assertThat(Dates.asZonedDateTime((String) null)).isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime("")).isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime("   ")).isEmpty();

    long refDt = 1733809243L;
    Assertions.assertThat(
            Dates.asZonedDateTime("2024-12-09T21:40:43.349104-08:00[America/Los_Angeles]")
                .get()
                .toInstant()
                .toEpochMilli())
        .isEqualTo(1733809243349L);
    Assertions.assertThat(
            Dates.asZonedDateTime("2024-12-09T21:40:43-08:00[America/Los_Angeles]")
                .get()
                .toInstant()
                .toEpochMilli())
        .isEqualTo(1733809243000L);
    Assertions.assertThat(
            Dates.asZonedDateTime("2024-12-09 21:40:43-08:00[America/Los_Angeles]")
                .get()
                .toInstant()
                .toEpochMilli())
        .isEqualTo(1733809243000L);
    Assertions.assertThat(
            Dates.asZonedDateTime("2024-12-09T21:40:43-08:00[America/Los_Angeles]")
                .get()
                .toEpochSecond())
        .isEqualTo(refDt);

    Assertions.assertThat(Dates.asZonedDateTime("2024-12-10T05:40:43Z[UTC]").get().toEpochSecond())
        .isEqualTo(refDt);
    Assertions.assertThat(Dates.asZonedDateTime("2024-12-10T05:40:43Z").get().toEpochSecond())
        .isEqualTo(refDt);

    Assertions.assertThat(Dates.asZonedDateTime("2024-12-10T05:40:43")).isEmpty();

    Assertions.assertThat(Dates.asLocalDate(null)).isEmpty();
    Assertions.assertThat(Dates.asLocalDate("")).isEmpty();
    Assertions.assertThat(Dates.asLocalDate("2024-12-10").get().getYear()).isEqualTo(2024);
    Assertions.assertThat(Dates.asLocalDate("2024-12-10").get().getMonthValue()).isEqualTo(12);
    Assertions.assertThat(Dates.asLocalDate("2024-12-10").get().getDayOfMonth()).isEqualTo(10);
    Assertions.assertThat(Dates.asLocalDate("20241210").get().getYear()).isEqualTo(2024);
    Assertions.assertThat(Dates.asLocalDate("20241210").get().getMonthValue()).isEqualTo(12);
    Assertions.assertThat(Dates.asLocalDate("20241210").get().getDayOfMonth()).isEqualTo(10);
    Assertions.assertThat(Dates.asLocalDate("2024/12/10").get().getYear()).isEqualTo(2024);
    Assertions.assertThat(Dates.asLocalDate("2024/12/10").get().getMonthValue()).isEqualTo(12);
    Assertions.assertThat(Dates.asLocalDate("2024/12/10").get().getDayOfMonth()).isEqualTo(10);

    Assertions.assertThat(Dates.asLocalDate("2024-12-10T05:40:43Z").get().toString())
        .isEqualTo("2024-12-10");
    Assertions.assertThat(Dates.asLocalDate("2024-12-10T05:40:43-05").get().toString())
        .isEqualTo("2024-12-10");
  }

  @Test
  public void testXX() {
    LocalDate d = LocalDate.of(2025, 5, 31);

    Assertions.assertThat(Dates.asZonedDateTime(d.toString())).isEmpty();

    Assertions.assertThat(Dates.asInstant(d.toString())).isEmpty();

    Assertions.assertThat(Dates.asLocalDate("2025/05/31").get().toString()).isEqualTo("2025-05-31");
    Assertions.assertThat(Dates.asLocalDate("20250531").get().toString()).isEqualTo("2025-05-31");
  }

  @Test
  public void testAsLocalDate() {

    long ts = 1738543678283L;
    Instant t = Instant.ofEpochMilli(ts);
    ZonedDateTime zdt = ZonedDateTime.ofInstant(t, Zones.UTC);
    LocalDate ld = LocalDate.of(2025, 2, 3);

    Assertions.assertThat(Dates.asLocalDate(zdt.toString()).get()).isEqualTo(ld.toString());
    Assertions.assertThat(Dates.asLocalDate(t.toString()).get()).isEqualTo(ld.toString());
  }

  @Test
  public void testAsLocalTime() {
    Assertions.assertThat(Dates.asLocalTime("")).isEmpty();
    Assertions.assertThat(Dates.asLocalTime((TemporalAccessor) null)).isEmpty();
    Assertions.assertThat(Dates.asLocalTime("13:12:22").get()).isEqualTo(LocalTime.of(13, 12, 22));
    Assertions.assertThat(Dates.asLocalTime("13:12").get()).isEqualTo(LocalTime.of(13, 12, 0));
    Assertions.assertThat(Dates.asLocalTime("13:12:22.2").get())
        .isEqualTo(LocalTime.of(13, 12, 22, 200000000));
    Assertions.assertThat(Dates.asLocalTime("2026-03-01T13:12:22").get())
        .isEqualTo(LocalTime.of(13, 12, 22));
    Assertions.assertThat(Dates.asLocalTime("2026-03-01T13:12:22Z").get())
        .isEqualTo(LocalTime.of(13, 12, 22));
    Assertions.assertThat(Dates.asLocalTime("2026-03-01T13:12:22+04:00").get())
        .isEqualTo(LocalTime.of(13, 12, 22));
    Assertions.assertThat(Dates.asLocalTime("2026-03-01T13:12:22-04:00").get())
        .isEqualTo(LocalTime.of(13, 12, 22));
  }

  @Test
  public void testAsLocalDateTime() {
    Assertions.assertThat(Dates.asLocalDateTime("")).isEmpty();
    Assertions.assertThat(Dates.asLocalDateTime((TemporalAccessor) null)).isEmpty();
    Assertions.assertThat(Dates.asLocalDateTime("13:12:22")).isEmpty();
    Assertions.assertThat(Dates.asLocalDateTime("13:12")).isEmpty();
    Assertions.assertThat(Dates.asLocalDateTime("13:12:22.2")).isEmpty();
    Assertions.assertThat(Dates.asLocalDateTime("2026-03-01T13:12:22").get())
        .isEqualTo(LocalDateTime.of(2026, 3, 01, 13, 12, 22));

    Assertions.assertThat(Dates.asLocalDateTime("2026-03-01T13:12:22+04:00").get())
        .isEqualTo(LocalDateTime.of(2026, 3, 1, 13, 12, 22));
    Assertions.assertThat(Dates.asLocalDateTime("2026-03-01T13:12:22-04:00").get())
        .isEqualTo(LocalDateTime.of(2026, 3, 01, 13, 12, 22));
    Assertions.assertThat(Dates.asLocalDateTime("2026-03-01T13:12:22Z").get())
        .isEqualTo(LocalDateTime.of(2026, 3, 1, 13, 12, 22));
  }

  @Test
  public void testAsZonedDateTime() {
    Assertions.assertThat(Dates.asZonedDateTime("")).isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime((TemporalAccessor) null)).isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime("13:12:22")).isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime("13:12")).isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime("13:12:22.2")).isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime("2026-03-01T13:12:22")).isEmpty();

    ZonedDateTime expected = ZonedDateTime.of(2026, 3, 1, 13, 12, 22, 0, ZoneOffset.UTC);

    Assertions.assertThat(Dates.asZonedDateTime("2026-03-01T13:12:22Z").get()).isEqualTo(expected);
    Assertions.assertThat(Dates.asZonedDateTime("2026-03-01T08:12:22-05").get())
        .isEqualTo(expected);
    Assertions.assertThat(Dates.asZonedDateTime("2026-03-01T08:12:22-0500").get())
        .isEqualTo(expected);
    Assertions.assertThat(Dates.asZonedDateTime("2026-03-01T08:12:22-05:00").get())
        .isEqualTo(expected);
    Assertions.assertThat(Dates.asZonedDateTime("2026-03-01T08:12:22.000-05:00").get())
        .isEqualTo(expected);
    Assertions.assertThat(Dates.asZonedDateTime("2026-03-01T08:12:22.000000-05:00").get())
        .isEqualTo(expected);

    Assertions.assertThat(Dates.asZonedDateTime("2026-03-01T13:12:22+04:00").get())
        .isEqualTo(ZonedDateTime.of(2026, 3, 1, 13, 12, 22, 0, ZoneOffset.ofHours(4)));
    Assertions.assertThat(Dates.asZonedDateTime("2026-03-01T13:12:22-04:00").get())
        .isEqualTo(ZonedDateTime.of(2026, 3, 1, 13, 12, 22, 0, ZoneOffset.ofHours(-4)));
  }

  @Test
  public void testAsOffsetDateTime() {
    Assertions.assertThat(Dates.asOffsetDateTime("")).isEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime((TemporalAccessor) null)).isEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime("13:12:22")).isEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime("13:12")).isEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime("13:12:22.2")).isEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime("2026-03-01T13:12:22")).isEmpty();

    OffsetDateTime expected = OffsetDateTime.of(2026, 3, 1, 13, 12, 22, 0, ZoneOffset.UTC);

    Assertions.assertThat(Dates.asOffsetDateTime("2026-03-01T13:12:22Z").get()).isEqualTo(expected);
    Assertions.assertThat(Dates.asOffsetDateTime("2026-03-01T13:12:22+04:00").get())
        .isEqualTo(OffsetDateTime.of(2026, 3, 1, 13, 12, 22, 0, ZoneOffset.ofHours(4)));
    Assertions.assertThat(Dates.asOffsetDateTime("2026-03-01T13:12:22-04:00").get())
        .isEqualTo(OffsetDateTime.of(2026, 3, 1, 13, 12, 22, 0, ZoneOffset.ofHours(-4)));
  }

  @Test
  public void testDefaultDuckTimestampOutput() {

    Assertions.assertThat(Dates.asInstant("2009-01-10 14:30:18-08").get().toString())
        .isEqualTo("2009-01-10T22:30:18Z");
    Assertions.assertThat(Dates.asInstant("2009-01-10 14:30:18-08").get().toString())
        .isEqualTo("2009-01-10T22:30:18Z");
    Assertions.assertThat(Dates.asOffsetDateTime("2009-01-10 14:30:18-08").get().toString())
        .isEqualTo("2009-01-10T14:30:18-08:00");
    Assertions.assertThat(Dates.asZonedDateTime("2009-01-10 14:30:18-08").get().toString())
        .isEqualTo("2009-01-10T14:30:18-08:00");
    Assertions.assertThat(Dates.asInstant("2026-01-15 21:11:25.456419-08").get().toString())
        .isEqualTo("2026-01-16T05:11:25.456419Z");
    Assertions.assertThat(Dates.asInstant("2026-01-15 21:11:25.456-08").get().toString())
        .isEqualTo("2026-01-16T05:11:25.456Z");

    Assertions.assertThat(Dates.asInstant("2009-01-10T14:30:18-08").get().toString())
        .isEqualTo("2009-01-10T22:30:18Z");
    Assertions.assertThat(Dates.asInstant("2009-01-10T14:30:18-08").get().toString())
        .isEqualTo("2009-01-10T22:30:18Z");
    Assertions.assertThat(Dates.asOffsetDateTime("2009-01-10T14:30:18-08").get().toString())
        .isEqualTo("2009-01-10T14:30:18-08:00");
    Assertions.assertThat(Dates.asZonedDateTime("2009-01-10T14:30:18-08").get().toString())
        .isEqualTo("2009-01-10T14:30:18-08:00");
    Assertions.assertThat(Dates.asInstant("2026-01-15T21:11:25.456419-08").get().toString())
        .isEqualTo("2026-01-16T05:11:25.456419Z");
    Assertions.assertThat(Dates.asInstant("2026-01-15T21:11:25.456-08").get().toString())
        .isEqualTo("2026-01-16T05:11:25.456Z");
  }

  @Test
  public void testParseEpochSecond() {
    Assertions.assertThat(Dates.parseEpochSecond(null)).isEmpty();
    Assertions.assertThat(Dates.parseEpochSecond("")).isEmpty();
    Assertions.assertThat(Dates.parseEpochSecond("somenonsense")).isEmpty();
    Assertions.assertThat(Dates.parseEpochSecond("1234567890").get().toString())
        .isEqualTo("2009-02-13T23:31:30Z");
  }

  @Test
  public void testParseEpochMilli() {
    Assertions.assertThat(Dates.parseEpochMilli(null)).isEmpty();
    Assertions.assertThat(Dates.parseEpochMilli("")).isEmpty();
    Assertions.assertThat(Dates.parseEpochMilli("somenonsense")).isEmpty();
    Assertions.assertThat(Dates.parseEpochMilli("1234567890123").get().toString())
        .isEqualTo("2009-02-13T23:31:30.123Z");
  }

  @Test
  public void testLocalDateRange() {
    Assertions.assertThat(Dates.range(LocalDate.of(2025, 12, 29), LocalDate.of(2026, 1, 1)))
        .containsExactly(
            LocalDate.of(2025, 12, 29),
            LocalDate.of(2025, 12, 30),
            LocalDate.of(2025, 12, 31),
            LocalDate.of(2026, 1, 1));
  }

  @Test
  public void testParseWithName() {

    Assertions.assertThat(Dates.asInstant("2025-01-15T10:30:00[America/New_York]").get().toString())
        .isEqualTo("2025-01-15T15:30:00Z");
    Assertions.assertThat(Dates.asInstant("2025-01-15 10:30:00[America/New_York]").get().toString())
        .isEqualTo("2025-01-15T15:30:00Z");
  }

  @Test
  public void testAsZonedDateTimeWithSupplement() {
    LocalDateTime ldt = null;
    String localDateTimeString = "2026-01-17T16:12:47.070275";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString))
        .withFailMessage("dates without zone info cannot resolve to a ZonedDateTime")
        .isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.UTC)).isNotEmpty();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.UTC).get())
        .isEqualTo(Dates.asZonedDateTime(localDateTimeString + "Z").get());

    localDateTimeString = "2026-01-17T16:12:47";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString))
        .withFailMessage("dates without zone info cannot resolve to a ZonedDateTime")
        .isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.UTC)).isNotEmpty();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.UTC).get())
        .isEqualTo(Dates.asZonedDateTime(localDateTimeString + "Z").get());

    localDateTimeString = "2026-01-17T16:12:47";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString))
        .withFailMessage("dates without zone info cannot resolve to a ZonedDateTime")
        .isEmpty();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.NYC)).isNotEmpty();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.NYC).get())
        .isEqualTo(Dates.asZonedDateTime(localDateTimeString + "-05:00").get());

    localDateTimeString = "2026-01-17T16:12:47Z";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();

    Assertions.assertThat(ldt.toString()).isEqualTo("2026-01-17T16:12:47");

    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.NYC)).isNotEmpty();
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.NYC).get().getZone())
        .isEqualTo(Zones.NYC);

    Assertions.assertThat(
            Dates.asZonedDateTime("2026-01-17T21:12:47Z").get().toInstant().toString())
        .isEqualTo("2026-01-17T21:12:47Z");

    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.NYC).get().toString())
        .isEqualTo("2026-01-17T11:12:47-05:00[America/New_York]");
    Assertions.assertThat(Dates.asZonedDateTime(localDateTimeString, Zones.NYC).get())
        .isEqualTo(Dates.asZonedDateTime("2026-01-17T16:12:47Z").get());
  }

  @Test
  public void testAsOffsetDateTimeWithSupplement() {
    LocalDateTime ldt = null;
    String localDateTimeString = "2026-01-17T16:12:47.070275";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();
    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString))
        .withFailMessage("dates without zone info cannot resolve to a ZonedDateTime")
        .isEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.UTC)).isNotEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.UTC).get())
        .isEqualTo(Dates.asOffsetDateTime(localDateTimeString + "Z").get());

    localDateTimeString = "2026-01-17T16:12:47";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();
    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString))
        .withFailMessage("dates without zone info cannot resolve to a ZonedDateTime")
        .isEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.UTC)).isNotEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.UTC).get())
        .isEqualTo(Dates.asOffsetDateTime(localDateTimeString + "Z").get());

    localDateTimeString = "2026-01-17T16:12:47";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();
    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString))
        .withFailMessage("dates without zone info cannot resolve to a ZonedDateTime")
        .isEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.ofHours(-5)))
        .isNotEmpty();
    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.ofHours(-5)).get())
        .isEqualTo(Dates.asOffsetDateTime(localDateTimeString + "-05:00").get());

    localDateTimeString = "2026-01-17T16:12:47Z";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();

    Assertions.assertThat(ldt.toString()).isEqualTo("2026-01-17T16:12:47");

    Assertions.assertThat(Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.ofHours(-5)))
        .isNotEmpty();
    Assertions.assertThat(
            Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.ofHours(-5)).get().getOffset())
        .isEqualTo(ZoneOffset.ofHours(-5));

    Assertions.assertThat(
            Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.ofHours(-5)).get().toString())
        .isEqualTo("2026-01-17T11:12:47-05:00");
    Assertions.assertThat(
            Dates.asOffsetDateTime(localDateTimeString, ZoneOffset.ofHours(-5)).get().toInstant())
        .isEqualTo(Dates.asOffsetDateTime("2026-01-17T16:12:47Z").get().toInstant());
  }

  @Test
  public void testAsInstantWithSuplement() {

    LocalDateTime ldt = null;
    String localDateTimeString = "2026-01-17T16:12:47.070275";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();
    Assertions.assertThat(Dates.asInstant(localDateTimeString))
        .withFailMessage("dates without zone info cannot resolve to an instant")
        .isEmpty();
    Assertions.assertThat(Dates.asInstant(localDateTimeString, Zones.UTC)).isNotEmpty();
    Assertions.assertThat(Dates.asInstant(localDateTimeString, Zones.UTC).get())
        .isEqualTo(Dates.asInstant(localDateTimeString + "Z").get());

    localDateTimeString = "2026-01-17T16:12:47";
    ldt = Dates.asLocalDateTime(localDateTimeString).get();
    Assertions.assertThat(Dates.asInstant(localDateTimeString))
        .withFailMessage("dates without zone info cannot resolve to an instant")
        .isEmpty();
    Assertions.assertThat(Dates.asInstant(localDateTimeString, Zones.UTC)).isNotEmpty();
    Assertions.assertThat(Dates.asInstant(localDateTimeString, Zones.UTC).get())
        .isEqualTo(Dates.asInstant(localDateTimeString + "Z").get());
  }

  public void checkInstant(Instant t) {

    Assertions.assertThat(Dates.asInstant(t.toString()).get()).isEqualTo(t);
    Assertions.assertThat(Dates.asZonedDateTime(t.toString()).get()).isEqualTo(t.atZone(Zones.UTC));
    Assertions.assertThat(Dates.asZonedDateTime(t.toString(), Zones.NYC).get().getZone())
        .isEqualTo(Zones.NYC);
    Assertions.assertThat(Dates.asInstant(t.toString()).get()).isEqualTo(t);
    Assertions.assertThat(Dates.asOffsetDateTime(t.toString()).get().toZonedDateTime())
        .isEqualTo(ZonedDateTime.ofInstant(t, Zones.LAX));

    Assertions.assertThat(
            Dates.asInstant(Dates.asZonedDateTime(t.toString(), Zones.NYC).get().toString()).get())
        .isEqualTo(t);
    Assertions.assertThat(
            Dates.asInstant(
                    Dates.asOffsetDateTime(t.toString(), ZoneOffset.ofHours(-6)).get().toString())
                .get())
        .isEqualTo(t);
    Assertions.assertThat(Dates.asZonedDateTime("\t " + t.toString() + " ").get())
        .isEqualTo(t.atZone(Zones.UTC));

    Assertions.assertThat(Dates.asLocalDate(t.toString()).get().toString())
        .isEqualTo(t.toString().substring(0, 10));
    Assertions.assertThat(Dates.asLocalDateTime(t.toString()).get().toString())
        .isEqualTo(ZonedDateTime.ofInstant(t, Zones.UTC).toLocalDateTime().toString());
  }

  @Test
  public void autoTest() throws Exception {

    Random r = new Random();
    long maxRange = ZonedDateTime.of(2100, 1, 1, 0, 0, 0, 0, Zones.UTC).toInstant().toEpochMilli();

    for (int count = 0; count < 10000; count++) {
      checkInstant(Instant.ofEpochMilli(r.nextLong() % maxRange));
    }
  }
}
