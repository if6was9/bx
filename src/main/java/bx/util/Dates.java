package bx.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;

public class Dates {

  private static final DateTimeFormatter DTS_PATTERN_0 =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX");
  private static final DateTimeFormatter DTS_PATTERN_1 =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSX");
  private static final DateTimeFormatter DTS_PATTERN_2 =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX");
  private static final DateTimeFormatter DTS_PATTERN_3 =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");
  private static final DateTimeFormatter DTS_PATTERN_4 =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX");
  private static final DateTimeFormatter DTS_PATTERN_5 =
      DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");

  private static final List<DateTimeFormatter> DTS_FORMATTERS =
      List.of(
          DateTimeFormatter.ISO_DATE_TIME,
          DateTimeFormatter.ISO_INSTANT,
          DTS_PATTERN_0,
          DTS_PATTERN_1,
          DTS_PATTERN_2,
          DTS_PATTERN_3,
          DTS_PATTERN_4,
          DTS_PATTERN_5);




  public static Optional<Instant> asInstant(String s) {
    Optional<ZonedDateTime> x = asZonedDateTime(s);
    if (x.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(x.get().toInstant());
  }

  public static Optional<ZonedDateTime> asZonedDateTime(String s, List<DateTimeFormatter> list) {
    if (list == null) {
      return Optional.empty();
    }
    if (s == null) {
      return Optional.empty();
    }

    for (DateTimeFormatter dtf : list) {
      if (dtf != null) {
        Optional<TemporalAccessor> ta = parse(s, dtf);
        if (!ta.isEmpty()) {
          return asZonedDateTime(ta.get());
        }
      }
    }
    return Optional.empty();
  }

  public static Optional<ZonedDateTime> asZonedDateTime(String s, DateTimeFormatter f) {

    Optional<TemporalAccessor> ta = parse(s, f);
    if (ta.isEmpty()) {
      return Optional.empty();
    }

    return asZonedDateTime(ta.get());
  }

  public static Optional<LocalDate> asLocalDate(String s) {
    if (S.isBlank(s)) {
      return Optional.empty();
    }

    try {
      LocalDate dt =
          LocalDate.parse(
              s.length() >= 10 ? s.substring(0, 10) : s, DateTimeFormatter.ISO_LOCAL_DATE);
      return Optional.of(dt);
    } catch (Exception ignore) {

    }
    try {
      LocalDate dt =
          LocalDate.parse(
              s.length() >= 8 ? s.substring(0, 8) : s, DateTimeFormatter.ofPattern("yyyyMMdd"));
      return Optional.of(dt);
    } catch (Exception ignore) {

    }
    try {
      LocalDate dt =
          LocalDate.parse(
              s.length() >= 10 ? s.substring(0, 10) : s, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
      return Optional.of(dt);
    } catch (Exception ignore) {

    }

    Optional<LocalDateTime> dt = asLocalDateTime(s);
    if (dt.isPresent()) {
      return Optional.of(
          LocalDate.of(dt.get().getYear(), dt.get().getMonthValue(), dt.get().getDayOfMonth()));
    }

    Optional<ZonedDateTime> zdt = asZonedDateTime(s);
    if (zdt.isPresent()) {
      return Optional.of(zdt.get().toLocalDate());
    }

    return Optional.empty();
  }

  public static Optional<LocalDateTime> asLocalDateTime(final String input) {
    if (S.isBlank(input)) {
      return Optional.empty();
    }

    String s = input;
    // hack!!!
    if (s.endsWith(".000000Z")) {
      s = s.replace(".000000Z", "");
    }

    try {
      LocalDateTime dt = LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
      return Optional.of(dt);
    } catch (Exception ignore) {

    }

    try {
      LocalDate dt = LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
      return Optional.of(dt.atStartOfDay());
    } catch (Exception ignore) {

    }
    try {
      LocalDate dt = LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyyMMdd"));
      return Optional.of(dt.atStartOfDay());
    } catch (Exception ignore) {

    }
    try {
      LocalDate dt = LocalDate.parse(s, DateTimeFormatter.ofPattern("yyyy/MM/dd"));
      return Optional.of(dt.atStartOfDay());
    } catch (Exception ignore) {

    }

    Optional<ZonedDateTime> zdt = asZonedDateTime(input);
    if (zdt.isPresent()) {
      return Optional.ofNullable(zdt.get().toLocalDateTime());
    }

    return Optional.empty();
  }



  public static Optional<ZonedDateTime> asZonedDateTime(String s) {

    if (S.isBlank(s)) {
      return Optional.empty();
    }
    Optional<ZonedDateTime> dt = asZonedDateTime(s, DateTimeFormatter.ISO_DATE);
    if (dt.isPresent()) {
      return dt;

    }

    dt = asZonedDateTime(s, DTS_FORMATTERS);

    if (dt.isPresent()) {
      return dt;
    }


 
    return Optional.empty();
  }



  public static List<LocalDate> localDateRange(ZonedDateTime from, ZonedDateTime to) {
    return localDateRange(from.toLocalDate(), to.toLocalDate());
  }

  public static List<LocalDate> localDateRange(LocalDate from, LocalDate to) {
    List<LocalDate> list = Lists.newArrayList();
    LocalDate d = from;
    while (!d.isAfter(to)) {
      list.add(d);
      d = d.plus(1, ChronoUnit.DAYS);
    }
    return list;
  }


  public static Optional<Instant> parseEpochMilli(String s) {
    if (S.isBlank(s)) {
      return Optional.empty();
    }
    try {
    
    return Optional.of(Instant.ofEpochMilli(Long.parseLong(s)));
    }
    catch (Exception ignore) {
      //ignore
    }  
    return Optional.empty();
  }
  
  public static Optional<Instant> parseEpochSecond(String s) {
    if (S.isBlank(s)) {
      return Optional.empty();
    }
    try {
    
    return Optional.of(Instant.ofEpochSecond(Long.parseLong(s)));
    }
    catch (Exception ignore) {
      //ignore
    }  
    return Optional.empty();
  }


 

  //// Everything after this point is NEW

  public static Optional<TemporalAccessor> parse(String input, DateTimeFormatter... formatters) {
    return parse(input, List.of(formatters));
  }

  public static Optional<TemporalAccessor> parse(String input, List<DateTimeFormatter> formatters) {

    if (formatters == null) {
      return Optional.empty();
    }
    if (S.isBlank(input)) {
      return Optional.empty();
    }

    for (DateTimeFormatter dtf : formatters) {

      if (dtf != null) {
        try {
          TemporalAccessor ta = dtf.parse(input);
          if (ta != null) {
            return Optional.of(ta);
          }
        } catch (Exception e) {
          // ignore
        }
      }
    }

    return Optional.empty();
  }

  public static Optional<LocalDateTime> asLocalDateTime(TemporalAccessor ta) {
    if (ta == null) {
      return Optional.empty();
    }
    try {
      LocalDateTime t = LocalDateTime.from(ta);
      return Optional.ofNullable(t);
    } catch (Exception ignore) {

    }
    return Optional.empty();
  }

  public static Optional<OffsetDateTime> asOffsetDateTime(String input) {
    Optional<ZonedDateTime> dt = asZonedDateTime(input);
    if (dt.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(dt.get().toOffsetDateTime());
  }

  public static Optional<OffsetDateTime> asOffsetDateTime(TemporalAccessor ta) {
    Optional<ZonedDateTime> dt = asZonedDateTime(ta);
    if (dt.isEmpty()) {
      return Optional.empty();
    }
    return Optional.of(dt.get().toOffsetDateTime());
  }

  public static Optional<ZonedDateTime> asZonedDateTime(TemporalAccessor ta) {

    if (ta == null) {
      return Optional.empty();
    }
    try {
      ZonedDateTime t = ZonedDateTime.from(ta);
      return Optional.ofNullable(t);
    } catch (Exception ignore) {

    }
    return Optional.empty();
  }

  public static Optional<LocalTime> asLocalTime(TemporalAccessor ta) {
    if (ta == null) {
      return Optional.empty();
    }
    try {
      LocalTime t = LocalTime.from(ta);
      return Optional.ofNullable(t);
    } catch (Exception ignore) {

    }
    return Optional.empty();
  }

  public static Optional<LocalTime> asLocalTime(String val) {

    if (S.isBlank(val)) {
      return Optional.empty();
    }

    Optional<TemporalAccessor> ta = parse(val, DateTimeFormatter.ISO_LOCAL_TIME);
    if (ta.isPresent()) {
      return asLocalTime(ta.get());
    }

    ta = parse(val, DateTimeFormatter.ISO_DATE_TIME, DateTimeFormatter.ISO_INSTANT);
    if (ta.isPresent()) {
      return asLocalTime(ta.get());
    }

    return Optional.empty();
  }
}
