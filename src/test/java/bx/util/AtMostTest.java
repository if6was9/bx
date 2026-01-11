package bx.util;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.assertj.core.api.Assertions;
import org.assertj.core.util.Lists;
import org.junit.jupiter.api.Test;

public class AtMostTest {

  @Test
  public void testIt() {

    int period = 100;
    AtMost limiter = AtMost.every(period, TimeUnit.MILLISECONDS);

    // times that the  block was actually executed
    List<Long> invocationTimes = Lists.newArrayList();

    long iterations = 0; // total count of loop iterations

    // loop until the critical block has been executed 5 times
    while (invocationTimes.size() < 5) {
      iterations++;
      limiter.invoke(
          () -> {
            invocationTimes.add(System.currentTimeMillis());
          });
    }

    // the loop should have executed many thousands of times
    // wile the function only exected 5 times
    Assertions.assertThat(iterations).isGreaterThan(10000);
    for (int i = 1; i < invocationTimes.size(); i++) {

      // assert that there was 100ms minimum duration between recorded times
      Assertions.assertThat(invocationTimes.get(i) - invocationTimes.get(i - 1))
          .isGreaterThan(period);
    }
  }
}
