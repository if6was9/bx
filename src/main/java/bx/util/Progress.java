package bx.util;

import com.google.common.base.Preconditions;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

public class Progress {

  long totalCount = 0;

  AtomicLong current = new AtomicLong();
  AtomicLong lastLog = new AtomicLong(System.currentTimeMillis());

  Duration atMostEvery = Duration.ofSeconds(1);

  Logger logger = LoggerFactory.getLogger(Progress.class);
  Level level = Level.INFO;

  AtomicBoolean finalLog = new AtomicBoolean();

  String message = "progress";

  private Progress() {}

  public static Progress ofTotal(long total) {
    Progress p = new Progress();
    p.totalCount = total;
    return p;
  }

  public Progress message(String message) {
    this.message = message;
    return this;
  }

  public Progress level(Level level) {
    this.level = level;
    return this;
  }

  public Progress logger(Logger logger) {
    this.logger = logger;
    return this;
  }

  public void set(long val) {
    current.set(val);
    maybeLog();
  }

  public void increment() {
    current.incrementAndGet();
    maybeLog();
  }

  public Progress atMostEverySec(int t) {
    return atMostEvery(t, TimeUnit.SECONDS);
  }

  public Progress atMostEvery(int t, TimeUnit tu) {
    this.atMostEvery = Duration.ofMillis(TimeUnit.MILLISECONDS.convert(t, tu));
    return this;
  }

  public Progress atMostEvery(Duration d) {
    Preconditions.checkArgument(d != null);
    this.atMostEvery = d;
    return this;
  }

  private void doLog() {
    long c = current.get();

    int pct = (int) ((c / (double) totalCount) * 100);

    logger.atLevel(level).log("{} ({}/{}) - {}%", message, c, totalCount, pct);
  }

  private void maybeLog() {
    long lastTs = lastLog.get();
    long now = System.currentTimeMillis();

    if (current.get() >= totalCount) {
      boolean log = finalLog.compareAndSet(false, true);
      if (log) {
        doLog();
      }
    } else if (now - lastTs > atMostEvery.toMillis()) {
      boolean b = lastLog.compareAndSet(lastTs, now);
      if (b && totalCount > 0) {
        doLog();
      }
    }
  }
}
