package bx.util;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class RateCounter {

  String label = "counter";
  AtomicLong counter = new AtomicLong();
  Stopwatch stopwatch;
  Logger logger;

  Level level = Level.DEBUG;
  TimeUnit rateUnit = TimeUnit.SECONDS;

  int every = -1;

  private RateCounter() {}

  public static RateCounter create() {

    return new RateCounter().new Builder().build();
  }

  public static Builder builder() {
    return new RateCounter().new Builder();
  }

  public class Builder {

    private Builder() {
      logger =
          LoggerFactory.getLogger(
              Classes.findEnclosingClassNameExcluding(
                      Set.of(RateCounter.Builder.class, RateCounter.class))
                  .orElse(RateCounter.class.getName()));
    }

    public RateCounter build() {

      stopwatch = Stopwatch.createStarted();
      return RateCounter.this;
    }

    public Builder logger(Logger logger) {
      Preconditions.checkNotNull(logger);
      RateCounter.this.logger = logger;
      return this;
    }

    public Builder name(String name) {
      RateCounter.this.label = name;
      return this;
    }

    public Builder every(int n) {
      Preconditions.checkArgument(n > 0);
      RateCounter.this.every = n;
      return this;
    }

    public Builder level(Level logLevel) {
      Preconditions.checkNotNull(logLevel);
      RateCounter.this.level = logLevel;
      return this;
    }
  }

  public long increment() {
    long c = counter.incrementAndGet();
    if (every > 0 && c % every == 0) {

      log();
    }
    return c;
  }

  static double toRate(long count, Duration d, TimeUnit unit) {

    long elapsed = d.toMillis();
    if (elapsed == 0) {
      elapsed = 1;
    }
    return (((double) count) / elapsed) * TimeUnit.MILLISECONDS.convert(1, unit);
  }

  private String toRateUnitName(TimeUnit unit) {
    switch (unit) {
      case SECONDS:
        return "sec";
      case MILLISECONDS:
        return "ms";
      case MINUTES:
        return "min";
      case HOURS:
        return "hour";
      default:
    }

    return unit.toString();
  }

  String toString(long c, Duration d) {
    return MoreObjects.toStringHelper("")
        .add("name", label)
        .add("count", c)
        .add("elapsed", String.format("%sms", d.toMillis()))
        .add(
            "rate",
            String.format(
                "%s/%s", Rounding.format(toRate(c, d, rateUnit), 1), toRateUnitName(rateUnit)))
        .toString();
  }

  public long getCount() {
    return counter.get();
  }

  public double getRate() {
    return toRate(getCount(), stopwatch.elapsed(), rateUnit);
  }

  public String toString() {
    long c = counter.get();
    Duration d = stopwatch.elapsed();
    return toString(c, d);
  }

  public void log() {

    logger.atLevel(level).log("{}", toString());
  }
}
