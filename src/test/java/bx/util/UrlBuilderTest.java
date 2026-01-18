package bx.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class UrlBuilderTest extends BxTest {

  @Test
  public void testIt() {

    Assertions.assertThat(UrlBuilder.create().toString()).isEqualTo("https://localhost");
    Assertions.assertThat(UrlBuilder.create().host("myhost").toString())
        .isEqualTo("https://myhost");
    Assertions.assertThat(UrlBuilder.create().port(443).host("myhost").toString())
        .isEqualTo("https://myhost");
    Assertions.assertThat(UrlBuilder.create().port(8443).host("myhost").toString())
        .isEqualTo("https://myhost:8443");

    Assertions.assertThat(UrlBuilder.create().protocol("http").host("myhost").toString())
        .isEqualTo("http://myhost");
    Assertions.assertThat(UrlBuilder.create().protocol("http").port(80).host("myhost").toString())
        .isEqualTo("http://myhost");
    Assertions.assertThat(UrlBuilder.create().protocol("http").port(8000).host("myhost").toString())
        .isEqualTo("http://myhost:8000");
    Assertions.assertThat(
            UrlBuilder.create().protocol("http").host("myhost").path("/foo/").toString())
        .isEqualTo("http://myhost/foo");
    Assertions.assertThat(
            UrlBuilder.create()
                .protocol("http")
                .host("myhost")
                .path("/foo/")
                .queryParam("foo", "bar")
                .queryParam("fizz", "buzz")
                .toString())
        .isEqualTo("http://myhost/foo?foo=bar&fizz=buzz");

    Assertions.assertThat(UrlBuilder.create("https://google.com/fizz").toString())
        .isEqualTo("https://google.com/fizz");
  }
}
