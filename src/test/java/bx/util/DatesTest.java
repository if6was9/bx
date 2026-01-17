package bx.util;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAccessor;

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
            Dates.asZonedDateTime("2024-12-09T21:40:43-08:00[America/Los_Angeles]")
                .get()
                .toEpochSecond())
        .isEqualTo(refDt);

    Assertions.assertThat(Dates.asZonedDateTime("2024-12-10T05:40:43Z[UTC]").get().toEpochSecond())
        .isEqualTo(refDt);
    Assertions.assertThat(Dates.asZonedDateTime("2024-12-10T05:40:43Z").get().toEpochSecond())
        .isEqualTo(refDt);

    Assertions.assertThat(Dates.asZonedDateTime("2024-12-10T05:40:43")).isEmpty();








    Assertions.assertThat(Dates.asLocalDate("2024-12-10").get().getYear()).isEqualTo(2024);
    Assertions.assertThat(Dates.asLocalDate("2024-12-10").get().getMonthValue()).isEqualTo(12);
    Assertions.assertThat(Dates.asLocalDate("2024-12-10").get().getDayOfMonth()).isEqualTo(10);
    Assertions.assertThat(Dates.asLocalDate("20241210").get().getYear()).isEqualTo(2024);
    Assertions.assertThat(Dates.asLocalDate("20241210").get().getMonthValue()).isEqualTo(12);
    Assertions.assertThat(Dates.asLocalDate("20241210").get().getDayOfMonth()).isEqualTo(10);
    Assertions.assertThat(Dates.asLocalDate("2024/12/10").get().getYear()).isEqualTo(2024);
    Assertions.assertThat(Dates.asLocalDate("2024/12/10").get().getMonthValue()).isEqualTo(12);
    Assertions.assertThat(Dates.asLocalDate("2024/12/10").get().getDayOfMonth()).isEqualTo(10);

 


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
  


  
}
