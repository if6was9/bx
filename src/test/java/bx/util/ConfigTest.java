package bx.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ConfigTest {

  @Test
  public void testIt() {

    ConfigImpl cfg = ConfigImpl.get();

    try {
      cfg.getProperties().keySet().add("foo");
      Assertions.failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
    } catch (UnsupportedOperationException expected) {

    }

    try {
      cfg.getProperties().put("foo", "bar");
      Assertions.failBecauseExceptionWasNotThrown(UnsupportedOperationException.class);
    } catch (UnsupportedOperationException expected) {

    }
    System.getenv()
        .keySet()
        .forEach(
            key -> {
              Assertions.assertThat(cfg.get(key).orElse("")).isEqualTo(System.getenv(key));
            });

    Assertions.assertThat(cfg.get("classpathtest").get()).isEqualTo("from classpath");
  }

  @Test
  public void testAppName() {
    ConfigImpl cfg = new ConfigImpl();

    Assertions.assertThat(cfg.getAppName()).isEqualTo("bx");

    System.out.println();
    System.setProperty("app.name", "foo");

    cfg = new ConfigImpl();
    Assertions.assertThat(cfg.getAppName()).isEqualTo("foo");

    System.setProperty("app.name", "");

    cfg.reload();
    Assertions.assertThat(cfg.getAppName()).isEqualTo("bx");
  }

  @Test
  public void testAppName2() {
    Config cfg = Config.get();

    Assertions.assertThat(cfg.getAppName()).isEqualTo("bx");

    System.out.println();
    System.setProperty("app.name", "foo");

    cfg.reload();
    Assertions.assertThat(cfg.getAppName()).isEqualTo("foo");

    System.setProperty("app.name", "");

    cfg.reload();
    Assertions.assertThat(cfg.getAppName()).isEqualTo("bx");
  }

  @Test
  public void testSrcMainResources() throws IOException {

    File smr = new File("./src/main/resources");
    if (!smr.exists()) {
      return;
    }
    Files.walk(smr.toPath())
        .filter(p -> p.toFile().isFile())
        .forEach(
            it -> {
              Assertions.fail("should not be present: %s", it);
            });
  }

  @Test
  public void testBoolean() {

    Assertions.assertThat(Config.just(Map.of()).getBoolean("b")).isEmpty();

    Assertions.assertThat(Config.just(Map.of("b", "true")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", "TrUe")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", "1")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", "enabled")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", "enable")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", "t")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", "yes")).getBoolean("b").get()).isTrue();

    Assertions.assertThat(Config.just(Map.of("b", "true ")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", " TrUe ")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", " 1")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", " enabled")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", "enable")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", "t")).getBoolean("b").get()).isTrue();
    Assertions.assertThat(Config.just(Map.of("b", "yes")).getBoolean("b").get()).isTrue();

    Assertions.assertThat(Config.just(Map.of("b", "false")).getBoolean("b").get()).isFalse();
    Assertions.assertThat(Config.just(Map.of("b", "fAlSe")).getBoolean("b").get()).isFalse();
    Assertions.assertThat(Config.just(Map.of("b", "0")).getBoolean("b").get()).isFalse();
    Assertions.assertThat(Config.just(Map.of("b", "disabled")).getBoolean("b").get()).isFalse();
    Assertions.assertThat(Config.just(Map.of("b", "disable")).getBoolean("b").get()).isFalse();
    Assertions.assertThat(Config.just(Map.of("b", "f")).getBoolean("b").get()).isFalse();
    Assertions.assertThat(Config.just(Map.of("b", "no")).getBoolean("b").get()).isFalse();

    Assertions.assertThat(Config.just(Map.of("b", "nope")).getBoolean("b")).isEmpty();
    Assertions.assertThat(Config.just(Map.of("b", "yessir")).getBoolean("b")).isEmpty();
  }

  @Test
  public void testInt() {
    Assertions.assertThat(Config.just(Map.of("v", "BS")).getInt("v")).isEmpty();
    Assertions.assertThat(Config.just(Map.of("v", "123")).getInt("v").get()).isEqualTo(123);
  }

  @Test
  public void testLong() {
    Assertions.assertThat(Config.just(Map.of("v", "BS")).getInt("v")).isEmpty();
    Assertions.assertThat(Config.just(Map.of("v", "123456")).getLong("v").get()).isEqualTo(123456);
  }

  @Test
  public void testDouble() {
    Assertions.assertThat(Config.just(Map.of("v", "BS")).getDouble("v")).isEmpty();
    Assertions.assertThat(Config.just(Map.of("v", "123.45")).getDouble("v").get())
        .isEqualTo(123.45);
  }

  @Test
  public void testImmutable() {

    Config cfg = Config.just(null);
    Assertions.assertThat(cfg.getProperties()).isEmpty();

    cfg = Config.just(Map.of("APP_NAME", "foo"));
    Assertions.assertThat(cfg.getAppName()).isEqualTo("foo");

    cfg = Config.just(Map.of("app.name", "fizz"));
    Assertions.assertThat(cfg.getAppName()).isEqualTo("fizz");
  }
}
