package bx.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class HttpResponseExceptionTest {

  @Test
  public void testIt() {

    Assertions.assertThat(new HttpResponseException(404).getMessage())
        .isEqualTo("(httpStatus=404)");

    Assertions.assertThat(new HttpResponseException(404, null).getMessage())
        .isEqualTo("(httpStatus=404)");
    Assertions.assertThat(new HttpResponseException(404, "").getMessage())
        .isEqualTo("(httpStatus=404)");
    Assertions.assertThat(new HttpResponseException(404, " some kind of problem ").getMessage())
        .isEqualTo("some kind of problem (httpStatus=404)");
  }
}
