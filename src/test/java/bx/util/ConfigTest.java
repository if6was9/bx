package bx.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigTest {

  @Test
  public void testIt() {

    Config cfg = Config.get();

    System.getenv()
        .keySet()
        .forEach(
            key -> {
              Assertions.assertThat(cfg.get(key).orElse("")).isEqualTo(System.getenv(key));
            });
    System.out.println(cfg.getKeys());
  }

  @Test
  public void testXX() {
    Config cfg = Config.get();

    Assertions.assertThat(cfg.getAppName()).isEqualTo("bx");

    System.setProperty("app.name", "foo");

    Assertions.assertThat(cfg.getAppName()).isEqualTo("foo");

    System.setProperty("app.name", "bx");
  }
}
