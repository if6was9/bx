package bx.util;

import com.google.common.base.Preconditions;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;

/**
 *  Convenience for restricting the frequency of repeated executions. For example:
 *  execute a block of code at most once per second, once every minute, etc.
 */
public class AtMost {

  static Logger logger = Slogger.forEnclosingClass();

  AtomicLong last = new AtomicLong();
  long durationMilli = 1000;

  private AtMost() {}

  public static AtMost every(int n, TimeUnit unit) {

    Preconditions.checkNotNull(unit, "TimeUnit must be set");
    AtMost am = new AtMost();
    am.durationMilli = TimeUnit.MILLISECONDS.convert(n, unit);

    return am;
  }

  public void invoke(Runnable r) {
    long now = System.currentTimeMillis();

    if (now - last.get() < durationMilli) {
      logger.atTrace().log("skipping execution");
      return;
    }

    boolean allowed = false;
    synchronized (this) {
      now = System.currentTimeMillis();
      if (now - last.get() > durationMilli) {
        allowed = true;
        last.set(now);
      }
    }
    if (allowed) {
      r.run();
    }
  }
}
