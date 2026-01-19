package bx.util;

import com.google.common.collect.Lists;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAccessor;
import java.util.List;
import java.util.Optional;

/**
 * Collection of tolerant String -> date/time parsing. All operations in this class are guaranteed not to throw exceptions.
 * Optional.empty should be returned on any/all parsing failure.
 */
public class Dates {

  private static final List<DateTimeFormatter> DTS_FORMATTERS =
      List.of(
          DateTimeFormatter.ISO_DATE_TIME,
          DateTimeFormatter.ISO_INSTANT,
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssX"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSSX"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSX"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSX"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'['VV']'"),
          DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss'['VV']'"));

  public static Optional<OffsetDateTime> asOffsetDateTime(String s, ZoneOffset offset) {
    Optional<OffsetDateTime> odt = asOffsetDateTime(s);
    if (odt.isPresent()) {

      if (offset != null) {
        return Optional.of(odt.get().withOffsetSameInstant(offset));
      }
      return odt;
    }
    if (offset == null) {
      return Optional.empty();
    }

    Optional<LocalDateTime> ldt = asLocalDateTime(s);
    if (ldt.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(ldt.get().atOffset(offset));
  }

  public static Optional<ZonedDateTime> asZonedDateTime(String s, ZoneId zone) {
    Optional<ZonedDateTime> zdt = asZonedDateTime(s);
    if (zdt.isPresent()) {

      if (zone != null) {
        return Optional.of(zdt.get().withZoneSameInstant(zone));
      }
      return zdt;
    }

    if (zone == null) {
      return Optional.empty();
    }

    Optional<LocalDateTime> ldt = asLocalDateTime(s);
    if (ldt.isEmpty()) {
      return Optional.empty();
    }

    return Optional.of(ldt.get().atZone(zone));
  }

  public static Optional<Instant> asInstant(String s, ZoneId zone) {

    Optional<ZonedDateTime> zdt = asZonedDateTime(s, zone);
    if (zdt.isPresent()) {
      return Optional.of(zdt.get().toInstant());
    }

    return Optional.empty();
  }

  public static Optional<Instant> asInstant(String s) {
    return asInstant(s, null);
  }

  public static Optional<ZonedDateTime> asZonedDateTime(String s, List<DateTimeFormatter> list) {

    if (s == null) {
      return Optional.empty();
    }
    if (list == null) {
      return Optional.empty();
    }
    s = s.strip();
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

    s = s.strip();
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

    s = s.strip();
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
    s = s.strip();
    Optional<ZonedDateTime> dt = asZonedDateTime(s, DateTimeFormatter.ISO_DATE);
    if (dt.isPresent()) {
      return dt;
    }

    dt = asZonedDateTime(s, DTS_FORMATTERS);

    if (dt.isPresent()) {
      return dt;
    }

    if (s.length() > 11 && s.charAt(10) == ' ') {

      char[] chars = s.toCharArray();
      chars[10] = 'T';
      s = new String(chars);
      return asZonedDateTime(s);
    }
    return Optional.empty();
  }

  public static List<LocalDate> range(LocalDate from, LocalDate to) {
    List<LocalDate> list = Lists.newArrayList();
    LocalDate d = from;
    while (!d.isAfter(to)) {
      list.add(d);
      d = d.plus(1, ChronoUnit.DAYS);
    }
    return list;
  }

  static Optional<Instant> parseEpochMilli(String s) {
    if (S.isBlank(s)) {
      return Optional.empty();
    }
    try {

      s = s.strip();
      return Optional.of(Instant.ofEpochMilli(Long.parseLong(s)));
    } catch (Exception ignore) {
      // ignore
    }
    return Optional.empty();
  }

  static Optional<Instant> parseEpochSecond(String s) {
    if (S.isBlank(s)) {
      return Optional.empty();
    }
    try {
      s = s.strip();
      return Optional.of(Instant.ofEpochSecond(Long.parseLong(s)));
    } catch (Exception ignore) {
      // ignore
    }
    return Optional.empty();
  }

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

    input = input.strip();
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

    val = val.strip();
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
