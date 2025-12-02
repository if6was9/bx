package bx.util;

import org.junit.jupiter.api.Test;

public class ConfigTest {

  @Test
  public void testIt() {

    Config cfg = Config.get();

    System.out.println(cfg.getKeys());
  }
}
