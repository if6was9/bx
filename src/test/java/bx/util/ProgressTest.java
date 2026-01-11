package bx.util;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;

public class ProgressTest {

  Logger logger = Slogger.forEnclosingClass();

  @Test
  public void testIt() {

    var progress = Progress.ofTotal(200).atMostEverySec(1).message("test progress").logger(logger);
    for (int i = 0; i <= 200; i++) {
      progress.increment();
      Sleep.sleepMillis(10);
    }
  }
}
