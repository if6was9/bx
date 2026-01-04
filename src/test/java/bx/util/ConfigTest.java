package bx.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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

    Assertions.assertThat(cfg.get("classpathtest").get()).isEqualTo("from classpath");
  }

  @Test
  public void testXX() {
    Config cfg = new Config();

    Assertions.assertThat(cfg.getAppName()).isEqualTo("bx");

    System.out.println();
    System.setProperty("app.name", "foo");

    cfg = new Config();
    Assertions.assertThat(cfg.getAppName()).isEqualTo("foo");

    System.setProperty("app.name", "bx");

    cfg.reset();
    Assertions.assertThat(cfg.getAppName()).isEqualTo("bx");
  }

  @Test
  public void testSrcMainResources() throws IOException {
    Files.walk(new File("./src/main/resources").toPath())
        .filter(p -> p.toFile().isFile())
        .forEach(
            it -> {
              Assertions.fail("should not be present: %s", it);
            });
  }
}
