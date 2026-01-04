package bx.util;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.event.Level;

public class RateCounterTest {

  @Test
  public void testDivByZero() {
    Assertions.assertThat(RateCounter.toRate(0, Duration.ofMillis(0), TimeUnit.SECONDS))
        .isEqualTo(0.0);
    Assertions.assertThat(RateCounter.toRate(12, Duration.ofMillis(0), TimeUnit.SECONDS))
        .isEqualTo(12000.0);
    Assertions.assertThat(RateCounter.toRate(12, Duration.ofMillis(0), TimeUnit.MINUTES))
        .isEqualTo(12000 * 60.0);
  }

  @Test
  public void testDefaultEvery() {
    Assertions.assertThat(RateCounter.create().every).isLessThan(0);
  }

  @Test
  public void testIt() {

    RateCounter c = RateCounter.builder().name("test").level(Level.INFO).build();
    c.increment();
    c.increment();
    c.increment();
    Sleep.sleep(3, ChronoUnit.MILLIS);
    c.log();

    Assertions.assertThat(c.getCount()).isEqualTo(3);
  }

  @Test
  public void testEvery() {
    RateCounter c = RateCounter.builder().every(10).build();

    for (int i = 0; i < 100; i++) {
      c.increment();
    }
  }
}
