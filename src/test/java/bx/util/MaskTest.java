package bx.util;

import org.junit.jupiter.api.Test;

public class MaskTest extends BxTest {

  @Test
  public void testIt() {

    org.assertj.core.api.Assertions.assertThat(Mask.mask(null)).isEqualTo("*****");

    org.assertj.core.api.Assertions.assertThat(Mask.mask("")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask(" ")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("1")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("12")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("123")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("1234")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("12345")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("123456")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("1234567")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("12345678")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("123456789")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("1234567890")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("12345678901")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("123456789012")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("1234567890123")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("12345678901234")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("123456789012345")).isEqualTo("12*****45");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("1234567890123456"))
        .isEqualTo("12*****56");
  }

  @Test
  public void testKV() {
    org.assertj.core.api.Assertions.assertThat(Mask.mask("password", "foo")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("name", "foo")).isEqualTo("foo");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("API_KEY", "foo")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("apikey", "foo")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(Mask.mask("secret", "foo")).isEqualTo("*****");
    org.assertj.core.api.Assertions.assertThat(
            Mask.mask("aws_access_key_id", "AKONPALTGT52J2VDW5YY"))
        .isEqualTo("AK*****YY");
    org.assertj.core.api.Assertions.assertThat(
            Mask.mask("aws_secret_access_key", "hWLCdl3BH0nTjeLvOSWmdofsCcLC7ZUkmEJxvbNF"))
        .isEqualTo("hW*****NF");
  }
}
