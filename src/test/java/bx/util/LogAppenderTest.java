package bx.util;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;
import com.google.common.collect.Maps;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

@Order(value = Integer.MAX_VALUE)
public class LogAppenderTest extends AppenderBase<ILoggingEvent> {

  static Map<String, AtomicInteger> counters = Maps.newConcurrentMap();

  static Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testIt() {

    counters.keySet().stream()
        .sorted()
        .forEach(
            key -> {
              logger.atInfo().log("{}={}", key, counters.get(key).get());
            });
  }

  @Override
  protected void append(ILoggingEvent eventObject) {

    String name = eventObject.getLoggerName();
    if (name == null) {
      return;
    }
    if (!name.endsWith("Test")) {

      getCounter(eventObject).incrementAndGet();
    }
  }

  AtomicInteger getCounter(ILoggingEvent event) {

    String key = String.format("%s.%s", event.getLoggerName(), event.getLevel());

    AtomicInteger counter = counters.get(key);
    if (counter == null) {
      counter = new AtomicInteger();
      counters.put(key, counter);
    }
    return counter;
  }
}
