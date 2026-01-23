package bx.util;

import com.google.common.base.Preconditions;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

public class TimeSeriesValue implements Comparable<TimeSeriesValue> {

  ZonedDateTime date;
  Number val;

  public TimeSeriesValue(LocalDate d, Number n) {
    Preconditions.checkNotNull(d, "date");
    this.date = d.atStartOfDay(Zones.UTC);
    this.val = n;
  }

  public TimeSeriesValue(Instant instant, Number n) {
    this(instant.atZone(Zones.UTC), n);
  }

  public TimeSeriesValue(ZonedDateTime dateTime, Number n) {
    Preconditions.checkNotNull(dateTime, "dateTime");
    this.date = dateTime;
    this.val = n;
  }

  public LocalDate getLocalDate() {
    return date.toLocalDate();
  }

  public Optional<? extends TimeSeriesValue> notNull() {
    if (val == null) {
      return Optional.empty();
    }
    return Optional.of(this);
  }

  public ZonedDateTime getZonedDateTime() {
    return date;
  }

  public Optional<Number> getValue() {
    return Optional.ofNullable(val);
  }

  public Optional<Double> getDouble() {
    if (val == null) {
      return Optional.empty();
    }
    return Optional.of(val.doubleValue());
  }

  public Optional<Long> getLong() {
    if (val == null) {
      return Optional.empty();
    }
    return Optional.of(val.longValue());
  }

  public boolean isStartOfDay() {
    if (date.toInstant().equals(date.truncatedTo(ChronoUnit.DAYS).toInstant())) {
      return true;
    }
    return false;
  }

  public LocalDateTime getLocalDateTime() {
    return date.toLocalDateTime();
  }

  public String getLocalString() {
    if (isStartOfDay()) {
      return date.toLocalDate().toString();
    } else {
      return date.toLocalDateTime().toString();
    }
  }

  public String toString() {

    return String.format("(%s, %s)", getLocalString(), val);
  }

  @Override
  public int compareTo(TimeSeriesValue o) {

    if (o == null) {
      return 1;
    }
    return this.date.compareTo(o.date);
  }
}
