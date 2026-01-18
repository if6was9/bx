package bx.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class ClasspathResourcesTest extends BxTest {

  @Test
  public void testIt() throws IOException {

    Assertions.assertThat(ClasspathResources.asCharSource("junit-platform.properties").read())
        .contains("ClassOrderer");
    Assertions.assertThat(ClasspathResources.asCharSource("/junit-platform.properties").read())
        .contains("ClassOrderer");

    Assertions.assertThat(
            new String(ClasspathResources.asByteSource("junit-platform.properties").read()))
        .contains("ClassOrderer");
    Assertions.assertThat(
            new String(ClasspathResources.asByteSource("/junit-platform.properties").read()))
        .contains("ClassOrderer");

    ClasspathResources.asJsonNode("config.yml");

    expect(
        FileNotFoundException.class,
        () -> {
          ClasspathResources.asByteSource("/notfound").read();
        });

    expect(
        FileNotFoundException.class,
        () -> {
          ClasspathResources.asCharSource("/notfound").read();
        });

    expect(
        BxException.class,
        () -> {
          ClasspathResources.asJsonNode("/notfound");
        });
  }
}
